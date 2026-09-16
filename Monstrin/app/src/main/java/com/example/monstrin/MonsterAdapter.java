package com.example.monstrin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonsterAdapter extends RecyclerView.Adapter<MonsterAdapter.MonsterViewHolder> {

    private List<Monster> monsterList;
    private Context context;

    public MonsterAdapter(List<Monster> monsterList, Context context) {
        this.monsterList = monsterList;
        this.context = context;
    }

    @NonNull
    @Override
    public MonsterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new MonsterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonsterViewHolder holder, int position) {
        Monster monster = monsterList.get(position);
        holder.tvNome.setText(monster.getNome());
        holder.tvOta.setText("Potacia: " + monster.getOta_value());
        holder.tvYra.setText("Yrara: " + monster.getYra_value());

        if (monster.getImage_base64() != null && !monster.getImage_base64().isEmpty()) {
            byte[] decodedString = Base64.decode(monster.getImage_base64(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imgMonster.setImageBitmap(decodedByte);
        } else {
            holder.imgMonster.setImageResource(R.drawable.quadrado);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContentActivity.class);
            intent.putExtra("id", monster.getId());
            // Optionally pass other data to avoid refetching if needed, but ID is enough for exclusion/update
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return monsterList.size();
    }

    public static class MonsterViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMonster;
        TextView tvNome, tvOta, tvYra;

        public MonsterViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMonster = itemView.findViewById(R.id.imageView3);
            tvNome = itemView.findViewById(R.id.textView13);
            tvOta = itemView.findViewById(R.id.textView14);
            tvYra = itemView.findViewById(R.id.textView15);
        }
    }
}
