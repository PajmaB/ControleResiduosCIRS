<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

include_once 'conexao.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->id_agendamento) && !empty($data->id_usuario_encerrou)) {
    try {
        $conn->beginTransaction();

        // 1. Busca os dados do Agendamento
        $stmtBusca = $conn->prepare("SELECT empresa_retirada FROM Agendamentos WHERE id_agendamento = :id");
        $stmtBusca->bindParam(':id', $data->id_agendamento);
        $stmtBusca->execute();
        $agendamento = $stmtBusca->fetch(PDO::FETCH_ASSOC);

        if (!$agendamento) {
            throw new Exception("Agendamento não encontrado.");
        }

        // 2. Busca os Resíduos desse agendamento para zerar e criar a string de histórico
        $stmtItens = $conn->prepare("
            SELECT e.id_residuo, e.nome_residuo, e.tipo_armazenagem 
            FROM Agendamentos_Itens ai 
            JOIN Estoque_residuos e ON ai.id_residuo = e.id_residuo 
            WHERE ai.id_agendamento = :id_agendamento
        ");
        $stmtItens->bindParam(':id_agendamento', $data->id_agendamento);
        $stmtItens->execute();
        
        $listaTextualResiduos = [];
        
        // Atualiza a quantidade de cada item para ZERO e preenche a lista textual
        $stmtZerar = $conn->prepare("UPDATE Estoque_residuos SET quantidade = 0 WHERE id_residuo = :id_residuo");
        
        while ($row = $stmtItens->fetch(PDO::FETCH_ASSOC)) {
            $listaTextualResiduos[] = $row['nome_residuo'] . " (" . $row['tipo_armazenagem'] . ")";
            
            $stmtZerar->bindParam(':id_residuo', $row['id_residuo']);
            $stmtZerar->execute();
        }
        
        $descricao_historico = implode(", ", $listaTextualResiduos); // Junta tudo. Ex: "Papelão (Rack), Plástico filme (Fardo)"

        // 3. Salva no Histórico
        $dataAtual = date('Y-m-d');
        $mes = date('m');
        $ano = date('Y');
        
        $stmtHist = $conn->prepare("
            INSERT INTO Historico_Carregamentos 
            (id_agendamento, data_conclusao, mes, ano, empresa_retirada, residuos_descricao, id_usuario_encerrou) 
            VALUES (:id_ag, :data_conclusao, :mes, :ano, :empresa, :descricao, :usuario)
        ");
        $stmtHist->bindParam(':id_ag', $data->id_agendamento);
        $stmtHist->bindParam(':data_conclusao', $dataAtual);
        $stmtHist->bindParam(':mes', $mes);
        $stmtHist->bindParam(':ano', $ano);
        $stmtHist->bindParam(':empresa', $agendamento['empresa_retirada']);
        $stmtHist->bindParam(':descricao', $descricao_historico);
        $stmtHist->bindParam(':usuario', $data->id_usuario_encerrou);
        $stmtHist->execute();

        // 4. Exclui o Agendamento (Como a tabela Agendamentos_Itens tem ON DELETE CASCADE, os itens serão excluídos sozinhos)
        $stmtDelete = $conn->prepare("DELETE FROM Agendamentos WHERE id_agendamento = :id");
        $stmtDelete->bindParam(':id', $data->id_agendamento);
        $stmtDelete->execute();

        $conn->commit();
        echo json_encode(["status" => "sucesso", "mensagem" => "Agendamento concluído. Estoque zerado e salvo no histórico."]);

    } catch (Exception $e) {
        $conn->rollBack();
        echo json_encode(["status" => "erro", "mensagem" => "Falha ao concluir agendamento: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "erro", "mensagem" => "ID do agendamento ou do usuário não informados."]);
}
?>