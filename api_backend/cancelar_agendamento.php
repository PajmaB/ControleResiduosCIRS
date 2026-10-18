<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
include_once 'conexao.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->id_agendamento)) {
    try {
        $query = "DELETE FROM Agendamentos WHERE id_agendamento = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(':id', $data->id_agendamento);
        
        if ($stmt->execute()) {
            echo json_encode(["status" => "sucesso", "mensagem" => "Agendamento cancelado com sucesso."]);
        } else {
            echo json_encode(["status" => "erro", "mensagem" => "Falha ao cancelar o agendamento."]);
        }
    } catch (Exception $e) {
        echo json_encode(["status" => "erro", "mensagem" => $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "erro", "mensagem" => "ID do agendamento não informado."]);
}
?>