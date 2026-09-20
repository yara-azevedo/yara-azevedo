package com.example.mylist.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mylist.R;
import com.example.mylist.model.Conteudo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetalheActivity extends AppCompatActivity {

    private LinearLayout linear_img, linear_et, linear_status, linear_date, linear_frase;
    private ImageView poster, img_add;
    private RadioGroup type_group, status_group, formato_group;
    private RadioButton rd_filme, rd_serie, rd_livro, rd_jogo, rd_concluido, rd_largado, rd_fisico, rd_kindle;
    private EditText et_titulo, et_saga, et_comentario, et_inicio, et_conclusao, et_frase, et_vol;
    private TextView tv_salvar, tv_excluir;
    private RatingBar ratingBar;
    private RecyclerView recyclerView;
    private FraseAdapter fraseAdapter;
    private List<String> listaFrases = new ArrayList<>();

    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Void> mTakePicture;
    private Uri imageUri;
    private Bitmap bitmapFoto;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String conteudoId;
    private String posterUrlExistente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        findd();
        setupToolbar();

        conteudoId = getIntent().getStringExtra("conteudoId");
        if (conteudoId != null) {
            loadConteudo();
        }

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        bitmapFoto = null;
                        poster.setImageURI(uri);
                    }
                });

        mTakePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        bitmapFoto = bitmap;
                        imageUri = null;
                        poster.setImageBitmap(bitmap);
                    }
                });
        actionn();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void actionn() {
        poster.setOnClickListener(v -> {
            String[] options = {"Galeria", "Câmera"};
            new AlertDialog.Builder(this)
                    .setTitle("Escolher Foto")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            mGetContent.launch("image/*");
                        } else {
                            mTakePicture.launch(null);
                        }
                    }).show();
        });

        type_group.setOnCheckedChangeListener((group, checkedId) -> {
            updateVisibility(checkedId);
        });
        updateVisibility(type_group.getCheckedRadioButtonId());

        et_inicio.setOnClickListener(v -> showDatePickerDialog(et_inicio));
        et_conclusao.setOnClickListener(v -> showDatePickerDialog(et_conclusao));

        // Impedir que o teclado apareça ao clicar, forçando o uso do seletor
        et_inicio.setFocusable(false);
        et_conclusao.setFocusable(false);

        fraseAdapter = new FraseAdapter(listaFrases);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fraseAdapter);

        img_add.setOnClickListener(v -> {
            String frase = et_frase.getText().toString().trim();
            if (!frase.isEmpty()) {
                listaFrases.add(frase);
                fraseAdapter.notifyItemInserted(listaFrases.size() - 1);
                et_frase.setText("");
                recyclerView.scrollToPosition(listaFrases.size() - 1);
            } else {
                Toast.makeText(this, "Digite uma frase", Toast.LENGTH_SHORT).show();
            }
        });

        tv_salvar.setOnClickListener(v -> prepararSalvar());
        tv_excluir.setOnClickListener(v -> excluirConteudo());
    }

    private void prepararSalvar() {
        String titulo = et_titulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            Toast.makeText(this, "O título é obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bitmapFoto != null) {
            String base64 = bitmapToBase64(bitmapFoto);
            saveDataToFirestore(base64);
        } else if (imageUri != null) {
            String base64 = uriToBase64(imageUri);
            if (base64 != null) {
                saveDataToFirestore(base64);
            } else {
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveDataToFirestore(posterUrlExistente);
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Comprime a imagem para garantir que caiba no limite de 1MB do Firestore
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveDataToFirestore(String posterBase64) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        Conteudo c = new Conteudo();
        c.setUid(mAuth.getCurrentUser().getUid());
        c.setTitulo(et_titulo.getText().toString().trim());
        c.setSaga(et_saga.getText().toString().trim());
        c.setVolume(et_vol.getText().toString().trim());
        c.setComentario(et_comentario.getText().toString().trim());
        
        String dInicio = et_inicio.getText().toString().trim();
        String dConclusao = et_conclusao.getText().toString().trim();
        
        if (dInicio.isEmpty()) dInicio = getCurrentDate();
        if (dConclusao.isEmpty()) dConclusao = getCurrentDate();

        c.setDataInicio(dInicio);
        c.setDataConclusao(dConclusao);
        c.setConcluido(rd_concluido.isChecked());
        c.setLargado(rd_largado.isChecked());
        c.setNota(ratingBar.getRating());
        c.setFrases(listaFrases);
        c.setPosterUrl(posterBase64);

        int typeId = type_group.getCheckedRadioButtonId();
        if (typeId == R.id.rd_filme) c.setTipo("Filme");
        else if (typeId == R.id.rd_serie) c.setTipo("Série");
        else if (typeId == R.id.rd_livro) c.setTipo("Livro");
        else if (typeId == R.id.rd_jogo) c.setTipo("Jogo");

        if (c.getTipo() != null && c.getTipo().equals("Livro")) {
            int formatId = formato_group.getCheckedRadioButtonId();
            if (formatId == R.id.rd_fisico) c.setFormato("Físico");
            else if (formatId == R.id.rd_ebook) c.setFormato("Ebook");
        }

        if (conteudoId == null) {
            db.collection("conteudos").add(c)
                    .addOnSuccessListener(documentReference -> {
                        //Toast.makeText(DetalheActivity.this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(DetalheActivity.this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            c.setId(conteudoId);
            db.collection("conteudos").document(conteudoId).set(c)
                    .addOnSuccessListener(aVoid -> {
                        //Toast.makeText(DetalheActivity.this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(DetalheActivity.this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadConteudo() {
        db.collection("conteudos").document(conteudoId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Conteudo c = documentSnapshot.toObject(Conteudo.class);
                    if (c != null) {
                        et_titulo.setText(c.getTitulo());
                        et_saga.setText(c.getSaga());
                        et_vol.setText(c.getVolume());
                        et_comentario.setText(c.getComentario());
                        et_inicio.setText(c.getDataInicio());
                        et_conclusao.setText(c.getDataConclusao());
                        rd_concluido.setChecked(c.isConcluido());
                        rd_largado.setChecked(c.isLargado());
                        ratingBar.setRating(c.getNota());
                        listaFrases.clear();
                        if (c.getFrases() != null) listaFrases.addAll(c.getFrases());
                        fraseAdapter.notifyDataSetChanged();
                        posterUrlExistente = c.getPosterUrl();

                        if (posterUrlExistente != null && !posterUrlExistente.isEmpty()) {
                            if (posterUrlExistente.startsWith("http")) {
                                Glide.with(this).load(posterUrlExistente).into(poster);
                            } else {
                                try {
                                    byte[] decodedString = Base64.decode(posterUrlExistente, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    poster.setImageBitmap(decodedByte);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if ("Filme".equals(c.getTipo())) type_group.check(R.id.rd_filme);
                        else if ("Série".equals(c.getTipo())) type_group.check(R.id.rd_serie);
                        else if ("Livro".equals(c.getTipo())) type_group.check(R.id.rd_livro);
                        else if ("Jogo".equals(c.getTipo())) type_group.check(R.id.rd_jogo);

                        if ("Físico".equals(c.getFormato())) formato_group.check(R.id.rd_fisico);
                        else if ("Ebook".equals(c.getFormato())) formato_group.check(R.id.rd_ebook);

                        updateVisibility(type_group.getCheckedRadioButtonId());
                    }
                });
    }

    private void excluirConteudo() {
        if (conteudoId == null) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Deseja realmente excluir este item?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    db.collection("conteudos").document(conteudoId).delete()
                            .addOnSuccessListener(aVoid -> {
                                //Toast.makeText(DetalheActivity.this, "Excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(DetalheActivity.this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void showDatePickerDialog(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
                    editText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%02d/%02d/%04d",
                c.get(Calendar.DAY_OF_MONTH),
                (c.get(Calendar.MONTH) + 1),
                c.get(Calendar.YEAR));
    }

    private void updateVisibility(int checkedId) {
        int colorRes = R.color.white;
        if (checkedId == R.id.rd_filme) {
            et_saga.setVisibility(View.VISIBLE);
            et_vol.setVisibility(View.VISIBLE);
            colorRes = R.color.roxo;
        } else if (checkedId == R.id.rd_serie) {
            et_saga.setVisibility(View.VISIBLE);
            et_vol.setVisibility(View.VISIBLE);
            colorRes = R.color.verde;
        } else if (checkedId == R.id.rd_livro) {
            et_saga.setVisibility(View.VISIBLE);
            et_vol.setVisibility(View.VISIBLE);
            colorRes = R.color.azul_claro;
        } else if (checkedId == R.id.rd_jogo) {
            et_saga.setVisibility(View.GONE);
            et_vol.setVisibility(View.GONE);
            colorRes = R.color.rosa;
        } else {
            et_saga.setVisibility(View.GONE);
            et_vol.setVisibility(View.GONE);
        }

        if (checkedId == R.id.rd_livro) {
            formato_group.setVisibility(View.VISIBLE);
        } else {
            formato_group.setVisibility(View.GONE);
        }

        if (colorRes != R.color.white) {
            ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, colorRes));
            ratingBar.setProgressTintList(colorStateList);
            ratingBar.setSecondaryProgressTintList(colorStateList);
            ratingBar.setIndeterminateTintList(colorStateList);
        }
    }

    private class FraseAdapter extends RecyclerView.Adapter<FraseAdapter.FraseViewHolder> {
        private List<String> frases;

        public FraseAdapter(List<String> frases) {
            this.frases = frases;
        }

        @NonNull
        @Override
        public FraseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frase, parent, false);
            return new FraseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FraseViewHolder holder, int position) {
            String frase = frases.get(position);
            holder.tvFrase.setText(frase);

            holder.itemView.setOnLongClickListener(v -> {
                showFraseOptions(position);
                return true;
            });
        }

        private void showFraseOptions(int position) {
            String[] options = {"Editar", "Excluir"};
            new AlertDialog.Builder(DetalheActivity.this)
                    .setTitle("Opções da Frase")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showEditDialog(position);
                        } else {
                            frases.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, frases.size());
                        }
                    }).show();
        }

        private void showEditDialog(int position) {
            EditText etEdit = new EditText(DetalheActivity.this);
            etEdit.setText(frases.get(position));
            etEdit.setPadding(40, 40, 40, 40);
            etEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            new AlertDialog.Builder(DetalheActivity.this)
                    .setTitle("Editar Frase")
                    .setView(etEdit)
                    .setPositiveButton("Salvar", (dialog, which) -> {
                        String novaFrase = etEdit.getText().toString().trim();
                        if (!novaFrase.isEmpty()) {
                            frases.set(position, novaFrase);
                            notifyItemChanged(position);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        @Override
        public int getItemCount() {
            return frases.size();
        }

        class FraseViewHolder extends RecyclerView.ViewHolder {
            TextView tvFrase;

            public FraseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFrase = itemView.findViewById(R.id.tv_frase_item);
            }
        }
    }

    private void findd() {
        linear_img = findViewById(R.id.poster_linear);
        linear_et = findViewById(R.id.edit_linear);
        linear_status = findViewById(R.id.status_linear);
        linear_date = findViewById(R.id.data_linear);
        linear_frase = findViewById(R.id.linear_frase);

        poster = findViewById(R.id.img_poster);
        img_add = findViewById(R.id.img_add);

        type_group = findViewById(R.id.tipo_group);
        status_group = findViewById(R.id.status_group);
        formato_group = findViewById(R.id.format_group);

        rd_filme = findViewById(R.id.rd_filme);
        rd_serie = findViewById(R.id.rd_serie);
        rd_livro = findViewById(R.id.rd_livro);
        rd_jogo = findViewById(R.id.rd_jogo);
        rd_concluido = findViewById(R.id.rd_concluido);
        rd_largado = findViewById(R.id.rd_largado);
        rd_fisico = findViewById(R.id.rd_fisico);
        rd_kindle = findViewById(R.id.rd_ebook);

        et_titulo = findViewById(R.id.et_titulo);
        et_saga = findViewById(R.id.et_saga);
        et_comentario = findViewById(R.id.et_comentario);
        et_inicio = findViewById(R.id.et_inicio);
        et_conclusao = findViewById(R.id.et_conclusao);
        et_frase = findViewById(R.id.et_frase);
        et_vol = findViewById(R.id.et_vol);
        ratingBar = findViewById(R.id.ratingBar);
        recyclerView = findViewById(R.id.recyclerView);

        tv_salvar = findViewById(R.id.txt_salvar);
        tv_excluir = findViewById(R.id.txt_excluir);
        toolbar = findViewById(R.id.toolbar_detalhe);
    }
}
