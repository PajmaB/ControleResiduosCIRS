<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

include_once 'conexao.php';

// Captura o JSON enviado pelo Android
$data = json_decode(file_get_contents("php://input"));

if (!empty($data->id_residuo) && isset($data->variacao)) {
    try {
        // Atualiza somando a variação (se for negativo, ele subtrai)
        $query = "UPDATE Estoque_residuos SET quantidade = quantidade + :variacao WHERE id_residuo = :id_residuo";
        $stmt = $conn->prepare($query);
        
        $stmt->bindParam(':variacao', $data->variacao);
        $stmt->bindParam(':id_residuo', $data->id_residuo);
        
        if ($stmt->execute()) {
            // Garante que o estoque não fique negativo após a conta
            $conn->query("UPDATE Estoque_residuos SET quantidade = 0 WHERE quantidade < 0");
            echo json_encode(["status" => "sucesso", "mensagem" => "Quantidade atualizada."]);
        } else {
            echo json_encode(["status" => "erro", "mensagem" => "Falha ao atualizar quantidade."]);
        }
    } catch (Exception $e) {
        echo json_encode(["status" => "erro", "mensagem" => $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "erro", "mensagem" => "Dados incompletos."]);
}
?>