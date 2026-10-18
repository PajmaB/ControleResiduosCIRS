package com.example.controlecirs;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EstoqueActivity extends AppCompatActivity {

    RecyclerView recyclerEstoque;
    EstoqueAdapter adapter;
    List<ItemResiduo> listaFinalAgrupada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque);

        recyclerEstoque = findViewById(R.id.recyclerEstoque);
        recyclerEstoque.setLayoutManager(new LinearLayoutManager(this));

        listaFinalAgrupada = new ArrayList<>();
        adapter = new EstoqueAdapter(listaFinalAgrupada, this);
        recyclerEstoque.setAdapter(adapter);

        buscarDadosEstoque();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Toda vez que a tela de Estoque voltar a aparecer, ela busca os dados no XAMPP novamente
        buscarDadosEstoque();
    }

    private void buscarDadosEstoque() {
        String url = "http://10.0.0.182/ControleResiduos/listar_residuos.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Usamos um LinkedHashMap para agrupar pelo nome do resíduo e manter a ordem
                        LinkedHashMap<String, ItemResiduo> mapaAgrupamento = new LinkedHashMap<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int id = obj.getInt("id_residuo");
                            String nome = obj.getString("nome_residuo");
                            String tipo = obj.getString("tipo_armazenagem");
                            int qtd = obj.getInt("quantidade");
                            String prioridade = obj.getString("prioridade");

                            // Se o resíduo (ex: Papelão) ainda não existe no mapa, cria ele
                            if (!mapaAgrupamento.containsKey(nome)) {
                                mapaAgrupamento.put(nome, new ItemResiduo(nome));
                            }

                            // Adiciona o sub-item (Fardo, Rack, etc) dentro do resíduo correspondente
                            mapaAgrupamento.get(nome).armazenagens.add(new Armazenagem(id, tipo, qtd, prioridade));
                        }

                        // Passa os dados agrupados para a lista final do Adapter
                        listaFinalAgrupada.clear();
                        listaFinalAgrupada.addAll(mapaAgrupamento.values());

                        // Avisa a tela para se redesenhar com os novos dados
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao ler dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Erro de rede: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}