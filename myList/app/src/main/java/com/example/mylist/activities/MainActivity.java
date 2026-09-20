package com.example.mylist.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mylist.R;
import com.example.mylist.model.Conteudo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageButton btn_search, btn_filter, btn_close;
    private EditText et_pesquisa;
    private TextView tv_filme_count, tv_serie_count, tv_livro_count,tv_jogo_count, tv_titulo_app;
    private RecyclerView recyclerView;
    private FloatingActionButton fob;
    private View titleBar, searchBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Conteudo> listaConteudo = new ArrayList<>();
    private List<Conteudo> listaFiltrada = new ArrayList<>();
    private ConteudoAdapter adapter;

    // Estados do Filtro
    private String filtroTipo = "Todos";
    private float filtroNota = 0;
    private String filtroAno = "";

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        findd();
        setupRecycler();
        actionn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupRecycler() {
        adapter = new ConteudoAdapter(listaFiltrada);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        if (mAuth.getCurrentUser() == null) return;

        db.collection("conteudos")
                .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaConteudo.clear();
                    int filmes = 0, series = 0, livros = 0, jogos = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Conteudo c = doc.toObject(Conteudo.class);
                        c.setId(doc.getId());
                        listaConteudo.add(c);

                        if ("Filme".equals(c.getTipo())) filmes++;
                        else if ("Série".equals(c.getTipo())) series++;
                        else if ("Livro".equals(c.getTipo())) livros++;
                        else if ("Jogo".equals(c.getTipo())) jogos++;
                    }

                    tv_filme_count.setText(String.valueOf(filmes));
                    tv_filme_count.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.roxo));

                    tv_serie_count.setText(String.valueOf(series));
                    tv_serie_count.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.verde));

                    tv_livro_count.setText(String.valueOf(livros));
                    tv_livro_count.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.azul_claro));

                    tv_jogo_count.setText(String.valueOf(jogos));
                    tv_jogo_count.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.rosa));

                    // Ordenar: mais recentes primeiro
                    Collections.sort(listaConteudo, (o1, o2) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date d1 = sdf.parse(o1.getDataConclusao());
                            Date d2 = sdf.parse(o2.getDataConclusao());
                            if (d1 != null && d2 != null) {
                                return d2.compareTo(d1);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    });

                    aplicarFiltros(et_pesquisa.getText().toString());
                });
    }

    private void aplicarFiltros(String queryNome) {
        listaFiltrada.clear();
        String query = normalize(queryNome.toLowerCase());

        for (Conteudo c : listaConteudo) {
            boolean matchesNome = true;
            boolean matchesTipo = true;
            boolean matchesNota = true;
            boolean matchesAno = true;

            // Filtro Nome
            if (!query.isEmpty()) {
                String titulo = normalize(c.getTitulo().toLowerCase());
                matchesNome = titulo.contains(query);
            }

            // Filtro Tipo
            if (!filtroTipo.equals("Todos")) {
                matchesTipo = filtroTipo.equals(c.getTipo());
            }

            // Filtro Nota (Mínima)
            if (filtroNota > 0) {
                matchesNota = c.getNota() >= filtroNota;
            }

            // Filtro Ano (Contido na data de conclusão)
            if (!filtroAno.isEmpty()) {
                String data = c.getDataConclusao();
                matchesAno = data != null && data.contains(filtroAno);
            }

            if (matchesNome && matchesTipo && matchesNota && matchesAno) {
                listaFiltrada.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void pesquisar(String texto) {
        aplicarFiltros(texto);
    }

    private String normalize(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    private class ConteudoAdapter extends RecyclerView.Adapter<ConteudoAdapter.ViewHolder> {
        private List<Conteudo> itens;

        public ConteudoAdapter(List<Conteudo> itens) {
            this.itens = itens;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conteudo_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Conteudo item = itens.get(position);
            holder.tvNome.setText(item.getTitulo());
            
            String saga = item.getSaga();
            String vol = item.getVolume();
            StringBuilder sb = new StringBuilder();
            if (saga != null && !saga.isEmpty()) sb.append(saga);
            if (vol != null && !vol.isEmpty()) {
                if (sb.length() > 0) sb.append(" - ");
                sb.append(vol);
            }
            
            if (sb.length() == 0) {
                holder.tvSaga.setVisibility(View.GONE);
            } else {
                holder.tvSaga.setVisibility(View.VISIBLE);
                holder.tvSaga.setText(sb.toString());
            }
            
            holder.tvNota.setText(String.format("%.1f", item.getNota()));
            
            String termino = item.getDataConclusao();
            if (termino == null || termino.isEmpty()) {
                holder.tvTermino.setVisibility(View.GONE);
            } else {
                holder.tvTermino.setVisibility(View.VISIBLE);
                holder.tvTermino.setText(termino);
            }

            // Aplicar cores baseadas no tipo
            int colorRes = R.color.white;
            if ("Filme".equals(item.getTipo())) colorRes = R.color.roxo;
            else if ("Série".equals(item.getTipo())) colorRes = R.color.verde;
            else if ("Livro".equals(item.getTipo())) colorRes = R.color.azul_claro;
            else if ("Jogo".equals(item.getTipo())) colorRes = R.color.rosa;
            
            int color = ContextCompat.getColor(MainActivity.this, colorRes);
            int bgColor = ContextCompat.getColor(MainActivity.this, R.color.azul_escuro);
            int transparentBg = ColorUtils.setAlphaComponent(bgColor, 128); // 50% de transparência no azul escuro
            int white = ContextCompat.getColor(MainActivity.this, R.color.white);
            
            holder.tvTermino.setTextColor(white);
            holder.tvTermino.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(MainActivity.this, R.color.black), 128));
            
            holder.tvNota.setTextColor(white);
            holder.tvNota.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(MainActivity.this, R.color.black), 128));
            
            holder.tvNome.setTextColor(color);
            holder.tvSaga.setTextColor(color);
            holder.tvNome.setBackgroundColor(transparentBg);
            holder.tvSaga.setBackgroundColor(transparentBg);

            if (item.getPosterUrl() != null && !item.getPosterUrl().isEmpty()) {
                holder.ivPoster.setScaleType(ImageView.ScaleType.FIT_XY);
                if (item.getPosterUrl().startsWith("http")) {
                    Glide.with(MainActivity.this).load(item.getPosterUrl()).into(holder.ivPoster);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(item.getPosterUrl(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.ivPoster.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        e.printStackTrace();
                        holder.ivPoster.setImageResource(R.drawable.icon);
                        holder.ivPoster.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                }
            } else {
                holder.ivPoster.setImageResource(R.drawable.icon);
                holder.ivPoster.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DetalheActivity.class);
                intent.putExtra("conteudoId", item.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return itens.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPoster;
            TextView tvNome, tvSaga, tvNota, tvTermino;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPoster = itemView.findViewById(R.id.imageView3);
                tvNome = itemView.findViewById(R.id.textView13);
                tvSaga = itemView.findViewById(R.id.textView6);
                tvNota = itemView.findViewById(R.id.textView15);
                tvTermino = itemView.findViewById(R.id.textView5);
            }
        }
    }

    private void actionn() {
        fob.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DetalheActivity.class);
            startActivity(intent);
        });

        tv_titulo_app.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logoff realizado", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btn_search.setOnClickListener(v -> {
            titleBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            et_pesquisa.requestFocus();
        });

        btn_close.setOnClickListener(v -> {
            et_pesquisa.setText("");
            searchBar.setVisibility(View.GONE);
            titleBar.setVisibility(View.VISIBLE);
        });

        et_pesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pesquisar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btn_filter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filtro, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        RadioGroup rg1 = view.findViewById(R.id.rg_tipo_filtro);
        RadioGroup rg2 = view.findViewById(R.id.rg_tipo_filtro_2);
        RatingBar rbNota = view.findViewById(R.id.rb_nota_filtro);
        EditText etAno = view.findViewById(R.id.et_ano_filtro);
        Button btnLimpar = view.findViewById(R.id.btn_limpar_filtro);
        Button btnAplicar = view.findViewById(R.id.btn_aplicar_filtro);

        // Setar valores atuais
        if (filtroTipo.equals("Todos")) rg1.check(R.id.rb_todos);
        else if (filtroTipo.equals("Filme")) rg1.check(R.id.rb_filme);
        else if (filtroTipo.equals("Série")) rg1.check(R.id.rb_serie);
        else if (filtroTipo.equals("Livro")) rg2.check(R.id.rb_livro);
        else if (filtroTipo.equals("Jogo")) rg2.check(R.id.rb_jogo);

        rbNota.setRating(filtroNota);
        etAno.setText(filtroAno);

        // Lógica de desmarcar o outro RadioGroup ao selecionar um
        rg1.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) rg2.clearCheck();
        });
        rg2.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) rg1.clearCheck();
        });

        btnLimpar.setOnClickListener(v -> {
            filtroTipo = "Todos";
            filtroNota = 0;
            filtroAno = "";
            aplicarFiltros(et_pesquisa.getText().toString());
            dialog.dismiss();
        });

        btnAplicar.setOnClickListener(v -> {
            int id1 = rg1.getCheckedRadioButtonId();
            int id2 = rg2.getCheckedRadioButtonId();

            if (id1 == R.id.rb_todos) filtroTipo = "Todos";
            else if (id1 == R.id.rb_filme) filtroTipo = "Filme";
            else if (id1 == R.id.rb_serie) filtroTipo = "Série";
            else if (id2 == R.id.rb_livro) filtroTipo = "Livro";
            else if (id2 == R.id.rb_jogo) filtroTipo = "Jogo";

            filtroNota = rbNota.getRating();
            filtroAno = etAno.getText().toString().trim();

            aplicarFiltros(et_pesquisa.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void findd() {
        btn_search = findViewById(R.id.btn_search);
        btn_filter = findViewById(R.id.btn_filter);
        btn_close = findViewById(R.id.btn_close);

        et_pesquisa = findViewById(R.id.et_pesquisa);
        tv_filme_count = findViewById(R.id.txt_filme_count);
        tv_serie_count = findViewById(R.id.txt_serie_count);
        tv_livro_count = findViewById(R.id.txt_livro_count);
        tv_jogo_count = findViewById(R.id.txt_jogo_count);
        tv_titulo_app = findViewById(R.id.textView7);
        titleBar = findViewById(R.id.title_bar);
        searchBar = findViewById(R.id.search_bar_layout);
        recyclerView = findViewById(R.id.recycler_view);
        fob = findViewById(R.id.btn_add);
    }
}