package com.example.tailwagging;

import java.io.Serializable;

public class ServiceRecord implements Serializable {
    public String recordId;
    public String petId;
    public String petName;
    public String providerId;
    public String providerName;
    public String providerRole;
    public String date;
    public String title; // e.g., "Full Grooming", "Vaccination", "Weekend Stay"
    public String description;
    public long timestamp;

    public ServiceRecord() {} // Required for Firebase

    public ServiceRecord(String recordId, String petId, String petName, String providerId, String providerName, String providerRole, String date, String title, String description, long timestamp) {
        this.recordId = recordId;
        this.petId = petId;
        this.petName = petName;
        this.providerId = providerId;
        this.providerName = providerName;
        this.providerRole = providerRole;
        this.date = date;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }
}