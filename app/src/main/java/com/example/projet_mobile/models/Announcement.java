package com.example.projet_mobile.models;

import com.google.firebase.Timestamp;

public class Announcement {
    private String id;
    private String title;
    private String content;
    private String imageBase64;
    private String authorId;
    private String authorName;
    private Timestamp createdAt;

    public Announcement() {
    }

    public Announcement(String title, String content, String imageBase64, String authorId, String authorName) {
        this.title = title;
        this.content = content;
        this.imageBase64 = imageBase64;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
