package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.example.projet_mobile.announcement.AnnouncementListActivity;
public class StudentDashboardActivity extends AppCompatActivity {

    Button btnLogout;
    Button btnViewAnnouncements;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnLogout);
        btnViewAnnouncements = findViewById(R.id.btnViewAnnouncements); // Liaison


        btnViewAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, AnnouncementListActivity.class);
            startActivity(intent);
        });

        // Logique de déconnexion existante
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent loginIntent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        });
    }
}