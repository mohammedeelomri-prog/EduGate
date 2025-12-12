package com.example.projet_mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEdit, emailEdit, passwordEdit;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEdit = findViewById(R.id.editName);
        emailEdit = findViewById(R.id.editEmail);
        passwordEdit = findViewById(R.id.editPassword);
        registerButton = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Rôle par défaut pour tous les utilisateurs inscrits
        String role = "etudiant";

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("role", role);

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    } else {
                        Toast.makeText(this, "Échec de l'inscription: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
