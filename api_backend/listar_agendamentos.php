<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
include_once 'conexao.php';

try {
    // Usa o DAYOFWEEK do MySQL para saber o dia da semana (1=Dom, 2=Seg, 3=Ter...)
    $query = "SELECT id_agendamento, data_agendada, horario, empresa_retirada,
              CASE DAYOFWEEK(data_agendada)
                  WHEN 2 THEN 'Segunda-feira'
                  WHEN 3 THEN 'Terça-feira'
                  WHEN 4 THEN 'Quarta-feira'
                  WHEN 5 THEN 'Quinta-feira'
                  WHEN 6 THEN 'Sexta-feira'
              END as dia_semana
              FROM Agendamentos 
              WHERE status = 'Pendente' 
              ORDER BY data_agendada ASC, horario ASC";
              
    $stmt = $conn->prepare($query);
    $stmt->execute();

    $agendamentos_arr = array();
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        array_push($agendamentos_arr, $row);
    }

    echo json_encode($agendamentos_arr);

} catch(Exception $e) {
    echo json_encode(array("erro" => $e->getMessage()));
}
?>