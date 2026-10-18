<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
include_once 'conexao.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->login) && !empty($data->senha)) {
    // Busca o usuário no banco
    $query = "SELECT id_usuario, nome FROM Usuarios WHERE login = :login AND senha = :senha LIMIT 1";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(':login', $data->login);
    $stmt->bindParam(':senha', $data->senha);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        echo json_encode(["status" => "sucesso", "id_usuario" => $row['id_usuario'], "nome" => $row['nome']]);
    } else {
        echo json_encode(["status" => "erro", "mensagem" => "Login ou senha incorretos."]);
    }
} else {
    echo json_encode(["status" => "erro", "mensagem" => "Preencha todos os campos."]);
}
?>