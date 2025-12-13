package com.example.projet_mobile.model;

import java.util.Date;

public class Announcement {
    private String title;
    private String content;
    private Date date;

    // ⚠️ Constructeur vide OBLIGATOIRE pour Firebase
    public Announcement() {}

    public Announcement(String title, String content, Date date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Date getDate() { return date; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDate(Date date) { this.date = date; }
}