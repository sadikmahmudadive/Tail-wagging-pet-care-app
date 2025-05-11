package com.example.tailwagging;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class AddEditPet extends AppCompatActivity {

    private EditText etPetDob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Gender Dropdown Setup
        AutoCompleteTextView genderDropdown = findViewById(R.id.etPetGender);
        String[] genderOptions = new String[] { "Male", "Female", "Other" };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderDropdown.setAdapter(genderAdapter);
        genderDropdown.setInputType(0);
        genderDropdown.setKeyListener(null);
        genderDropdown.setOnClickListener(v -> genderDropdown.showDropDown());

        // Date of Birth Picker Setup
        etPetDob = findViewById(R.id.etPetDob);
        etPetDob.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        // Use current date as default
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0-based!
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: YYYY-MM-DD
                    String dob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etPetDob.setText(dob);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}