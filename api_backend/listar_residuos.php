<?php
// Cabeçalhos obrigatórios para permitir acesso externo e definir formato JSON
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Inclui a conexão criada no passo anterior
include_once 'conexao.php';

try {
    // Usando o nome atualizado: tipo_armazenagem
    $query = "SELECT id_residuo, nome_residuo, tipo_armazenagem, quantidade, limite_medio, limite_alto FROM Estoque_residuos";
    $stmt = $conn->prepare($query);
    $stmt->execute();

    $residuos_arr = array();

    // Passa por cada linha retornada do banco
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        
        // O PHP já calcula a prioridade para poupar processamento no celular
        $prioridade = "BAIXA";
        if ($row['quantidade'] >= $row['limite_alto']) {
            $prioridade = "ALTA";
        } elseif ($row['quantidade'] >= $row['limite_medio']) {
            $prioridade = "MEDIA";
        }

        $residuo_item = array(
            "id_residuo" => $row['id_residuo'],
            "nome_residuo" => $row['nome_residuo'],
            "tipo_armazenagem" => $row['tipo_armazenagem'],
            "quantidade" => $row['quantidade'],
            "prioridade" => $prioridade
        );

        array_push($residuos_arr, $residuo_item);
    }

    // Imprime o array convertido em texto JSON
    echo json_encode($residuos_arr);

} catch(Exception $e) {
    echo json_encode(array("erro" => "Falha ao buscar dados: " . $e->getMessage()));
}
?>