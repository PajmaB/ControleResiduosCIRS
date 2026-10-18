<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

include_once 'conexao.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->data_agendada) && !empty($data->horario) && !empty($data->empresa_retirada) && !empty($data->id_usuario) && !empty($data->itens)) {
    try {
        $conn->beginTransaction(); // Inicia a transação

        // 1. Cria o Agendamento principal
        $queryAgendamento = "INSERT INTO Agendamentos (data_agendada, horario, empresa_retirada, id_usuario_criou) VALUES (:data, :horario, :empresa, :id_usuario)";
        $stmt = $conn->prepare($queryAgendamento);
        $stmt->bindParam(':data', $data->data_agendada);
        $stmt->bindParam(':horario', $data->horario);
        $stmt->bindParam(':empresa', $data->empresa_retirada);
        $stmt->bindParam(':id_usuario', $data->id_usuario);
        $stmt->execute();

        $id_agendamento = $conn->lastInsertId(); // Pega o ID que acabou de ser gerado

        // 2. Insere os itens vinculados a esse agendamento
        $queryItem = "INSERT INTO Agendamentos_Itens (id_agendamento, id_residuo) VALUES (:id_agendamento, :id_residuo)";
        $stmtItem = $conn->prepare($queryItem);

        // O Android deve enviar um array de IDs de resíduos no campo "itens"
        foreach ($data->itens as $id_residuo) {
            $stmtItem->bindParam(':id_agendamento', $id_agendamento);
            $stmtItem->bindParam(':id_residuo', $id_residuo);
            $stmtItem->execute();
        }

        $conn->commit(); // Confirma o salvamento de tudo
        echo json_encode(["status" => "sucesso", "mensagem" => "Agendamento criado com sucesso."]);

    } catch (Exception $e) {
        $conn->rollBack(); // Desfaz tudo se der erro
        echo json_encode(["status" => "erro", "mensagem" => "Falha no agendamento: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "erro", "mensagem" => "Dados incompletos para o agendamento."]);
}
?>