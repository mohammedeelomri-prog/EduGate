package com.example.projet_mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_mobile.models.DocumentRequest;
import com.example.projet_mobile.utils.NotificationHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DocumentRequestActivity extends AppCompatActivity {

    private TextInputLayout documentTypeLayout;
    private AutoCompleteTextView documentTypeSpinner;
    private TextInputLayout commentLayout;
    private TextInputEditText commentEditText;
    private Button btnSubmitRequest, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String[] documentTypes = {
            "Certificat de scolarité",
            "Attestation de réussite",
            "Relevé de notes",
            "Attestation d'inscription",
            "Certificat de stage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_request);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        documentTypeLayout = findViewById(R.id.documentTypeLayout);
        documentTypeSpinner = findViewById(R.id.documentTypeSpinner);
        commentLayout = findViewById(R.id.commentLayout);
        commentEditText = findViewById(R.id.commentEditText);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        btnCancel = findViewById(R.id.btnCancel);

        // Configuration du dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                documentTypes
        );
        documentTypeSpinner.setAdapter(adapter);

        // Listeners
        btnSubmitRequest.setOnClickListener(v -> submitRequest());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitRequest() {
        String documentType = documentTypeSpinner.getText().toString().trim();
        String comment = commentEditText.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(documentType)) {
            documentTypeLayout.setError("Veuillez sélectionner un type de document");
            return;
        } else {
            documentTypeLayout.setError(null);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pendant le traitement
        btnSubmitRequest.setEnabled(false);

        // Créer l'objet demande
        DocumentRequest request = new DocumentRequest(
                currentUser.getUid(),
                currentUser.getEmail(),
                documentType,
                comment
        );

        // Enregistrer dans Firestore
        db.collection("document_requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    // Notification de succès avec Snackbar
                    NotificationHelper.showToast(
                            DocumentRequestActivity.this,
                            "✓ Demande de " + documentType + " soumise avec succès",
                            NotificationHelper.NotificationType.SUCCESS
                    );
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Notification d'erreur
                    NotificationHelper.showToast(
                            DocumentRequestActivity.this,
                            "Erreur lors de la soumission: " + e.getMessage(),
                            NotificationHelper.NotificationType.ERROR
                    );
                    btnSubmitRequest.setEnabled(true);
                });
    }
}
