package com.example.projet_mobile.models;

import com.google.firebase.Timestamp;

public class DocumentRequest {
    private String id;
    private String userId;
    private String userName;
    private String documentType;
    private String comment;
    private String status; // "en_attente", "approuvee", "pret", "rejetee"
    private String pdfUrl; // URL du PDF dans Firebase Storage
    private Timestamp createdAt;

    public DocumentRequest() {
        // Constructeur vide requis pour Firestore
    }

    public DocumentRequest(String userId, String userName, String documentType, String comment) {
        this.userId = userId;
        this.userName = userName;
        this.documentType = documentType;
        this.comment = comment;
        this.status = "en_attente";
        this.createdAt = Timestamp.now();
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
