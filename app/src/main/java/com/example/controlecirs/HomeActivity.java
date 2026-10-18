package com.example.controlecirs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnIrEstoque, btnIrAgendamentos, btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnIrEstoque = findViewById(R.id.btnIrEstoque);
        btnIrAgendamentos = findViewById(R.id.btnIrAgendamentos);
        btnSair = findViewById(R.id.btnSair);

        // Botão que abre a tela de Estoque
        btnIrEstoque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EstoqueActivity.class);
                startActivity(intent);
            }
        });

        // Botão que abre a tela de Agendamentos
        btnIrAgendamentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AgendamentoActivity.class);
                startActivity(intent);
            }
        });

        // Botão para sair da conta (Limpa o SharedPreferences e volta pro Login)
        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("AppResiduosPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // Apaga o ID_USUARIO salvo
                editor.apply();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Destrói a HomeActivity para não voltar a ela
            }
        });
    }
}