package com.example.controlecirs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class HistoricoActivity extends AppCompatActivity {

    Spinner spinnerMes, spinnerAno;
    Button btnBuscarHistorico;
    TextView txtStatusBusca;
    RecyclerView recyclerHistorico;

    List<LogItem> listaLogs = new ArrayList<>();
    HistoricoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        // Seta de voltar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Histórico Mensal");
        }

        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerAno = findViewById(R.id.spinnerAno);
        btnBuscarHistorico = findViewById(R.id.btnBuscarHistorico);
        txtStatusBusca = findViewById(R.id.txtStatusBusca);
        recyclerHistorico = findViewById(R.id.recyclerHistorico);

        configurarFiltros();

        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoricoAdapter(listaLogs);
        recyclerHistorico.setAdapter(adapter);

        btnBuscarHistorico.setOnClickListener(v -> buscarDadosHistorico());

        // Já faz uma busca automática assim que a tela abre, usando o mês atual
        buscarDadosHistorico();
    }

    private void configurarFiltros() {
        // Meses
        String[] meses = {"01 - Janeiro", "02 - Fevereiro", "03 - Março", "04 - Abril", "05 - Maio", "06 - Junho",
                "07 - Julho", "08 - Agosto", "09 - Setembro", "10 - Outubro", "11 - Novembro", "12 - Dezembro"};
        ArrayAdapter<String> adapterMes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, meses);
        spinnerMes.setAdapter(adapterMes);

        // Anos (De 2023 até 3 anos no futuro)
        Calendar cal = Calendar.getInstance();
        int anoAtual = cal.get(Calendar.YEAR);
        int mesAtual = cal.get(Calendar.MONTH); // 0 a 11

        List<String> anos = new ArrayList<>();
        for (int i = 2023; i <= anoAtual + 3; i++) {
            anos.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapterAno = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, anos);
        spinnerAno.setAdapter(adapterAno);

        // Seleciona automaticamente o mês e ano atuais nas caixinhas
        spinnerMes.setSelection(mesAtual);
        spinnerAno.setSelection(anos.indexOf(String.valueOf(anoAtual)));
    }

    private void buscarDadosHistorico() {
        listaLogs.clear();
        adapter.notifyDataSetChanged();
        txtStatusBusca.setText("Buscando registros...");

        // Extrai só o número do mês (Ex: "03 - Março" -> "3")
        String mesSelecionado = spinnerMes.getSelectedItem().toString().substring(0, 2);
        int mes = Integer.parseInt(mesSelecionado); // Remove o zero à esquerda se houver
        String ano = spinnerAno.getSelectedItem().toString();

        String url = "http://10.0.0.182/ControleResiduos/listar_historico.php";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mes", mes);
            jsonBody.put("ano", ano);
        } catch (JSONException e) { e.printStackTrace(); }

        // Usamos Custom JsonRequest para mandar POST e receber Array de volta
        CustomJsonArrayRequest request = new CustomJsonArrayRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.length() == 0) {
                            txtStatusBusca.setText("Nenhum carregamento encontrado neste período.");
                        } else {
                            txtStatusBusca.setText(response.length() + " carregamentos encontrados.");
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                listaLogs.add(new LogItem(
                                        obj.getString("data_conclusao"),
                                        obj.getString("empresa_retirada"),
                                        obj.getString("residuos_descricao"),
                                        obj.getString("usuario_nome")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> txtStatusBusca.setText("Erro de rede ao buscar histórico.")
        );

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ================= CLASSES INTERNAS (DADOS E ADAPTER) =================
    class LogItem {
        String data, empresa, residuos, usuario;
        public LogItem(String d, String e, String r, String u) {
            this.data = d; this.empresa = e; this.residuos = r; this.usuario = u;
        }
    }

    class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {
        List<LogItem> logs;

        public HistoricoAdapter(List<LogItem> logs) { this.logs = logs; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LogItem item = logs.get(position);
            holder.txtData.setText(item.data);
            holder.txtEmpresa.setText(item.empresa);
            holder.txtResiduos.setText(item.residuos);
            holder.txtUsuario.setText("Concluído por: " + item.usuario);
        }

        @Override public int getItemCount() { return logs.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtData, txtEmpresa, txtResiduos, txtUsuario;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtData = itemView.findViewById(R.id.txtDataLog);
                txtEmpresa = itemView.findViewById(R.id.txtEmpresaLog);
                txtResiduos = itemView.findViewById(R.id.txtResiduosLog);
                txtUsuario = itemView.findViewById(R.id.txtResponsavelLog);
            }
        }
    }

    // Classe auxiliar nativa para Volley mandar POST e ler JsonArray
    class CustomJsonArrayRequest extends Request<JSONArray> {
        private final JSONObject jsonBody;
        private final com.android.volley.Response.Listener<JSONArray> listener;

        public CustomJsonArrayRequest(int method, String url, JSONObject jsonBody, com.android.volley.Response.Listener<JSONArray> listener, com.android.volley.Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.jsonBody = jsonBody;
            this.listener = listener;
        }

        @Override public byte[] getBody() { return jsonBody != null ? jsonBody.toString().getBytes() : null; }
        @Override public String getBodyContentType() { return "application/json; charset=utf-8"; }

        @Override protected com.android.volley.Response<JSONArray> parseNetworkResponse(com.android.volley.NetworkResponse response) {
            try {
                String jsonString = new String(response.data, com.android.volley.toolbox.HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                return com.android.volley.Response.success(new JSONArray(jsonString), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return com.android.volley.Response.error(new com.android.volley.ParseError(e));
            }
        }

        @Override protected void deliverResponse(JSONArray response) { listener.onResponse(response); }
    }
}