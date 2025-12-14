package com.example.projet_mobile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_mobile.utils.NotificationHelper;
import com.example.projet_mobile.utils.PdfUploader;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvDetailStudentName, tvDetailDocumentType, tvDetailRequestDate;
    private TextView tvDetailStatus, tvDetailComment, tvCommentLabel;
    private Button btnClose, btnValidate, btnMarkReady, btnReject, btnAddPdfUrl;
    
    private FirebaseFirestore db;
    private String requestId;
    private String currentStatus;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
    
    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private AlertDialog uploadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        db = FirebaseFirestore.getInstance();

        tvDetailStudentName = findViewById(R.id.tvDetailStudentName);
        tvDetailDocumentType = findViewById(R.id.tvDetailDocumentType);
        tvDetailRequestDate = findViewById(R.id.tvDetailRequestDate);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailComment = findViewById(R.id.tvDetailComment);
        tvCommentLabel = findViewById(R.id.tvCommentLabel);
        btnClose = findViewById(R.id.btnClose);
        btnValidate = findViewById(R.id.btnValidate);
        btnMarkReady = findViewById(R.id.btnMarkReady);
        btnReject = findViewById(R.id.btnReject);
        btnAddPdfUrl = findViewById(R.id.btnAddPdfUrl);

        requestId = getIntent().getStringExtra("requestId");
        currentStatus = getIntent().getStringExtra("status");

        setupPdfPicker();
        loadRequestDetails();
        setupActionButtons();

        btnClose.setOnClickListener(v -> finish());
        btnValidate.setOnClickListener(v -> updateStatus("approuvee", "Demande validée"));
        btnAddPdfUrl.setOnClickListener(v -> showUploadPdfDialog());
        btnMarkReady.setOnClickListener(v -> showMarkReadyDialog());
        btnReject.setOnClickListener(v -> showRejectDialog());
    }

    private void setupPdfPicker() {
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri pdfUri = result.getData().getData();
                        if (pdfUri != null) {
                            uploadPdfToStorage(pdfUri);
                        }
                    }
                });
    }

    private void loadRequestDetails() {
        String studentName = getIntent().getStringExtra("studentName");
        String documentType = getIntent().getStringExtra("documentType");
        String status = getIntent().getStringExtra("status");
        String comment = getIntent().getStringExtra("comment");
        long createdAtMillis = getIntent().getLongExtra("createdAt", 0);

        tvDetailStudentName.setText(studentName != null ? studentName : "N/A");
        tvDetailDocumentType.setText(documentType != null ? documentType : "N/A");
        
        if (createdAtMillis > 0) {
            tvDetailRequestDate.setText(dateFormat.format(new Date(createdAtMillis)));
        } else {
            tvDetailRequestDate.setText("N/A");
        }

        String statusText = getStatusText(status);
        tvDetailStatus.setText(statusText);
        
        GradientDrawable background = (GradientDrawable) tvDetailStatus.getBackground();
        background.setColor(getStatusColor(status));

        if (comment != null && !comment.isEmpty()) {
            tvCommentLabel.setVisibility(View.VISIBLE);
            tvDetailComment.setVisibility(View.VISIBLE);
            tvDetailComment.setText(comment);
        } else {
            tvCommentLabel.setVisibility(View.GONE);
            tvDetailComment.setVisibility(View.GONE);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "en_attente":
                return "En attente";
            case "approuvee":
                return "Validé";
            case "pret":
                return "Prêt";
            case "rejetee":
                return "Rejeté";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "en_attente":
                return Color.parseColor("#FF9800");
            case "approuvee":
                return Color.parseColor("#4CAF50");
            case "pret":
                return Color.parseColor("#2196F3");
            case "rejetee":
                return Color.parseColor("#F44336");
            default:
                return Color.parseColor("#9E9E9E");
        }
    }

    private void setupActionButtons() {
        // Afficher les boutons selon le statut actuel
        switch (currentStatus) {
            case "en_attente":
                btnValidate.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnAddPdfUrl.setVisibility(View.GONE);
                btnMarkReady.setVisibility(View.GONE);
                break;
            case "approuvee":
                btnValidate.setVisibility(View.GONE);
                btnAddPdfUrl.setVisibility(View.VISIBLE);
                btnMarkReady.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                break;
            case "pret":
            case "rejetee":
                btnValidate.setVisibility(View.GONE);
                btnAddPdfUrl.setVisibility(View.GONE);
                btnMarkReady.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                break;
        }
    }

    private void updateStatus(String newStatus, String successMessage) {
        if (requestId == null) {
            NotificationHelper.showToast(this, "Erreur: ID de demande manquant", 
                    NotificationHelper.NotificationType.ERROR);
            return;
        }

        db.collection("document_requests")
                .document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Notification de succès avec icône
                    NotificationHelper.showToast(this, successMessage, 
                            NotificationHelper.NotificationType.SUCCESS);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Notification d'erreur
                    NotificationHelper.showToast(this, "Erreur: " + e.getMessage(), 
                            NotificationHelper.NotificationType.ERROR);
                });
    }

    private void showMarkReadyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Marquer comme prêt")
                .setMessage("Avez-vous uploadé le PDF dans Firebase Storage et ajouté l'URL dans le champ 'pdfUrl' ?")
                .setPositiveButton("Oui, marquer comme prêt", (dialog, which) -> {
                    updateStatus("pret", "Document marqué comme prêt");
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showRejectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Rejeter la demande")
                .setMessage("Êtes-vous sûr de vouloir rejeter cette demande ?")
                .setPositiveButton("Rejeter", (dialog, which) -> {
                    updateStatus("rejetee", "Demande rejetée");
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAddPdfUrlDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_pdf_url, null);
        com.google.android.material.textfield.TextInputEditText etPdfUrl = 
                dialogView.findViewById(R.id.etPdfUrl);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String pdfUrl = etPdfUrl.getText().toString().trim();
                    if (!pdfUrl.isEmpty()) {
                        updatePdfUrl(pdfUrl);
                    } else {
                        Toast.makeText(this, "Veuillez entrer une URL", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updatePdfUrl(String pdfUrl) {
        if (requestId == null) {
            NotificationHelper.showToast(this, "Erreur: ID de demande manquant", 
                    NotificationHelper.NotificationType.ERROR);
            return;
        }

        db.collection("document_requests")
                .document(requestId)
                .update("pdfUrl", pdfUrl)
                .addOnSuccessListener(aVoid -> {
                    NotificationHelper.showToast(this, "✓ PDF ajouté avec succès", 
                            NotificationHelper.NotificationType.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUploadPdfDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_pdf, null);
        TextView tvSelectedFile = dialogView.findViewById(R.id.tvSelectedFile);
        Button btnSelectPdf = dialogView.findViewById(R.id.btnSelectPdf);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        TextView tvUploadProgress = dialogView.findViewById(R.id.tvUploadProgress);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setNegativeButton("Fermer", null);
        uploadDialog = builder.create();

        btnSelectPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pdfPickerLauncher.launch(Intent.createChooser(intent, "Sélectionner un PDF"));
        });

        uploadDialog.show();
    }

    private void uploadPdfToStorage(Uri pdfUri) {
        if (uploadDialog == null) return;

        View dialogView = uploadDialog.findViewById(android.R.id.content);
        if (dialogView == null) return;

        TextView tvSelectedFile = dialogView.findViewById(R.id.tvSelectedFile);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        TextView tvUploadProgress = dialogView.findViewById(R.id.tvUploadProgress);
        Button btnSelectPdf = dialogView.findViewById(R.id.btnSelectPdf);

        // Afficher le nom du fichier
        String fileName = getFileName(pdfUri);
        tvSelectedFile.setText("📄 " + fileName);

        // Désactiver le bouton pendant l'upload
        btnSelectPdf.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvUploadProgress.setVisibility(View.VISIBLE);

        PdfUploader.uploadPdf(this, pdfUri, requestId, new PdfUploader.UploadCallback() {
            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                    tvUploadProgress.setText("Upload en cours... " + progress + "%");
                });
            }

            @Override
            public void onSuccess(String downloadUrl) {
                runOnUiThread(() -> {
                    // Mettre à jour l'URL dans Firestore
                    updatePdfUrl(downloadUrl);
                    
                    progressBar.setVisibility(View.GONE);
                    tvUploadProgress.setText("✓ Upload terminé avec succès !");
                    
                    // Fermer le dialog après 2 secondes
                    tvUploadProgress.postDelayed(() -> {
                        if (uploadDialog != null) {
                            uploadDialog.dismiss();
                        }
                    }, 2000);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    btnSelectPdf.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    tvUploadProgress.setVisibility(View.GONE);
                    Toast.makeText(RequestDetailActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String getFileName(Uri uri) {
        String fileName = "document.pdf";
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
}
