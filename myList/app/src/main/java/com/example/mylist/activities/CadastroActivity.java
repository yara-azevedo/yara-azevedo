package com.example.mylist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mylist.R;
import com.google.firebase.auth.FirebaseAuth;

public class CadastroActivity extends AppCompatActivity {

    private EditText et_email, et_senha;
    private Button btn_cadastrar;
    private TextView tv_login;
    private ProgressBar progress_bar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        findd();
        actionn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(CadastroActivity.this, MainActivity.class));
            finish();
        }
    }

    private void actionn() {

        btn_cadastrar.setOnClickListener(v -> {
            String email = et_email.getText().toString().trim();
            String senha = et_senha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (senha.length() < 6) {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            cadastrarUsuario(email, senha);
        });

        tv_login.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void cadastrarUsuario(String email, String senha) {
        progress_bar.setVisibility(View.VISIBLE);
        btn_cadastrar.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    progress_bar.setVisibility(View.GONE);
                    btn_cadastrar.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(CadastroActivity.this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                        Toast.makeText(CadastroActivity.this, "Erro ao cadastrar: " + erro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void findd() {
        et_email = findViewById(R.id.et_email);
        et_senha = findViewById(R.id.et_senha);
        btn_cadastrar = findViewById(R.id.btn_cadastrar);
        tv_login = findViewById(R.id.tv_cadastro);
        progress_bar = findViewById(R.id.progress_bar);
    }
}