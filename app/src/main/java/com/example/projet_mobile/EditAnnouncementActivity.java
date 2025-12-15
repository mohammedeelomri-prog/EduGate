package com.example.projet_mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_mobile.utils.ImageUploader;
import com.example.projet_mobile.utils.NotificationHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAnnouncementActivity extends AppCompatActivity {

    private TextInputLayout titleLayout, contentLayout;
    private TextInputEditText etTitle, etContent;
    private Button btnSelectImage, btnRemoveImage, btnUpdate, btnCancel;
    private MaterialCardView imagePreviewCard;
    private ImageView ivPreview;
    private ProgressBar progressBar;
    private TextView tvProgress;

    private FirebaseFirestore db;
    private String announcementId;
    private String imageBase64 = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        // Récupérer les données
        announcementId = getIntent().getStringExtra("announcementId");
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        imageBase64 = getIntent().getStringExtra("imageBase64");

        // Initialisation des vues
        titleLayout = findViewById(R.id.titleLayout);
        contentLayout = findViewById(R.id.contentLayout);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        btnUpdate = findViewById(R.id.btnPublish);
        btnCancel = findViewById(R.id.btnCancel);
        imagePreviewCard = findViewById(R.id.imagePreviewCard);
        ivPreview = findViewById(R.id.ivPreview);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        // Modifier le texte du bouton
        btnUpdate.setText("💾 Mettre à jour");

        // Remplir les champs
        etTitle.setText(title);
        etContent.setText(content);

        // Afficher l'image si présente
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            Bitmap bitmap = ImageUploader.decodeBase64(imageBase64);
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap);
                imagePreviewCard.setVisibility(View.VISIBLE);
                btnSelectImage.setText("🖼️ Changer l'image");
            }
        }

        // Configuration du sélecteur d'image
        setupImagePicker();

        // Listeners
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnRemoveImage.setOnClickListener(v -> removeImage());
        btnUpdate.setOnClickListener(v -> updateAnnouncement());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImage(imageUri);
                        }
                    }
                });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner une image"));
    }

    private void uploadImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        tvProgress.setVisibility(View.VISIBLE);
        btnSelectImage.setEnabled(false);

        ImageUploader.uploadImage(this, imageUri, new ImageUploader.UploadCallback() {
            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                    tvProgress.setText("Traitement de l'image... " + progress + "%");
                });
            }

            @Override
            public void onSuccess(String base64Image) {
                runOnUiThread(() -> {
                    imageBase64 = base64Image;
                    Bitmap bitmap = ImageUploader.decodeBase64(base64Image);
                    if (bitmap != null) {
                        ivPreview.setImageBitmap(bitmap);
                        imagePreviewCard.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    btnSelectImage.setEnabled(true);
                    btnSelectImage.setText("🖼️ Changer l'image");
                    NotificationHelper.showToast(
                            EditAnnouncementActivity.this,
                            "Image mise à jour",
                            NotificationHelper.NotificationType.SUCCESS
                    );
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    btnSelectImage.setEnabled(true);
                    NotificationHelper.showToast(
                            EditAnnouncementActivity.this,
                            error,
                            NotificationHelper.NotificationType.ERROR
                    );
                });
            }
        });
    }

    private void removeImage() {
        imageBase64 = null;
        imagePreviewCard.setVisibility(View.GONE);
        ivPreview.setImageBitmap(null);
        btnSelectImage.setText("🖼️ Sélectionner une image");
        NotificationHelper.showToast(
                this,
                "Image supprimée",
                NotificationHelper.NotificationType.INFO
        );
    }

    private void updateAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError("Le titre est obligatoire");
            isValid = false;
        } else {
            titleLayout.setError(null);
        }

        if (TextUtils.isEmpty(content)) {
            contentLayout.setError("Le contenu est obligatoire");
            isValid = false;
        } else {
            contentLayout.setError(null);
        }

        if (!isValid) {
            return;
        }

        // Désactiver le bouton
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Mise à jour en cours...");

        // Mettre à jour dans Firestore
        db.collection("announcements")
                .document(announcementId)
                .update(
                        "title", title,
                        "content", content,
                        "imageBase64", imageBase64
                )
                .addOnSuccessListener(aVoid -> {
                    NotificationHelper.showToast(
                            EditAnnouncementActivity.this,
                            "✓ Annonce mise à jour",
                            NotificationHelper.NotificationType.SUCCESS
                    );
                    finish();
                })
                .addOnFailureListener(e -> {
                    NotificationHelper.showToast(
                            EditAnnouncementActivity.this,
                            "Erreur: " + e.getMessage(),
                            NotificationHelper.NotificationType.ERROR
                    );
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("💾 Mettre à jour");
                });
    }
}
