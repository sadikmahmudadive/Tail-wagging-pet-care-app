package com.example.tailwagging;

import android.os.Bundle;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Calendar extends AppCompatActivity implements CalendarAdapter.OnItemListener, MonthPickerAdapter.OnMonthSelectedListener, TodayEventAdapter.OnEventChangedListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView monthPickerRecyclerView;
    private TextView tvYearPicker;
    private RecyclerView todayEventsRecyclerView;
    private TodayEventAdapter todayEventAdapter;
    private Button btnEvents;
    private LocalDate selectedDate;
    private int selectedDay;
    private int selectedMonthIndex;
    private int selectedYear;
    private List<String> monthsList;
    private MonthPickerAdapter monthPickerAdapter;

    private LocalTime fromTime = LocalTime.of(12, 0); // Default from time
    private LocalTime toTime = LocalTime.of(14, 0); // Default to time
    private String selectedCategory = null; // Track selected category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        selectedDate = LocalDate.now();
        selectedYear = selectedDate.getYear();
        selectedDay = selectedDate.getDayOfMonth();
        setUpYearPicker();
        setUpMonthsList();
        setMonthView();
        setUpMonthPicker();
        setUpTodayEventsSection();

        btnEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, EventsActivity.class));
        });
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        monthPickerRecyclerView = findViewById(R.id.monthPickerRecyclerView);
        tvYearPicker = findViewById(R.id.tvYearPicker);
        todayEventsRecyclerView = findViewById(R.id.todayEventsRecyclerView);
        btnEvents = findViewById(R.id.btnEvents);
    }

    private void setUpYearPicker() {
        tvYearPicker.setText(String.valueOf(selectedYear));
        tvYearPicker.setOnClickListener(v -> showYearPickerDialog(tvYearPicker));
    }

    private void showYearPickerDialog(TextView tvYearPicker) {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1900);
        picker.setMaxValue(2100);
        picker.setValue(selectedYear);

        new AlertDialog.Builder(this)
                .setTitle("Select Year")
                .setView(picker)
                .setPositiveButton("OK", (dialog, which) -> {
                    selectedYear = picker.getValue();
                    tvYearPicker.setText(String.valueOf(selectedYear));
                    selectedDate = LocalDate.of(selectedYear, selectedDate.getMonthValue(), 1);
                    setMonthView();
                    updateTodayEvents();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setUpMonthsList() {
        monthsList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String month = YearMonth.now().withMonth(i + 1).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthsList.add(month);
        }
        selectedMonthIndex = selectedDate.getMonthValue() - 1;
    }

    private void setUpMonthPicker() {
        monthPickerAdapter = new MonthPickerAdapter(monthsList, selectedMonthIndex, this);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        monthPickerRecyclerView.setLayoutManager(lm);
        monthPickerRecyclerView.setAdapter(monthPickerAdapter);
        // Scroll to selected month
        monthPickerRecyclerView.post(() -> monthPickerRecyclerView.scrollToPosition(selectedMonthIndex));
    }

    private void setMonthView() {
        monthYearText.setText(selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + selectedDate.getYear());
        ArrayList<DayCell> dayCells = getDayCells(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(dayCells, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Update the month picker selection
        selectedMonthIndex = selectedDate.getMonthValue() - 1;
        if (monthPickerAdapter != null) {
            monthPickerAdapter.setSelectedIndex(selectedMonthIndex);
            monthPickerAdapter.notifyDataSetChanged();
            monthPickerRecyclerView.scrollToPosition(selectedMonthIndex);
        }
    }

    private ArrayList<DayCell> getDayCells(LocalDate date) {
        ArrayList<DayCell> cells = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        YearMonth prevYearMonth = currentYearMonth.minusMonths(1);

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int firstDayOfWeek = date.withDayOfMonth(1).getDayOfWeek().getValue() % 7; // Sunday=0

        // Fill previous month's end days
        int prevMonthDays = prevYearMonth.lengthOfMonth();
        for (int i = 0; i < firstDayOfWeek; i++) {
            int d = prevMonthDays - firstDayOfWeek + i + 1;
            cells.add(new DayCell(String.valueOf(d), true, false, false));
        }

        // Fill current month days
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= daysInMonth; i++) {
            boolean isToday = (today.getYear() == date.getYear() && today.getMonthValue() == date.getMonthValue() && today.getDayOfMonth() == i);
            boolean isSelected = (selectedDay == i && selectedDate.getMonthValue() == date.getMonthValue() && selectedDate.getYear() == date.getYear());
            cells.add(new DayCell(String.valueOf(i), false, isToday, isSelected));
        }

        // Fill next month's start days to complete 6 rows (42 cells)
        int remaining = 42 - cells.size();
        for (int i = 1; i <= remaining; i++) {
            cells.add(new DayCell(String.valueOf(i), true, false, false));
        }
        return cells;
    }

    @Override
    public void onItemClick(int position, DayCell dayCell) {
        if (!dayCell.dayText.isEmpty() && !dayCell.isOtherMonth) {
            selectedDay = Integer.parseInt(dayCell.dayText);
            selectedDate = LocalDate.of(selectedYear, selectedDate.getMonthValue(), selectedDay);
            setMonthView();
            showAddEventDialog(dayCell.dayText);

            // If user taps on today, update the events
            if (selectedDate.equals(LocalDate.now())) {
                updateTodayEvents();
            }
        }
    }

    @Override
    public void onMonthSelected(int monthIndex) {
        selectedMonthIndex = monthIndex;
        selectedDate = LocalDate.of(selectedYear, monthIndex + 1, 1);
        selectedDay = 1;
        setMonthView();
        updateTodayEvents();
    }

    // --- Today's Events Section ---

    private void setUpTodayEventsSection() {
        todayEventsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        todayEventAdapter = new TodayEventAdapter(this, getTodayEvents(LocalDate.now()), this);
        todayEventsRecyclerView.setAdapter(todayEventAdapter);
    }

    private void updateTodayEvents() {
        todayEventAdapter.setEvents(getTodayEvents(LocalDate.now()));
    }

    private List<Event> getTodayEvents(LocalDate date) {
        List<Event> events = EventStore.getInstance(this).getEventsForDate(date);
        // Sort by fromTime
        Collections.sort(events, Comparator.comparing(e -> e.fromTime));
        return events;
    }

    // --- Event Dialog and Time Picker ---

    private void showAddEventDialog(String dayText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etTitle = dialogView.findViewById(R.id.etEventTitle);
        TextView tvFromTime = dialogView.findViewById(R.id.tvFromTime);
        TextView tvToTime = dialogView.findViewById(R.id.tvToTime);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        Button btnSaveEvent = dialogView.findViewById(R.id.btnSaveEvent);
        LinearLayout categoryLayout = dialogView.findViewById(R.id.categoryLayout);

        tvFromTime.setText(fromTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        tvToTime.setText(toTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        // Category Chips (clear first, then add)
        categoryLayout.removeAllViews();
        List<String> categories = Arrays.asList("Meeting With Vet", "Vaccination", "Hangout with pet", "Pet Training", "Recreation & Bonding", "Shopping", "Other");

        List<TextView> chipViews = new ArrayList<>();
        for (String category : categories) {
            TextView chip = createCategoryChip(category, dialogView.getContext());
            categoryLayout.addView(chip);
            chipViews.add(chip);
        }
        for (TextView chip : chipViews) {
            chip.setOnClickListener(v -> {
                for (TextView c : chipViews) {
                    c.setSelected(false);
                    c.setTextColor(Color.parseColor("#222222"));
                }
                chip.setSelected(true);
                chip.setTextColor(Color.WHITE);
                selectedCategory = chip.getText().toString();
            });
        }
        if (!chipViews.isEmpty()) {
            chipViews.get(0).performClick();
        }

        tvFromTime.setOnClickListener(v -> showTimePickerDialog(tvFromTime, true));
        tvToTime.setOnClickListener(v -> showTimePickerDialog(tvToTime, false));

        btnSaveEvent.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String note = etNote.getText().toString();

            if (title.isEmpty()) {
                etTitle.setError("Title required");
                return;
            }
            if (selectedCategory == null) {
                Toast.makeText(this, "Select category", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate unique event id
            int eventId = (int) (System.currentTimeMillis() & 0xFFFFFFF);

            Event event = new Event(
                    eventId,
                    title,
                    selectedCategory,
                    note,
                    selectedDate,
                    fromTime,
                    toTime
            );
            EventStore.getInstance(this).addEvent(event);

            // (Optional: Check for permission before setting exact alarm on Android 12+)
            // requestExactAlarmPermissionIfNeeded();

            AlarmHelper.setEventAlarm(this, event);
            Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show();
            updateTodayEvents();
            dialog.dismiss();
        });

        dialog.show();
    }

    // (Optional: implement this for Android 12+)
    /*
    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    */

    private void showTimePickerDialog(final TextView timeTextView, final boolean isFromTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
                    timeTextView.setText(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                    if (isFromTime) {
                        fromTime = selectedTime;
                    } else {
                        toTime = selectedTime;
                    }
                },
                isFromTime ? fromTime.getHour() : toTime.getHour(),
                isFromTime ? fromTime.getMinute() : toTime.getMinute(),
                true
        );
        timePickerDialog.show();
    }

    private TextView createCategoryChip(String category, android.content.Context context) {
        TextView chip = new TextView(context);
        chip.setText(category);
        chip.setPadding(48, 0, 48, 0);
        chip.setHeight(96);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setTextColor(Color.parseColor("#222222"));
        chip.setTextSize(14);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 24, 0);
        chip.setLayoutParams(params);

        chip.setBackgroundResource(R.drawable.chip_category_selector);
        chip.setSelected(false);

        return chip;
    }

    @Override
    public void onEventDeleted() {
        updateTodayEvents();
    }
}