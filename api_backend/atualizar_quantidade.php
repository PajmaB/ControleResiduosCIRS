<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
include_once 'conexao.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->id_residuo)) {
    try {
        // Se for um resíduo de Volume, atualiza cravando no limite do banco
        if (isset($data->modo) && $data->modo == "volume" && isset($data->nivel)) {
            $query = "UPDATE Estoque_residuos
                      SET quantidade = CASE
                          WHEN :nivel = 'ALTA' THEN limite_alto
                          WHEN :nivel = 'MEDIA' THEN limite_medio
                          ELSE 0
                      END
                      WHERE id_residuo = :id_residuo";
            $stmt = $conn->prepare($query);
            $stmt->bindParam(':nivel', $data->nivel);
            $stmt->bindParam(':id_residuo', $data->id_residuo);
        }
        // Se for um resíduo normal (Fardo, Rack, etc), faz a soma ou subtração
        else if (isset($data->variacao)) {
            $query = "UPDATE Estoque_residuos SET quantidade = quantidade + :variacao WHERE id_residuo = :id_residuo";
            $stmt = $conn->prepare($query);
            $stmt->bindParam(':variacao', $data->variacao);
            $stmt->bindParam(':id_residuo', $data->id_residuo);
        } else {
             echo json_encode(["status" => "erro", "mensagem" => "Dados inválidos."]);
             exit;
        }

        if ($stmt->execute()) {
            // Garante que não fique negativo (para os de variação)
            $conn->query("UPDATE Estoque_residuos SET quantidade = 0 WHERE quantidade < 0");
            echo json_encode(["status" => "sucesso"]);
        } else {
            echo json_encode(["status" => "erro", "mensagem" => "Falha na execução."]);
        }
    } catch (Exception $e) {
        echo json_encode(["status" => "erro", "mensagem" => $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "erro", "mensagem" => "Dados incompletos."]);
}
?>