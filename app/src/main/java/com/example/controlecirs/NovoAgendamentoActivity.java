package com.example.controlecirs;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NovoAgendamentoActivity extends AppCompatActivity {

    Button btnSelecionarData, btnAdicionarResiduo, btnSalvarAgendamento;
    Spinner spinnerHorarios;
    EditText editEmpresa;
    LinearLayout layoutResiduosSelecionados;

    String dataFormatadaMySQL = ""; // Vai guardar a data no formato YYYY-MM-DD
    List<Integer> idsResiduosSelecionados = new ArrayList<>(); // Guarda os IDs para enviar ao PHP
    List<String> nomesResiduosDisponiveis = new ArrayList<>();
    List<Integer> idsResiduosDisponiveis = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_agendamento);
        // Ativa a seta de voltar na barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        spinnerHorarios = findViewById(R.id.spinnerHorarios);
        editEmpresa = findViewById(R.id.editEmpresa);
        btnAdicionarResiduo = findViewById(R.id.btnAdicionarResiduo);
        layoutResiduosSelecionados = findViewById(R.id.layoutResiduosSelecionados);
        btnSalvarAgendamento = findViewById(R.id.btnSalvarAgendamento);

        configurarHorarios();
        buscarResiduosParaSelecao();

        // Abre o Calendário ao clicar no botão de Data
        btnSelecionarData.setOnClickListener(v -> abrirCalendario());

        // Abre um Pop-up para escolher o resíduo
        btnAdicionarResiduo.setOnClickListener(v -> mostrarDialogoResiduos());

        // Botão final para salvar
        btnSalvarAgendamento.setOnClickListener(v -> salvarAgendamentoNoBanco());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Destrói a tela atual e revela a tela anterior que estava embaixo
        return true;
    }

    private void configurarHorarios() {
        // Horários exatos permitidos pela regra de negócio
        String[] horarios = {"08:30", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, horarios);
        spinnerHorarios.setAdapter(adapter);
    }

    private void abrirCalendario() {
        Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Formata para o MySQL (YYYY-MM-DD)
            String mesFormatado = String.format("%02d", (month + 1));
            String diaFormatado = String.format("%02d", dayOfMonth);
            dataFormatadaMySQL = year + "-" + mesFormatado + "-" + diaFormatado;

            // Mostra pro usuário no formato BR (DD/MM/YYYY)
            btnSelecionarData.setText(diaFormatado + "/" + mesFormatado + "/" + year);
        }, ano, mes, dia);

        datePicker.show();
    }

    // Busca a lista de resíduos do XAMPP para popular o pop-up de seleção
    private void buscarResiduosParaSelecao() {
        String url = "http://10.0.0.182/ControleResiduos/listar_residuos.php"; // ALTERE PARA SEU IP

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            idsResiduosDisponiveis.add(obj.getInt("id_residuo"));
                            nomesResiduosDisponiveis.add(obj.getString("nome_residuo") + " (" + obj.getString("tipo_armazenagem") + ")");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro ao buscar resíduos.", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void mostrarDialogoResiduos() {
        if (nomesResiduosDisponiveis.isEmpty()) {
            Toast.makeText(this, "Carregando resíduos, aguarde...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] arrayNomes = nomesResiduosDisponiveis.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um Resíduo");
        builder.setItems(arrayNomes, (dialog, which) -> {
            // which é a posição que o usuário clicou na lista
            int idSelecionado = idsResiduosDisponiveis.get(which);
            String nomeSelecionado = nomesResiduosDisponiveis.get(which);

            if (!idsResiduosSelecionados.contains(idSelecionado)) {
                idsResiduosSelecionados.add(idSelecionado);
                adicionarResiduoNaTela(nomeSelecionado);
            } else {
                Toast.makeText(this, "Este resíduo já foi adicionado.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    // Cria uma caixinha de texto visualizando o que foi selecionado
    private void adicionarResiduoNaTela(String nome) {
        TextView txtItem = new TextView(this);
        txtItem.setText("- " + nome);
        txtItem.setTextSize(16f);
        txtItem.setPadding(0, 8, 0, 8);
        layoutResiduosSelecionados.addView(txtItem);
    }

    private void salvarAgendamentoNoBanco() {
        String empresa = editEmpresa.getText().toString();
        String horario = spinnerHorarios.getSelectedItem().toString();

        if (dataFormatadaMySQL.isEmpty() || empresa.isEmpty() || idsResiduosSelecionados.isEmpty()) {
            Toast.makeText(this, "Preencha a data, empresa e adicione pelo menos um resíduo.", Toast.LENGTH_LONG).show();
            return;
        }

        // Pega o ID do usuário que fez o login (Cofre SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("AppResiduosPrefs", MODE_PRIVATE);
        int idUsuarioLogado = prefs.getInt("ID_USUARIO", 1); // 1 é um fallback de segurança

        String url = "http://10.0.0.182/ControleResiduos/inserir_agendamento.php"; // ALTERE PARA SEU IP

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("data_agendada", dataFormatadaMySQL);
            jsonBody.put("horario", horario + ":00"); // Adiciona os segundos para o MySQL (TIME)
            jsonBody.put("empresa_retirada", empresa);
            jsonBody.put("id_usuario", idUsuarioLogado);

            // Converte a lista de IDs para um Array JSON
            JSONArray jsonArrayItens = new JSONArray(idsResiduosSelecionados);
            jsonBody.put("itens", jsonArrayItens);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getString("status").equals("sucesso")) {
                            Toast.makeText(this, "Agendamento salvo!", Toast.LENGTH_SHORT).show();
                            finish(); // Fecha a tela e volta para a lista de agendamentos
                        } else {
                            Toast.makeText(this, response.getString("mensagem"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro de comunicação.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}