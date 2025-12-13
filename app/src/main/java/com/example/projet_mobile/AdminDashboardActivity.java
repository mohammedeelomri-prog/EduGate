package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.example.projet_mobile.announcement.CreateAnnouncementActivity;
import com.example.projet_mobile.announcement.AnnouncementListActivity; // Importez la liste

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnCreateAnnouncement;
    private Button btnManageAnnouncements;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnLogout);
        btnCreateAnnouncement = findViewById(R.id.btnCreateAnnouncement);
        btnManageAnnouncements = findViewById(R.id.btnManageAnnouncements); // Liaison du nouveau bouton

        // 1. Logique du bouton "Créer une nouvelle annonce"
        btnCreateAnnouncement.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, CreateAnnouncementActivity.class);
            startActivity(intent);
        });

        // 2. Logique du bouton "Gérer les annonces"
        btnManageAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AnnouncementListActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}