package com.example.controlecirs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class DetalheAgendamentoActivity extends AppCompatActivity {

    TextView txtNomeEmpresaDetalhe;
    Button btnConcluido, btnNaoConcluido;
    int idAgendamento;
    String nomeEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_agendamento);
        // Ativa a seta de voltar na barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txtNomeEmpresaDetalhe = findViewById(R.id.txtNomeEmpresaDetalhe);
        btnConcluido = findViewById(R.id.btnConcluido);
        btnNaoConcluido = findViewById(R.id.btnNaoConcluido);

        // Recebe os dados passados pelo cartão clicado na tela anterior
        idAgendamento = getIntent().getIntExtra("id_agendamento", -1);
        nomeEmpresa = getIntent().getStringExtra("empresa");

        txtNomeEmpresaDetalhe.setText("Empresa: " + nomeEmpresa);

        btnConcluido.setOnClickListener(v -> encerrarAgendamento("concluido"));
        btnNaoConcluido.setOnClickListener(v -> encerrarAgendamento("nao_concluido"));
    }

    private void encerrarAgendamento(String acao) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "";
        JSONObject jsonBody = new JSONObject();

        try {
            if (acao.equals("concluido")) {
                // Chama a API que zera o estoque e cria o histórico
                url = "http://10.0.0.182/ControleResiduos/concluir_agendamento.php";

                SharedPreferences prefs = getSharedPreferences("AppResiduosPrefs", MODE_PRIVATE);
                int idUsuarioLogado = prefs.getInt("ID_USUARIO", 1);

                jsonBody.put("id_agendamento", idAgendamento);
                jsonBody.put("id_usuario_encerrou", idUsuarioLogado);
            } else {
                // Chama a API que apenas deleta o agendamento
                url = "http://10.0.0.182/ControleResiduos/cancelar_agendamento.php";
                jsonBody.put("id_agendamento", idAgendamento);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        String status = response.getString("status");
                        String msg = response.getString("mensagem");

                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                        if (status.equals("sucesso")) {
                            finish(); // Volta para a tela de lista de agendamentos
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro de rede.", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Destrói a tela atual e revela a tela anterior que estava embaixo
        return true;
    }
}