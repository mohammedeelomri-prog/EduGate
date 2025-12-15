package com.example.projet_mobile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_mobile.utils.ImageUploader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnnouncementDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle, tvDetailContent, tvDetailAuthor, tvDetailDate;
    private ImageView ivDetailImage;
    private Button btnClose;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

        // Initialisation des vues
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        tvDetailAuthor = findViewById(R.id.tvDetailAuthor);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        ivDetailImage = findViewById(R.id.ivDetailImage);
        btnClose = findViewById(R.id.btnClose);

        // Récupérer les données
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String imageBase64 = getIntent().getStringExtra("imageBase64");
        String authorName = getIntent().getStringExtra("authorName");
        long createdAtMillis = getIntent().getLongExtra("createdAt", 0);

        // Afficher les données
        tvDetailTitle.setText(title);
        tvDetailContent.setText(content);
        tvDetailAuthor.setText("Par " + authorName);

        if (createdAtMillis > 0) {
            tvDetailDate.setText(dateFormat.format(new Date(createdAtMillis)));
        }

        // Afficher l'image si présente
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            Bitmap bitmap = ImageUploader.decodeBase64(imageBase64);
            if (bitmap != null) {
                ivDetailImage.setImageBitmap(bitmap);
                ivDetailImage.setVisibility(View.VISIBLE);
            }
        }

        btnClose.setOnClickListener(v -> finish());
    }
}
