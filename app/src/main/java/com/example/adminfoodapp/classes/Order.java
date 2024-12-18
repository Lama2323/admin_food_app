package com.example.adminfoodapp.classes;
import java.util.Date;
import java.util.List;

public class Order {
    private String objectId;
    private String address;
    private String food_list; // JSON string
    private boolean is_done;
    private int payment_method;
    private String phone_number;
    private int total;
    private Date created;
    private Date updated;

    // Getters and Setters

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFood_list() {
        return food_list;
    }

    public void setFood_list(String food_list) {
        this.food_list = food_list;
    }

    public boolean isIs_done() {
        return is_done;
    }

    public void setIs_done(boolean is_done) {
        this.is_done = is_done;
    }

    public int getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(int payment_method) {
        this.payment_method = payment_method;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}