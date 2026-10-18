<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
include_once 'conexao.php';

$data = json_decode(file_get_contents("php://input"));

if (isset($data->mes) && isset($data->ano)) {
    try {
        // Busca os logs do mês e ano selecionados, ordenando dos mais recentes para os mais antigos
        $query = "SELECT h.data_conclusao, h.empresa_retirada, h.residuos_descricao, u.nome AS usuario_nome 
                  FROM Historico_Carregamentos h 
                  JOIN Usuarios u ON h.id_usuario_encerrou = u.id_usuario 
                  WHERE h.mes = :mes AND h.ano = :ano 
                  ORDER BY h.data_conclusao DESC";
                  
        $stmt = $conn->prepare($query);
        $stmt->bindParam(':mes', $data->mes);
        $stmt->bindParam(':ano', $data->ano);
        $stmt->execute();

        $historico_arr = array();
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            // Formata a data de YYYY-MM-DD para DD/MM/YYYY direto no PHP para facilitar
            $dataConvertida = date("d/m/Y", strtotime($row['data_conclusao']));
            $row['data_conclusao'] = $dataConvertida;
            
            array_push($historico_arr, $row);
        }

        echo json_encode($historico_arr);

    } catch (Exception $e) {
        echo json_encode(["erro" => $e->getMessage()]);
    }
} else {
    echo json_encode(["erro" => "Mês e Ano não informados."]);
}
?>