package com.example.projet_mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_mobile.adapters.DocumentRequestAdapter;
import com.example.projet_mobile.models.DocumentRequest;
import com.example.projet_mobile.utils.NotificationHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRequests;
    private TextView tvEmptyState;
    private DocumentRequestAdapter adapter;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration requestsListener;
    
    // Pour détecter les changements de statut
    private Map<String, String> previousStatuses = new HashMap<>();
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        recyclerViewRequests = findViewById(R.id.recyclerViewRequests);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Configuration RecyclerView
        adapter = new DocumentRequestAdapter();
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRequests.setAdapter(adapter);

        // Charger les demandes
        loadRequests();
    }

    private void loadRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Écouter les changements en temps réel avec SnapshotListener
        requestsListener = db.collection("document_requests")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(MyRequestsActivity.this,
                                "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        // Détecter les changements de statut
                        if (!isFirstLoad) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                    DocumentRequest request = dc.getDocument().toObject(DocumentRequest.class);
                                    String requestId = dc.getDocument().getId();
                                    String newStatus = request.getStatus();
                                    String oldStatus = previousStatuses.get(requestId);
                                    
                                    if (oldStatus != null && !oldStatus.equals(newStatus)) {
                                        // Le statut a changé, afficher une notification
                                        showStatusChangeNotification(request.getDocumentType(), oldStatus, newStatus);
                                    }
                                }
                            }
                        }
                        
                        List<DocumentRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            DocumentRequest request = doc.toObject(DocumentRequest.class);
                            request.setId(doc.getId());
                            requests.add(request);
                            
                            // Sauvegarder le statut actuel
                            previousStatuses.put(doc.getId(), request.getStatus());
                        }

                        // Mettre à jour l'affichage
                        if (requests.isEmpty()) {
                            recyclerViewRequests.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            recyclerViewRequests.setVisibility(View.VISIBLE);
                            tvEmptyState.setVisibility(View.GONE);
                            adapter.setRequests(requests);
                        }
                        
                        isFirstLoad = false;
                    }
                });
    }

    private void showStatusChangeNotification(String documentType, String oldStatus, String newStatus) {
        String message = getStatusChangeMessage(documentType, newStatus);
        
        // Afficher un Snackbar avec l'icône appropriée
        View rootView = findViewById(android.R.id.content);
        NotificationHelper.NotificationType type = getNotificationType(newStatus);
        
        if (rootView != null) {
            NotificationHelper.showSnackbar(rootView, message, type);
        } else {
            NotificationHelper.showToast(this, message, type);
        }
    }
    
    private String getStatusChangeMessage(String documentType, String newStatus) {
        switch (newStatus) {
            case "approuvee":
                return "✓ " + documentType + " : Demande validée";
            case "pret":
                return "📄 " + documentType + " : Document prêt à télécharger !";
            case "rejetee":
                return "✗ " + documentType + " : Demande rejetée";
            default:
                return documentType + " : Statut mis à jour";
        }
    }
    
    private NotificationHelper.NotificationType getNotificationType(String status) {
        switch (status) {
            case "approuvee":
            case "pret":
                return NotificationHelper.NotificationType.SUCCESS;
            case "rejetee":
                return NotificationHelper.NotificationType.ERROR;
            default:
                return NotificationHelper.NotificationType.INFO;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Arrêter l'écoute des changements
        if (requestsListener != null) {
            requestsListener.remove();
        }
    }
}
