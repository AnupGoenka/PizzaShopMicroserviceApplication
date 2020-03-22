package com.austindorsey.menumicroservice.models;

public class UpdateMenuRequest {
    String items;

    public UpdateMenuRequest(String items) {
        this.items = items;
    }
    
    public UpdateMenuRequest() {
        this.items = null;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }
}