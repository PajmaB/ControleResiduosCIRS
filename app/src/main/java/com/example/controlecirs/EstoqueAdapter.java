package com.example.controlecirs;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EstoqueAdapter extends RecyclerView.Adapter<EstoqueAdapter.ViewHolder> {

    private List<ItemResiduo> listaResiduos;
    private Context context;

    public EstoqueAdapter(List<ItemResiduo> listaResiduos, Context context) {
        this.listaResiduos = listaResiduos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_residuo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemResiduo item = listaResiduos.get(position);
        holder.txtNomeResiduo.setText(item.nome);

        // Limpa as caixinhas antigas (necessário pois o Android recicla as linhas invisíveis)
        holder.layoutArmazenagens.removeAllViews();

        // Cria uma caixinha colorida para cada tipo de armazenagem
        for (Armazenagem arm : item.armazenagens) {
            TextView tvCaixa = new TextView(context);
            tvCaixa.setText(arm.tipo + ": " + arm.quantidade);
            tvCaixa.setTextColor(Color.BLACK);
            tvCaixa.setPadding(24, 16, 24, 16);
            tvCaixa.setTextSize(14f);

            // Configura as margens entre as caixinhas
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            tvCaixa.setLayoutParams(params);

            // Define a cor de fundo com base na prioridade (usando tons pastéis para facilitar a leitura)
            if (arm.prioridade.equals("ALTA")) {
                tvCaixa.setBackgroundColor(Color.parseColor("#EF9A9A")); // Vermelho claro
            } else if (arm.prioridade.equals("MEDIA")) {
                tvCaixa.setBackgroundColor(Color.parseColor("#FFF59D")); // Amarelo claro
            } else {
                tvCaixa.setBackgroundColor(Color.parseColor("#A5D6A7")); // Verde claro
            }

            holder.layoutArmazenagens.addView(tvCaixa);
        }

        // Evento de clique na linha inteira
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, AlterarQuantidadeActivity.class);
            // Passa o objeto inteiro (com todos os Fardos, Racks, etc) para a próxima tela
            intent.putExtra("residuoSelecionado", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaResiduos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeResiduo;
        LinearLayout layoutArmazenagens;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeResiduo = itemView.findViewById(R.id.txtNomeResiduo);
            layoutArmazenagens = itemView.findViewById(R.id.layoutArmazenagens);
        }
    }
}