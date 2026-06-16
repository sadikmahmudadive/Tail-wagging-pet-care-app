package com.example.tailwagging;

import java.io.Serializable;

public class Appointment implements Serializable {
    public String id;
    public String userId;
    public String ownerName;
    public String vetId;
    public String vetName;
    public String petId;
    public String petName;
    public String petImageUrl;
    public String date;
    public String time;
    public String status;

    public Appointment() {} // Required for Firebase
}