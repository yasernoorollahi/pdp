package com.datarain.pdp.item.dto;

import com.datarain.pdp.item.entity.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ItemRequest {

    @NotBlank(message = "title must not be blank")
    @Size(min = 3, max = 100, message = "title length must be between 3 and 100")
    private String title;

    @NotNull(message = "type must not be null")
    private ItemType type;   // 👈 حالا Enum شد

    @NotBlank(message = "content must not be blank")
    private String content;

    @Size(max = 500, message = "description max length is 500")
    private String description;



    // getter / setter
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
