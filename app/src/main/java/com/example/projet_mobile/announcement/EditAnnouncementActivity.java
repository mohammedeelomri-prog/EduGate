package com.example.projet_mobile.announcement;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projet_mobile.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditAnnouncementActivity extends AppCompatActivity {

    private EditText etEditTitle, etEditContent;
    private Button btnUpdate, btnDelete;
    private FirebaseFirestore db;
    private String announcementId; // ID du document Firestore
    private String originalTitle;
    private String originalContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_announcement);

        db = FirebaseFirestore.getInstance();
        etEditTitle = findViewById(R.id.etEditTitle);
        etEditContent = findViewById(R.id.etEditContent);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // 1. Récupération des données passées par l'Adapter
        if (getIntent().getExtras() != null) {
            announcementId = getIntent().getStringExtra("ANNOUNCEMENT_ID");
            originalTitle = getIntent().getStringExtra("ANNOUNCEMENT_TITLE");
            originalContent = getIntent().getStringExtra("ANNOUNCEMENT_CONTENT");

            // Remplissage des champs
            etEditTitle.setText(originalTitle);
            etEditContent.setText(originalContent);
        } else {
            Toast.makeText(this, "Erreur: ID de l'annonce manquant.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Logique de Modification (Update)
        btnUpdate.setOnClickListener(v -> updateAnnouncement());

        // 3. Logique de Suppression (Delete)
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void updateAnnouncement() {
        String newTitle = etEditTitle.getText().toString().trim();
        String newContent = etEditContent.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newContent)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de la map des champs à mettre à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("content", newContent);

        db.collection("announcements").document(announcementId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Annonce modifiée avec succès!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de modification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de Suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette annonce?")
                .setPositiveButton("Oui", (dialog, which) -> deleteAnnouncement())
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteAnnouncement() {
        db.collection("announcements").document(announcementId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Annonce supprimée avec succès!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de suppression: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}