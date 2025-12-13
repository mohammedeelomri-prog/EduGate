package com.example.projet_mobile.announcement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projet_mobile.R;
import com.example.projet_mobile.model.Announcement;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

public class CreateAnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnPublish;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnPublish = findViewById(R.id.btnPublish);

        btnPublish.setOnClickListener(v -> publishAnnouncement());
    }

    private void publishAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Announcement announcement = new Announcement(title, content, new Date());

        db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Annonce publiée avec succès !", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(CreateAnnouncementActivity.this, AnnouncementListActivity.class);
                    startActivity(intent);
                    finish();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur d'envoi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}