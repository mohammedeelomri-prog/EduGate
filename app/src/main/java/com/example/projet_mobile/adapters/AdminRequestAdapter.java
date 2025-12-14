package com.example.projet_mobile.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_mobile.R;
import com.example.projet_mobile.models.DocumentRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.ViewHolder> {

    private List<DocumentRequest> requests = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(DocumentRequest request);
    }

    public AdminRequestAdapter(OnRequestClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentRequest request = requests.get(position);
        
        holder.tvStudentName.setText(request.getUserName());
        holder.tvDocumentType.setText(request.getDocumentType());
        
        if (request.getCreatedAt() != null) {
            holder.tvRequestDate.setText("Soumis le " + dateFormat.format(request.getCreatedAt().toDate()));
        }
        
        String status = getStatusText(request.getStatus());
        holder.tvStatus.setText(status);
        
        GradientDrawable background = (GradientDrawable) holder.tvStatus.getBackground();
        background.setColor(getStatusColor(request.getStatus()));
        
        if (request.getComment() != null && !request.getComment().isEmpty()) {
            holder.tvComment.setVisibility(View.VISIBLE);
            holder.tvComment.setText("💬 " + request.getComment());
        } else {
            holder.tvComment.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestClick(request);
            }
        });
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
                return Color.parseColor("#FF9800");
            case "approuvee":
                return Color.parseColor("#4CAF50");
            case "pret":
                return Color.parseColor("#2196F3");
            case "rejetee":
                return Color.parseColor("#F44336");
            default:
                return Color.parseColor("#9E9E9E");
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvDocumentType, tvRequestDate, tvStatus, tvComment;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvDocumentType = itemView.findViewById(R.id.tvDocumentType);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvComment = itemView.findViewById(R.id.tvComment);
        }
    }
}
