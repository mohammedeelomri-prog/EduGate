package com.example.projet_mobile.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_mobile.R;
import com.example.projet_mobile.models.Announcement;
import com.example.projet_mobile.utils.ImageUploader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Announcement> announcements = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private boolean isAdmin = false;
    private OnAnnouncementActionListener listener;

    public interface OnAnnouncementActionListener {
        void onEdit(Announcement announcement);
        void onDelete(Announcement announcement);
        void onClick(Announcement announcement);
    }

    public AnnouncementAdapter(boolean isAdmin, OnAnnouncementActionListener listener) {
        this.isAdmin = isAdmin;
        this.listener = listener;
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
        Announcement announcement = announcements.get(position);

        // Titre
        holder.tvTitle.setText(announcement.getTitle());

        // Contenu
        holder.tvContent.setText(announcement.getContent());

        // Date
        if (announcement.getCreatedAt() != null) {
            holder.tvDate.setText(dateFormat.format(announcement.getCreatedAt().toDate()));
        }

        // Auteur
        holder.tvAuthor.setText("Par " + announcement.getAuthorName());

        // Image
        if (announcement.getImageBase64() != null && !announcement.getImageBase64().isEmpty()) {
            Bitmap bitmap = ImageUploader.decodeBase64(announcement.getImageBase64());
            if (bitmap != null) {
                holder.ivAnnouncementImage.setImageBitmap(bitmap);
                holder.ivAnnouncementImage.setVisibility(View.VISIBLE);
            } else {
                holder.ivAnnouncementImage.setVisibility(View.GONE);
            }
        } else {
            holder.ivAnnouncementImage.setVisibility(View.GONE);
        }

        // Menu admin
        if (isAdmin) {
            holder.ivMenuAdmin.setVisibility(View.VISIBLE);
            holder.ivMenuAdmin.setOnClickListener(v -> showPopupMenu(v, announcement));
        } else {
            holder.ivMenuAdmin.setVisibility(View.GONE);
        }

        // Click sur l'annonce
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(announcement);
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
        notifyDataSetChanged();
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    private void showPopupMenu(View view, Announcement announcement) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.menu_announcement_admin);
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                if (listener != null) {
                    listener.onEdit(announcement);
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                if (listener != null) {
                    listener.onDelete(announcement);
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate, tvAuthor;
        ImageView ivAnnouncementImage, ivMenuAdmin;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            ivAnnouncementImage = itemView.findViewById(R.id.ivAnnouncementImage);
            ivMenuAdmin = itemView.findViewById(R.id.ivMenuAdmin);
        }
    }
}
