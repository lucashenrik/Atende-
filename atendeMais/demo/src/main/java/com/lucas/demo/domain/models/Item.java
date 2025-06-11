package com.lucas.demo.domain.models;

import java.time.LocalTime;

public class Item{

    private int referenceId;
    private String name;
    private int quantity;
    private String status;
    private LocalTime time;

    public Item(int referenceId, String name, int quantity, String status, LocalTime time){
        this.referenceId = referenceId;
        this.name = name;
        this.quantity = quantity;
        this.status = status;
        this.time = time;
    }

    public Item(){
    }

    public int getReferenceId() {
        return referenceId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalTime getTime() {
        return time;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }
}