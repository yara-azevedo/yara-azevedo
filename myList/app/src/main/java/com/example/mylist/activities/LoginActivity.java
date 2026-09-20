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

public class LoginActivity extends AppCompatActivity {

    private EditText et_email, et_senha;
    private Button btn_logar;
    private TextView tv_cadastro, tv_esqueci_senha;
    private ProgressBar progress_bar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void actionn() {

        btn_logar.setOnClickListener(v -> {
            String email = et_email.getText().toString().trim();
            String senha = et_senha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            logarUsuario(email, senha);
        });

        tv_cadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        tv_esqueci_senha.setOnClickListener(v -> {
            String email = et_email.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Informe seu e-mail para recuperar a senha", Toast.LENGTH_SHORT).show();
            } else {
                recuperarSenha(email);
            }
        });
    }

    private void recuperarSenha(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "E-mail de recuperação enviado para: " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                        Toast.makeText(LoginActivity.this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logarUsuario(String email, String senha) {
        progress_bar.setVisibility(View.VISIBLE);
        btn_logar.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    progress_bar.setVisibility(View.GONE);
                    btn_logar.setEnabled(true);

                    if (task.isSuccessful()) {
                        //Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                        Toast.makeText(LoginActivity.this, "Erro ao logar: " + erro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void findd() {
        et_email = findViewById(R.id.et_email);
        et_senha = findViewById(R.id.et_senha);
        btn_logar = findViewById(R.id.btn_logar);
        tv_cadastro = findViewById(R.id.tv_cadastro);
        tv_esqueci_senha = findViewById(R.id.tv_esqueci_senha);
        progress_bar = findViewById(R.id.progress_bar);
    }
}