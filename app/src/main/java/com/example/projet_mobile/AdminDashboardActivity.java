package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

        Button btnLogout, btnManageRequests, btnCreateAnnouncement, btnViewAnnouncements;
        FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_admin_dashboard);

            mAuth = FirebaseAuth.getInstance();
            btnLogout = findViewById(R.id.btnLogout);
            btnManageRequests = findViewById(R.id.btnManageRequests);
            btnCreateAnnouncement = findViewById(R.id.btnCreateAnnouncement);
            btnViewAnnouncements = findViewById(R.id.btnViewAnnouncements);

            btnManageRequests.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminRequestsActivity.class);
                startActivity(intent);
            });

            btnCreateAnnouncement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, CreateAnnouncementActivity.class);
                startActivity(intent);
            });

            btnViewAnnouncements.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AnnouncementsActivity.class);
                startActivity(intent);
            });

            btnLogout.setOnClickListener(v -> {
                mAuth.signOut(); // Déconnexion Firebase

                // Revenir vers LoginActivity
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

    }

