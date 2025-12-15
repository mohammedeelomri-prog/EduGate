package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_mobile.adapters.AnnouncementAdapter;
import com.example.projet_mobile.models.Announcement;
import com.example.projet_mobile.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAnnouncements;
    private LinearLayout emptyState;
    private AnnouncementAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration announcementsListener;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        recyclerViewAnnouncements = findViewById(R.id.recyclerViewAnnouncements);
        emptyState = findViewById(R.id.emptyState);

        // Configuration RecyclerView
        setupRecyclerView();

        // Vérifier le rôle et charger les annonces
        checkIfAdminAndLoadAnnouncements();
    }

    private void checkIfAdminAndLoadAnnouncements() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            // Admin ou Prof peuvent gérer les annonces
                            isAdmin = "admin".equals(role) || "prof".equals(role);
                            
                            // Mettre à jour l'adapter avec le bon rôle
                            adapter.setIsAdmin(isAdmin);
                        }
                        
                        // Charger les annonces après avoir vérifié le rôle
                        loadAnnouncements();
                    })
                    .addOnFailureListener(e -> {
                        // En cas d'erreur, charger quand même les annonces
                        loadAnnouncements();
                    });
        } else {
            // Pas d'utilisateur connecté, charger quand même
            loadAnnouncements();
        }
    }

    private void setupRecyclerView() {
        adapter = new AnnouncementAdapter(isAdmin, new AnnouncementAdapter.OnAnnouncementActionListener() {
            @Override
            public void onEdit(Announcement announcement) {
                editAnnouncement(announcement);
            }

            @Override
            public void onDelete(Announcement announcement) {
                confirmDelete(announcement);
            }

            @Override
            public void onClick(Announcement announcement) {
                viewAnnouncementDetail(announcement);
            }
        });

        recyclerViewAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnnouncements.setAdapter(adapter);
    }

    private void loadAnnouncements() {
        // Écouter les changements en temps réel
        announcementsListener = db.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        NotificationHelper.showToast(
                                AnnouncementsActivity.this,
                                "Erreur: " + error.getMessage(),
                                NotificationHelper.NotificationType.ERROR
                        );
                        return;
                    }

                    if (snapshots != null) {
                        List<Announcement> announcements = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Announcement announcement = doc.toObject(Announcement.class);
                            announcement.setId(doc.getId());
                            announcements.add(announcement);
                        }

                        // Mettre à jour l'affichage
                        if (announcements.isEmpty()) {
                            recyclerViewAnnouncements.setVisibility(View.GONE);
                            emptyState.setVisibility(View.VISIBLE);
                        } else {
                            recyclerViewAnnouncements.setVisibility(View.VISIBLE);
                            emptyState.setVisibility(View.GONE);
                            adapter.setAnnouncements(announcements);
                        }
                    }
                });
    }

    private void editAnnouncement(Announcement announcement) {
        Intent intent = new Intent(this, EditAnnouncementActivity.class);
        intent.putExtra("announcementId", announcement.getId());
        intent.putExtra("title", announcement.getTitle());
        intent.putExtra("content", announcement.getContent());
        intent.putExtra("imageBase64", announcement.getImageBase64());
        startActivity(intent);
    }

    private void confirmDelete(Announcement announcement) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l'annonce")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette annonce ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    deleteAnnouncement(announcement);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteAnnouncement(Announcement announcement) {
        db.collection("announcements")
                .document(announcement.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    NotificationHelper.showToast(
                            AnnouncementsActivity.this,
                            "✓ Annonce supprimée",
                            NotificationHelper.NotificationType.SUCCESS
                    );
                })
                .addOnFailureListener(e -> {
                    NotificationHelper.showToast(
                            AnnouncementsActivity.this,
                            "Erreur: " + e.getMessage(),
                            NotificationHelper.NotificationType.ERROR
                    );
                });
    }

    private void viewAnnouncementDetail(Announcement announcement) {
        Intent intent = new Intent(this, AnnouncementDetailActivity.class);
        intent.putExtra("announcementId", announcement.getId());
        intent.putExtra("title", announcement.getTitle());
        intent.putExtra("content", announcement.getContent());
        intent.putExtra("imageBase64", announcement.getImageBase64());
        intent.putExtra("authorName", announcement.getAuthorName());
        intent.putExtra("createdAt", announcement.getCreatedAt() != null ?
                announcement.getCreatedAt().toDate().getTime() : 0L);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (announcementsListener != null) {
            announcementsListener.remove();
        }
    }
}
