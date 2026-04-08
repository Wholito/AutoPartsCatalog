package com.autoparts.catalog;

public class Part {
    private long id;
    private String title;
    private String description;
    private String date;
    private String category;
    private String imageUrl;
    private String remoteId;

    public Part(long id, String title, String description, String date,
                String category, String imageUrl, String remoteId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.category = category != null ? category : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.remoteId = remoteId != null ? remoteId : "";
    }

    public Part(long id, String title, String description, String date) {
        this(id, title, description, date, "", "", "");
    }

    public Part(String title, String description, String date) {
        this(-1, title, description, date, "", "", "");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category != null ? category : "";
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId != null ? remoteId : "";
    }

    /** Текст для нечёткого поиска по ключевым словам */
    public String searchableText() {
        return (title + " " + description + " " + category).toLowerCase();
    }
}
