package com.example.projet_mobile.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_mobile.R;
import com.example.projet_mobile.models.DocumentRequest;
import com.example.projet_mobile.utils.PdfDownloader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DocumentRequestAdapter extends RecyclerView.Adapter<DocumentRequestAdapter.ViewHolder> {

    private List<DocumentRequest> requests = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentRequest request = requests.get(position);
        
        holder.tvDocumentType.setText(request.getDocumentType());
        
        if (request.getCreatedAt() != null) {
            holder.tvRequestDate.setText(dateFormat.format(request.getCreatedAt().toDate()));
        }
        
        // Afficher le statut avec couleur
        String status = getStatusText(request.getStatus());
        holder.tvStatus.setText(status);
        
        GradientDrawable background = (GradientDrawable) holder.tvStatus.getBackground();
        background.setColor(getStatusColor(request.getStatus()));
        
        // Afficher le commentaire s'il existe
        if (request.getComment() != null && !request.getComment().isEmpty()) {
            holder.tvComment.setVisibility(View.VISIBLE);
            holder.tvComment.setText(request.getComment());
        } else {
            holder.tvComment.setVisibility(View.GONE);
        }
        
        // Afficher le bouton de téléchargement si statut == "pret" et pdfUrl existe
        if ("pret".equals(request.getStatus()) && 
            request.getPdfUrl() != null && 
            !request.getPdfUrl().isEmpty()) {
            holder.btnDownloadPdf.setVisibility(View.VISIBLE);
            holder.btnDownloadPdf.setOnClickListener(v -> {
                String fileName = request.getDocumentType().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                PdfDownloader.downloadAndOpenPdf(v.getContext(), request.getPdfUrl(), fileName);
            });
        } else {
            holder.btnDownloadPdf.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void setRequests(List<DocumentRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "en_attente":
                return "En attente";
            case "approuvee":
                return "Validé";
            case "pret":
                return "Prêt";
            case "rejetee":
                return "Rejeté";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "en_attente":
                return Color.parseColor("#FF9800"); // Orange
            case "approuvee":
                return Color.parseColor("#4CAF50"); // Vert
            case "pret":
                return Color.parseColor("#2196F3"); // Bleu
            case "rejetee":
                return Color.parseColor("#F44336"); // Rouge
            default:
                return Color.parseColor("#9E9E9E"); // Gris
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocumentType, tvRequestDate, tvStatus, tvComment;
        Button btnDownloadPdf;

        ViewHolder(View itemView) {
            super(itemView);
            tvDocumentType = itemView.findViewById(R.id.tvDocumentType);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvComment = itemView.findViewById(R.id.tvComment);
            btnDownloadPdf = itemView.findViewById(R.id.btnDownloadPdf);
        }
    }
}
