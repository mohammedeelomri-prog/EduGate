package com.example.projet_mobile.announcement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projet_mobile.R;
import com.example.projet_mobile.adapter.AnnouncementAdapter;
import com.example.projet_mobile.model.Announcement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.projet_mobile.LoginActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Map<String, Object>> announcementDataList;
    private AnnouncementAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // --- VÉRIFICATION IMMÉDIATE DE LA SESSION ---
        // S'assurer que l'utilisateur est connecté avant d'initialiser la liste
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Session perdue. Reconnexion requise.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // Stoppe l'exécution si la session est invalide
        }
        // ------------------------------------------

        announcementDataList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnnouncementAdapter(announcementDataList);
        recyclerView.setAdapter(adapter);

        // GESTION DU CLIC : Logique d'édition pour l'Admin, détail pour l'Étudiant
        adapter.setOnItemClickListener((documentId, title, content) -> {

            // mAuth.getCurrentUser() est garanti non-null ici grâce à la vérification de début de onCreate()
            final String ADMIN_EMAIL = "admin.test@edugate.com";
            String userEmail = mAuth.getCurrentUser().getEmail();

            if (userEmail != null && userEmail.equals(ADMIN_EMAIL)) {

                // Lancement de l'activité d'édition (Admin)
                Intent intent = new Intent(this, EditAnnouncementActivity.class);
                intent.putExtra("ANNOUNCEMENT_ID", documentId);
                intent.putExtra("ANNOUNCEMENT_TITLE", title);
                intent.putExtra("ANNOUNCEMENT_CONTENT", content);
                startActivity(intent);

            } else {
                // Comportement par défaut (pour les étudiants)
                Toast.makeText(this, "Détail de l'annonce : " + title, Toast.LENGTH_SHORT).show();
            }
        });

        // Appel initial pour charger les annonces au démarrage
        loadAnnouncements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharge la liste chaque fois que l'activité redevient visible
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        db.collection("announcements")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        announcementDataList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Créer une Map contenant à la fois l'objet Annonce ET son ID
                            Map<String, Object> data = new HashMap<>();
                            data.put("announcement", document.toObject(Announcement.class));
                            data.put("documentId", document.getId());
                            announcementDataList.add(data);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Erreur de chargement des annonces. Vérifiez Firebase.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}