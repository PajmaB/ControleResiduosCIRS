package com.example.controlecirs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    // Arrays separados: um para armazenar números (+ e -) e outro para armazenar os textos de volume
    int[] variacoes;
    String[] niveisSelecionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_quantidade);
        // Ativa a seta de voltar na barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txtTituloResiduo = findViewById(R.id.txtTituloResiduo);
        layoutControles = findViewById(R.id.layoutControles);
        btnConfirmarAlteracao = findViewById(R.id.btnConfirmarAlteracao);

        residuo = (ItemResiduo) getIntent().getSerializableExtra("residuoSelecionado");

        if (residuo != null) {
            txtTituloResiduo.setText("Alterar: " + residuo.nome);
            variacoes = new int[residuo.armazenagens.size()];
            niveisSelecionados = new String[residuo.armazenagens.size()];
            montarControlesNaTela();
        }

        btnConfirmarAlteracao.setOnClickListener(v -> salvarAlteracoes());
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Destrói a tela atual e revela a tela anterior que estava embaixo
        return true;
    }

    private void montarControlesNaTela() {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < residuo.armazenagens.size(); i++) {
            Armazenagem arm = residuo.armazenagens.get(i);
            final int index = i;

            // Salva o nível inicial para sabermos se o usuário alterou algo
            niveisSelecionados[index] = arm.prioridade;

            // REGRA: Se o tipo conter a palavra "volume" (ex: Volume, Volume Combo)
            if (arm.tipo.toLowerCase().contains("volume")) {
                View viewControle = inflater.inflate(R.layout.item_controle_volume, layoutControles, false);

                TextView txtTipo = viewControle.findViewById(R.id.txtTipoAtualVolume);
                RadioGroup radioGroup = viewControle.findViewById(R.id.radioGroupVolume);
                RadioButton radioBaixo = viewControle.findViewById(R.id.radioBaixo);
                RadioButton radioMedio = viewControle.findViewById(R.id.radioMedio);
                RadioButton radioAlto = viewControle.findViewById(R.id.radioAlto);

                txtTipo.setText(arm.tipo + " (Atual: " + arm.prioridade + ")");

                // Marca a bolinha certa baseado em como o estoque está agora
                if (arm.prioridade.equals("ALTA")) radioAlto.setChecked(true);
                else if (arm.prioridade.equals("MEDIA")) radioMedio.setChecked(true);
                else radioBaixo.setChecked(true);

                // Escuta quando o usuário clica em uma bolinha diferente
                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.radioAlto) niveisSelecionados[index] = "ALTA";
                    else if (checkedId == R.id.radioMedio) niveisSelecionados[index] = "MEDIA";
                    else niveisSelecionados[index] = "BAIXA";
                });

                layoutControles.addView(viewControle);

            } else {
                // SE NÃO FOR VOLUME, MONTA O LAYOUT DE + E - (Padrão)
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
    }

    private void atualizarTextoVariacao(TextView txtVariacao, int valor) {
        if (valor > 0) {
            txtVariacao.setText("+" + valor);
            txtVariacao.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        } else if (valor < 0) {
            txtVariacao.setText(String.valueOf(valor));
            txtVariacao.setTextColor(android.graphics.Color.parseColor("#EF5350"));
        } else {
            txtVariacao.setText("0");
            txtVariacao.setTextColor(android.graphics.Color.parseColor("#333333"));
        }
    }

    private void salvarAlteracoes() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.0.182/ControleResiduos/atualizar_quantidade.php";
        boolean teveAlteracao = false;

        for (int i = 0; i < residuo.armazenagens.size(); i++) {
            Armazenagem arm = residuo.armazenagens.get(i);
            boolean isVolume = arm.tipo.toLowerCase().contains("volume");
            boolean vaiAtualizar = false;

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("id_residuo", arm.id_residuo);

                if (isVolume) {
                    // Verifica se a bolinha selecionada é diferente da original
                    if (!niveisSelecionados[i].equals(arm.prioridade)) {
                        vaiAtualizar = true;
                        jsonBody.put("modo", "volume");
                        jsonBody.put("nivel", niveisSelecionados[i]);
                    }
                } else {
                    // Lógica original de variação numérica
                    if (variacoes[i] != 0) {
                        vaiAtualizar = true;
                        jsonBody.put("variacao", variacoes[i]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (vaiAtualizar) {
                teveAlteracao = true;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        response -> { },
                        error -> Toast.makeText(this, "Erro ao atualizar: " + arm.tipo, Toast.LENGTH_SHORT).show()
                );
                queue.add(request);
            }
        }

        if (teveAlteracao) {
            Toast.makeText(this, "Alterações enviadas!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Nenhuma alteração foi feita.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}