package com.example.controlecirs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnIrEstoque, btnIrAgendamentos, btnIrHistorico, btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnIrEstoque = findViewById(R.id.btnIrEstoque);
        btnIrAgendamentos = findViewById(R.id.btnIrAgendamentos);
        btnIrHistorico = findViewById(R.id.btnIrHistorico);
        btnSair = findViewById(R.id.btnSair);

        if (btnIrEstoque != null) {
            btnIrEstoque.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, EstoqueActivity.class);
                startActivity(intent);
            });
        }

        if (btnIrAgendamentos != null) {
            btnIrAgendamentos.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AgendamentoActivity.class);
                startActivity(intent);
            });
        }

        if (btnIrHistorico != null) {
            btnIrHistorico.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, HistoricoActivity.class);
                startActivity(intent);
            });
        }

        if (btnSair != null) {
            btnSair.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("AppResiduosPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}