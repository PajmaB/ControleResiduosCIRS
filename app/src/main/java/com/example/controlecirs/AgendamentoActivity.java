package com.example.controlecirs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class AgendamentoActivity extends AppCompatActivity {

    LinearLayout containerSegunda, containerTerca, containerQuarta, containerQuinta, containerSexta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamento);

        containerSegunda = findViewById(R.id.containerSegunda);
        containerTerca = findViewById(R.id.containerTerca);
        containerQuarta = findViewById(R.id.containerQuarta);
        containerQuinta = findViewById(R.id.containerQuinta);
        containerSexta = findViewById(R.id.containerSexta);

        buscarAgendamentos();
    }

    // Recarrega a lista toda vez que a tela volta a aparecer
    @Override
    protected void onResume() {
        super.onResume();
        buscarAgendamentos();
    }

    private void buscarAgendamentos() {
        // Limpa as listas antes de preencher novamente
        containerSegunda.removeAllViews();
        containerTerca.removeAllViews();
        containerQuarta.removeAllViews();
        //containerQuinta.removeAllViews();
        //containerSexta.removeAllViews();

        String url = "http://10.0.0.182/ControleResiduos/listar_agendamentos.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int id = obj.getInt("id_agendamento");
                            String horario = obj.getString("horario").substring(0, 5); // Pega apenas HH:MM
                            String empresa = obj.getString("empresa_retirada");
                            String diaSemana = obj.getString("dia_semana");

                            // Infla o layout do cartão
                            View cardView = inflater.inflate(R.layout.item_agendamento_card, null);
                            TextView txtHorario = cardView.findViewById(R.id.txtHorarioCard);
                            TextView txtEmpresa = cardView.findViewById(R.id.txtEmpresaCard);
                            Button btnAbrir = cardView.findViewById(R.id.btnAbrirAgendamento);

                            txtHorario.setText(horario);
                            txtEmpresa.setText(empresa);

                            // Botão para abrir e encerrar este agendamento específico
                            btnAbrir.setOnClickListener(v -> {
                                android.content.Intent intent = new android.content.Intent(AgendamentoActivity.this, DetalheAgendamentoActivity.class);
                                intent.putExtra("id_agendamento", id);
                                intent.putExtra("empresa", empresa);
                                startActivity(intent);
                            });

                            // Descobre em qual dia da semana encaixar o cartão
                            if (diaSemana != null) {
                                switch (diaSemana) {
                                    case "Segunda-feira":
                                        containerSegunda.addView(cardView);
                                        break;
                                    case "Terça-feira":
                                        containerTerca.addView(cardView);
                                        break;
                                    case "Quarta-feira":
                                        containerQuarta.addView(cardView);
                                        break;
                                    case "Quinta-feira":
                                        containerQuinta.addView(cardView);
                                        break;
                                    case "Sexta-feira":
                                        containerSexta.addView(cardView);
                                        break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // Mantemos o método que abre a tela de NOVO agendamento
    public void abrirNovoAgendamento(View view) {
        Button botaoClicado = (Button) view;
        String diaSelecionado = botaoClicado.getTag().toString();

        Intent intent = new Intent(AgendamentoActivity.this, NovoAgendamentoActivity.class);
        intent.putExtra("diaSemana", diaSelecionado);
        startActivity(intent);
    }
}