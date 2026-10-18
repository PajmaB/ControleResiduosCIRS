<?php
$host = "localhost";
$db_name = "db_controle_residuos";
$username = "root"; 
$password = ""; 

try {
    $conn = new PDO("mysql:host={$host};dbname={$db_name};charset=utf8", $username, $password);
    // Configura para mostrar erros se algo der errado
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $exception) {
    // Se falhar, retorna um erro em JSON
    echo json_encode(["erro" => "Erro de conexão: " . $exception->getMessage()]);
    exit; // Para a execução
}
?>