package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEdit, passwordEdit;
    private Button loginButton, registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEdit = findViewById(R.id.editEmail);
        passwordEdit = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        DocumentSnapshot doc = userTask.getResult();
                                        if (doc.exists()) {
                                            String role = doc.getString("role");
                                            redirectUser(role);
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Échec de la connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectUser(String role) {
        switch (role) {
            case "admin":
                startActivity(new Intent(this, AdminDashboardActivity.class));
                break;
            case "prof":
                startActivity(new Intent(this, ProfDashboardActivity.class));
                break;
            case "etudiant":
                startActivity(new Intent(this, StudentDashboardActivity.class));
                break;
            default:
                Toast.makeText(this, "Rôle inconnu", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
