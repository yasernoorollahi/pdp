package com.datarain.pdp.item.dto;

import com.datarain.pdp.item.entity.ItemType;

import java.util.UUID;

public class ItemResponse {

    private UUID id;
    private String title;
    private ItemType type;   // 👈 Enum
    private String content;
    private String description;



    // getter / setter
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
