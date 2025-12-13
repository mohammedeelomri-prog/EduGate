package com.example.projet_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projet_mobile.R;
import com.example.projet_mobile.model.Announcement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    // Utilisation d'une Map pour stocker l'objet Annonce et son ID Firestore
    private List<Map<String, Object>> announcementDataList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        // L'écouteur reçoit l'ID du document Firestore, le titre et le contenu
        void onItemClick(String documentId, String title, String content);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // L'Adapter reçoit maintenant une Map contenant l'objet Annonce ET l'ID
    public AnnouncementAdapter(List<Map<String, Object>> announcementDataList) {
        this.announcementDataList = announcementDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = announcementDataList.get(position);
        Announcement announcement = (Announcement) data.get("announcement");
        String documentId = (String) data.get("documentId");

        holder.title.setText(announcement.getTitle());
        holder.contentPreview.setText(announcement.getContent());

        if(announcement.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);
            holder.date.setText("Publié le " + sdf.format(announcement.getDate()));
        }

        // Ajout du clic pour l'édition/détail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && documentId != null) {
                listener.onItemClick(documentId, announcement.getTitle(), announcement.getContent());
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcementDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, contentPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            contentPreview = itemView.findViewById(R.id.tvContentPreview);
        }
    }
}