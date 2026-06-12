package com.example.tailwagging;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private String vetId, vetName;
    private TextView tvVetName;
    private Spinner spinnerPet;
    private EditText etDate, etTime;
    private Button btnConfirm;
    private ImageButton btnBack;

    private DatabaseReference dbRef;
    private List<Pet> userPets = new ArrayList<>();
    private LocalDate selectedDate;
    private LocalTime selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        vetId = getIntent().getStringExtra("VET_ID");
        vetName = getIntent().getStringExtra("VET_NAME");

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        tvVetName = findViewById(R.id.tvBookingVetName);
        spinnerPet = findViewById(R.id.spinnerPetBooking);
        etDate = findViewById(R.id.etBookingDate);
        etTime = findViewById(R.id.etBookingTime);
        btnConfirm = findViewById(R.id.btnConfirmBooking);
        btnBack = findViewById(R.id.btnBack);

        tvVetName.setText(vetName);
        btnBack.setOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        btnConfirm.setOnClickListener(v -> saveAppointment());

        fetchUserPets();
    }

    private void fetchUserPets() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        dbRef.child("pets").orderByChild("ownerID").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> petNames = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Pet pet = snapshot.getValue(Pet.class);
                            if (pet != null) {
                                userPets.add(pet);
                                petNames.add(pet.getName());
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BookingActivity.this, android.R.layout.simple_spinner_item, petNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPet.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            etDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedTime = LocalTime.of(hourOfDay, minute);
            etTime.setText(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveAppointment() {
        if (selectedDate == null || selectedTime == null || spinnerPet.getSelectedItem() == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Pet selectedPet = userPets.get(spinnerPet.getSelectedItemPosition());
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Fetch user's name first to save it in the appointment
        dbRef.child("users").child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ownerName = snapshot.getValue(String.class);
                if (ownerName == null) ownerName = "Pet Owner";

                String appointmentId = dbRef.child("appointments").push().getKey();
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("id", appointmentId);
                appointment.put("userId", userId);
                appointment.put("ownerName", ownerName);
                appointment.put("vetId", vetId);
                appointment.put("vetName", vetName);
                appointment.put("petId", selectedPet.getPetID());
                appointment.put("petName", selectedPet.getName());
                appointment.put("petImageUrl", selectedPet.getImageUrl());
                appointment.put("date", selectedDate.toString());
                appointment.put("time", selectedTime.toString());
                appointment.put("status", "Pending");

                if (appointmentId != null) {
                    dbRef.child("appointments").child(appointmentId).setValue(appointment)
                            .addOnSuccessListener(aVoid -> {
                                addBookingToEvents(selectedPet, vetName, selectedDate, selectedTime);
                                Toast.makeText(BookingActivity.this, "Appointment Requested!", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(BookingActivity.this, "Failed to book", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingActivity.this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookingToEvents(Pet pet, String vetName, LocalDate date, LocalTime time) {
        int eventId = (int) System.currentTimeMillis();
        String userId = FirebaseAuth.getInstance().getUid();
        Event bookingEvent = new Event(
                eventId,
                userId,
                "Vet: " + vetName,
                "Vet Appointment",
                "Appointment for " + pet.getName(),
                pet.getName(),
                pet.getPetID(),
                date,
                time,
                time.plusHours(1),
                true
        );
        EventStore.getInstance(this).addEvent(bookingEvent);
        AlarmHelper.setEventAlarm(this, bookingEvent);
    }
}