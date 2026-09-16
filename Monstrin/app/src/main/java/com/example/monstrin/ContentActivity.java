package com.example.monstrin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ContentActivity extends AppCompatActivity {
    private ImageView imageView;
    private EditText editText;
    private TextView t01, t02, t03, t04, t05, t06, t07, t08, t09, t00;
    private TextView t11, t12, t13, t14, t15, t16, t17, t18, t19, t10;
    private TextView txt_salvar, txt_excluir;

    private String ota_value = "", yra_value = "";
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;
    private Uri imageUri;
    private Bitmap imageBitmap;
    private String documentId = null;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_content);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        documentId = getIntent().getStringExtra("id");
        
        setupPermissionLauncher();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(documentId == null ? "Novo Monstrin" : "Editar Monstrin");
        }

        setupImageLaunchers();
        findd();
        actionn();

        if (documentId != null) {
            carregarDados();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void carregarDados() {
        FirebaseFirestore.getInstance().collection("monstros")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Monster monster = documentSnapshot.toObject(Monster.class);
                        if (monster != null) {
                            editText.setText(monster.getNome());
                            ota_value = monster.getOta_value();
                            yra_value = monster.getYra_value();

                            // Atualizar seleção visual
                            updateVisualSelection(ota_value, yra_value);

                            if (monster.getImage_base64() != null) {
                                byte[] decodedString = Base64.decode(monster.getImage_base64(), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imageView.setImageBitmap(decodedByte);
                            }
                        }
                    }
                });
    }

    private void updateVisualSelection(String ota, String yra) {
        TextView[] otaGroup = {t01, t02, t03, t04, t05, t06, t07, t08, t09, t00};
        TextView[] yraGroup = {t11, t12, t13, t14, t15, t16, t17, t18, t19, t10};

        for (TextView t : otaGroup) {
            if (t.getText().toString().equals(ota)) {
                updateSelection(otaGroup, t, Color.parseColor("#8BB63B"));
            }
        }
        for (TextView t : yraGroup) {
            if (t.getText().toString().equals(yra)) {
                updateSelection(yraGroup, t, Color.parseColor("#75BABE"));
            }
        }
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        try {
                            cameraLauncher.launch(null);
                        } catch (Exception e) {
                            Toast.makeText(this, "Erro ao abrir a câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupImageLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imageBitmap = null;
                        imageView.setImageURI(uri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        imageBitmap = bitmap;
                        imageUri = null;
                        imageView.setImageBitmap(bitmap);
                    }
                }
        );
    }
    
    private void actionn() {
        TextView[] otaGroup = {t01, t02, t03, t04, t05, t06, t07, t08, t09, t00};
        TextView[] yraGroup = {t11, t12, t13, t14, t15, t16, t17, t18, t19, t10};

        for (TextView t : otaGroup) {
            t.setOnClickListener(v -> {
                ota_value = t.getText().toString();
                updateSelection(otaGroup, t, Color.parseColor("#8BB63B"));
            });
        }

        for (TextView t : yraGroup) {
            t.setOnClickListener(v -> {
                yra_value = t.getText().toString();
                updateSelection(yraGroup, t, Color.parseColor("#75BABE"));
            });
        }

        txt_salvar.setOnClickListener(v -> {
            salvarNoFirebase();
        });

        txt_excluir.setOnClickListener(v -> {
            excluirDoFirebase();
        });

        imageView.setOnClickListener(v -> showImageOptions());

    }

    private void excluirDoFirebase() {
        if (documentId == null) {
            // Se for um novo registro, apenas fecha a tela
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Tem certeza que deseja excluir este item?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("monstros")
                            .document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void showImageOptions() {
        String[] options = {"Câmera", "Galeria"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecionar Imagem");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraLauncher.launch(null);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao abrir a câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else {
                galleryLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void salvarNoFirebase() {
        String nome = editText.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(this, "Preencha o nome", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Image = (imageUri != null) ? uriToBase64(imageUri) : 
                          (imageBitmap != null) ? bitmapToBase64(imageBitmap) : null;

        saveDataToFirestore(base64Image);
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmapToBase64(bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Comprimindo agressivamente para ficar abaixo de 1MB
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void saveDataToFirestore(String base64Image) {
        Map<String, Object> data = new HashMap<>();
        data.put("nome", editText.getText().toString());
        data.put("ota_value", ota_value);
        data.put("yra_value", yra_value);
        if (base64Image != null) {
            data.put("image_base64", base64Image);
        }

        if (documentId == null) {
            FirebaseFirestore.getInstance().collection("monstros")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            FirebaseFirestore.getInstance().collection("monstros")
                    .document(documentId)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateSelection(TextView[] group, TextView selected, int color) {
        for (TextView t : group) {
            if (t == selected) {
                t.setBackgroundColor(color);
                t.setTextColor(Color.WHITE);
            } else {
                t.setBackgroundColor(Color.TRANSPARENT);
                t.setTextColor(Color.BLACK);
            }
        }
    }

    private void findd() {
        t01 = findViewById(R.id.txt01);
        t02 = findViewById(R.id.txt02);
        t03 = findViewById(R.id.txt03);
        t04 = findViewById(R.id.txt04);
        t05 = findViewById(R.id.txt05);
        t06 = findViewById(R.id.txt06);
        t07 = findViewById(R.id.txt07);
        t08 = findViewById(R.id.txt08);
        t09 = findViewById(R.id.txt09);
        t00 = findViewById(R.id.txt00);

        t11 = findViewById(R.id.txt11);
        t12 = findViewById(R.id.txt12);
        t13 = findViewById(R.id.txt13);
        t14 = findViewById(R.id.txt14);
        t15 = findViewById(R.id.txt15);
        t16 = findViewById(R.id.txt16);
        t17 = findViewById(R.id.txt17);
        t18 = findViewById(R.id.txt18);
        t19 = findViewById(R.id.txt19);
        t10 = findViewById(R.id.txt10);

        txt_salvar = findViewById(R.id.txt_salvar);
        txt_excluir = findViewById(R.id.txt_excluir);

        editText = findViewById(R.id.et_nome);
        imageView = findViewById(R.id.item_img);
    }
}