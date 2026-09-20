package com.example.monstrin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fob;
    private MonsterAdapter adapter;
    private List<Monster> monsterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findd();
        setupRecyclerView();
        actionn();
    }

    private void setupRecyclerView() {
        monsterList = new ArrayList<>();
        adapter = new MonsterAdapter(monsterList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMonsters();
    }

    private void fetchMonsters() {
        FirebaseFirestore.getInstance().collection("monstros")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    monsterList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Monster monster = document.toObject(Monster.class);
                        monster.setId(document.getId());
                        
                        // Fallback para itens antigos sem timestamp
                        if (monster.getTimestamp() == null) {
                            monster.setTimestamp(0L);
                        }
                        
                        monsterList.add(monster);
                    }
                    
                    // Ordenar manualmente: maior timestamp (mais recente) primeiro
                    monsterList.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                    
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar itens", Toast.LENGTH_SHORT).show();
                });
    }

    private void actionn() {

        fob.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContentActivity.class);
            startActivity(intent);
        });
    }

    private void findd() {
        recyclerView = findViewById(R.id.recycler_view);
        fob = findViewById(R.id.btn_add);
    }
}