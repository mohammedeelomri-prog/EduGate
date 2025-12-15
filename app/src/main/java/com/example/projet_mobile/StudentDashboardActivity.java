
package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class StudentDashboardActivity extends AppCompatActivity {

    Button btnLogout, btnRequestDocument, btnViewRequests, btnViewAnnouncements;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnLogout);
        btnRequestDocument = findViewById(R.id.btnRequestDocument);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        btnViewAnnouncements = findViewById(R.id.btnViewAnnouncements);

        btnRequestDocument.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, DocumentRequestActivity.class);
            startActivity(intent);
        });

        btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, MyRequestsActivity.class);
            startActivity(intent);
        });

        btnViewAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, AnnouncementsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Déconnexion Firebase

            // Revenir vers LoginActivity
            Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}



