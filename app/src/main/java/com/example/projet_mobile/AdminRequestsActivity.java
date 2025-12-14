package com.example.projet_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_mobile.adapters.AdminRequestAdapter;
import com.example.projet_mobile.models.DocumentRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAdminRequests;
    private TextView tvEmptyState;
    private Spinner spinnerStatusFilter, spinnerTypeFilter;
    private AdminRequestAdapter adapter;
    
    private FirebaseFirestore db;
    private ListenerRegistration requestsListener;
    
    private String selectedStatus = "Tous";
    private String selectedType = "Tous";
    
    private String[] statusFilters = {"Tous", "En attente", "Validé", "Prêt", "Rejeté"};
    private String[] typeFilters = {
        "Tous",
        "Certificat de scolarité",
        "Attestation de réussite",
        "Relevé de notes",
        "Attestation d'inscription",
        "Certificat de stage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_requests);

        db = FirebaseFirestore.getInstance();

        recyclerViewAdminRequests = findViewById(R.id.recyclerViewAdminRequests);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerTypeFilter = findViewById(R.id.spinnerTypeFilter);

        setupSpinners();
        setupRecyclerView();
        loadRequests();
    }

    private void setupSpinners() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusFilters);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, typeFilters);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeFilter.setAdapter(typeAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = statusFilters[position];
                loadRequests();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = typeFilters[position];
                loadRequests();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminRequestAdapter(request -> {
            Intent intent = new Intent(AdminRequestsActivity.this, RequestDetailActivity.class);
            intent.putExtra("requestId", request.getId());
            intent.putExtra("studentName", request.getUserName());
            intent.putExtra("documentType", request.getDocumentType());
            intent.putExtra("status", request.getStatus());
            intent.putExtra("comment", request.getComment());
            intent.putExtra("createdAt", request.getCreatedAt() != null ? 
                    request.getCreatedAt().toDate().getTime() : 0L);
            startActivity(intent);
        });
        
        recyclerViewAdminRequests.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdminRequests.setAdapter(adapter);
    }

    private void loadRequests() {
        if (requestsListener != null) {
            requestsListener.remove();
        }

        Query query = db.collection("document_requests")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        requestsListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Toast.makeText(this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                List<DocumentRequest> allRequests = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    DocumentRequest request = doc.toObject(DocumentRequest.class);
                    request.setId(doc.getId());
                    allRequests.add(request);
                }

                List<DocumentRequest> filteredRequests = filterRequests(allRequests);

                if (filteredRequests.isEmpty()) {
                    recyclerViewAdminRequests.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewAdminRequests.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);
                    adapter.setRequests(filteredRequests);
                }
            }
        });
    }

    private List<DocumentRequest> filterRequests(List<DocumentRequest> requests) {
        List<DocumentRequest> filtered = new ArrayList<>();
        
        for (DocumentRequest request : requests) {
            boolean matchStatus = selectedStatus.equals("Tous") || 
                    matchesStatus(request.getStatus(), selectedStatus);
            boolean matchType = selectedType.equals("Tous") || 
                    request.getDocumentType().equals(selectedType);
            
            if (matchStatus && matchType) {
                filtered.add(request);
            }
        }
        
        return filtered;
    }

    private boolean matchesStatus(String status, String filter) {
        switch (filter) {
            case "En attente":
                return "en_attente".equals(status);
            case "Validé":
                return "approuvee".equals(status);
            case "Prêt":
                return "pret".equals(status);
            case "Rejeté":
                return "rejetee".equals(status);
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestsListener != null) {
            requestsListener.remove();
        }
    }
}
