package com.example.controlecirs;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class AlterarQuantidadeActivity extends AppCompatActivity {

    TextView txtTituloResiduo;
    LinearLayout layoutControles;
    Button btnConfirmarAlteracao;
    ItemResiduo residuo;

    // Array para guardar as variações de cada tipo de armazenagem
    int[] variacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_quantidade);

        txtTituloResiduo = findViewById(R.id.txtTituloResiduo);
        layoutControles = findViewById(R.id.layoutControles);
        btnConfirmarAlteracao = findViewById(R.id.btnConfirmarAlteracao);

        // Recebe o objeto da tela anterior
        residuo = (ItemResiduo) getIntent().getSerializableExtra("residuoSelecionado");

        if (residuo != null) {
            txtTituloResiduo.setText("Alterar: " + residuo.nome);
            variacoes = new int[residuo.armazenagens.size()];
            montarControlesNaTela();
        }

        btnConfirmarAlteracao.setOnClickListener(v -> salvarAlteracoes());
    }

    private void montarControlesNaTela() {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < residuo.armazenagens.size(); i++) {
            Armazenagem arm = residuo.armazenagens.get(i);
            final int index = i; // Necessário para usar dentro dos cliques

            // Infla (desenha) o bloquinho de controle
            View viewControle = inflater.inflate(R.layout.item_controle_quantidade, layoutControles, false);

            TextView txtTipoAtual = viewControle.findViewById(R.id.txtTipoAtual);
            TextView txtVariacao = viewControle.findViewById(R.id.txtVariacao);
            Button btnMenos = viewControle.findViewById(R.id.btnMenos);
            Button btnMais = viewControle.findViewById(R.id.btnMais);

            txtTipoAtual.setText(arm.tipo + " (Atual: " + arm.quantidade + ")");

            btnMais.setOnClickListener(v -> {
                variacoes[index]++;
                atualizarTextoVariacao(txtVariacao, variacoes[index]);
            });

            btnMenos.setOnClickListener(v -> {
                // Evita que a pessoa tente subtrair mais do que existe no estoque
                if ((arm.quantidade + variacoes[index]) > 0) {
                    variacoes[index]--;
                    atualizarTextoVariacao(txtVariacao, variacoes[index]);
                } else {
                    Toast.makeText(this, "Estoque não pode ficar negativo", Toast.LENGTH_SHORT).show();
                }
            });

            layoutControles.addView(viewControle);
        }
    }

    private void atualizarTextoVariacao(TextView txtVariacao, int valor) {
        if (valor > 0) {
            txtVariacao.setText("+" + valor);
            txtVariacao.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Verde
        } else if (valor < 0) {
            txtVariacao.setText(String.valueOf(valor));
            txtVariacao.setTextColor(android.graphics.Color.parseColor("#EF5350")); // Vermelho
        } else {
            txtVariacao.setText("0");
            txtVariacao.setTextColor(android.graphics.Color.parseColor("#333333")); // Cinza/Preto
        }
    }

    private void salvarAlteracoes() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.0.182/ControleResiduos/atualizar_quantidade.php";
        boolean teveAlteracao = false;

        for (int i = 0; i < residuo.armazenagens.size(); i++) {
            if (variacoes[i] != 0) {
                teveAlteracao = true;
                Armazenagem arm = residuo.armazenagens.get(i);

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("id_residuo", arm.id_residuo);
                    jsonBody.put("variacao", variacoes[i]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        response -> {
                            // Opcional: tratar sucesso individual
                        },
                        error -> Toast.makeText(this, "Erro ao atualizar: " + arm.tipo, Toast.LENGTH_SHORT).show()
                );
                queue.add(request);
            }
        }

        if (teveAlteracao) {
            Toast.makeText(this, "Alterações enviadas!", Toast.LENGTH_SHORT).show();
            finish(); // Fecha esta tela e volta para o Estoque
        } else {
            Toast.makeText(this, "Nenhuma alteração foi feita.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}