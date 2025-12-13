package com.example.projet_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
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


        try {
            emailEdit = findViewById(R.id.editEmail);
            passwordEdit = findViewById(R.id.editPassword);
            loginButton = findViewById(R.id.btnLogin);
            registerButton = findViewById(R.id.btnRegister);
        } catch (Exception e) {
            Toast.makeText(this, "ERREUR D'INITIALISATION des vues. Vérifiez les ID XML.", Toast.LENGTH_LONG).show();

        }

        // 2. Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 3. Listeners
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> loginUser());
        }
        if (registerButton != null) {
            registerButton.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class))
            );
        }
    }

    private void loginUser() {



        String email;
        String password;


        try {
            email = emailEdit.getText().toString().trim();
            password = passwordEdit.getText().toString().trim();
        } catch (NullPointerException e) {
            Toast.makeText(this, "ERREUR CRITIQUE: editEmail ou editPassword est NULL (Vérifiez XML ID).", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }


        Toast.makeText(this, "C. Tentative de connexion Firebase...", Toast.LENGTH_SHORT).show();


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // --- POINT DE CONTRÔLE D (Succès ou Échec Firebase) ---
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // 1. Connexion réussie, maintenant lisons le rôle dans Firestore
                        db.collection("users").document(uid).get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        DocumentSnapshot doc = userTask.getResult();
                                        if (doc.exists()) {
                                            String role = doc.getString("role");

                                            // Sauvegarde du rôle (pour la persistance future)
                                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                            prefs.edit().putString("USER_ROLE", role).apply();

                                            redirectUser(role);
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Erreur: Données utilisateur (rôle) introuvables.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut(); // Déconnecter l'utilisateur si les données sont incomplètes
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Erreur lors de la lecture des données de l'utilisateur.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // Échec de l'authentification (mot de passe, e-mail non enregistré, etc.)
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue.";
                        Toast.makeText(LoginActivity.this, "D. Échec connexion: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectUser(String role) {
        Intent intent;

        switch (role) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "prof":
                intent = new Intent(this, ProfDashboardActivity.class);
                break;
            case "etudiant":
                intent = new Intent(this, StudentDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Rôle utilisateur inconnu: " + role, Toast.LENGTH_LONG).show();
                return;
        }

        // Empêche de revenir à l'écran de connexion avec le bouton Retour
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}