package com.example.controlecirs;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText editLogin, editSenha;
    Button btnLogin;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicia o SharedPreferences (O cofre)
        sharedPreferences = getSharedPreferences("AppResiduosPrefs", MODE_PRIVATE);

        // Verifica se já existe um ID salvo. Se existir, pula direto pro app!
        if (sharedPreferences.contains("ID_USUARIO")) {
            abrirTelaPrincipal();
            return;
        }

        editLogin = findViewById(R.id.editLogin);
        editSenha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerLogin();
            }
        });
    }

    private void fazerLogin() {
        String login = editLogin.getText().toString();
        String senha = editSenha.getText().toString();

        if (login.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.0.182/ControleResiduos/login.php";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("login", login);
            jsonBody.put("senha", senha);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("sucesso")) {
                            int idUsuario = response.getInt("id_usuario");

                            // Salva o ID no cofre para não precisar logar de novo
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("ID_USUARIO", idUsuario);
                            editor.apply();

                            Toast.makeText(MainActivity.this, "Login efetuado!", Toast.LENGTH_SHORT).show();
                            abrirTelaPrincipal();
                        } else {
                            String msg = response.getString("mensagem");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Erro de rede: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Fecha a tela de login para não voltar nela pelo botão "Voltar" do celular
    }
}