package com.example.tailwagging;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmHelper {
    public static void setEventAlarm(Context context, Event event) {
        if (!event.isReminderEnabled) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                Toast.makeText(context, "Please allow exact alarms for reminders", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Intent intent = new Intent(context, EventAlarmReceiver.class);
        intent.putExtra("title", event.title);
        intent.putExtra("category", event.category);
        intent.putExtra("id", event.id);
        intent.putExtra("userId", event.userId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.set(event.date.getYear(), event.date.getMonthValue() - 1, event.date.getDayOfMonth(), event.fromTime.getHour(), event.fromTime.getMinute(), 0);
        cal.set(Calendar.MILLISECOND, 0);

        // If the time is in the past, don't set the alarm (or it will fire immediately)
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public static void setFeedingAlarms(Context context, Pet pet) {
        if (pet.getFeedingTimes() == null || pet.getFeedingTimes().isEmpty()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }

        for (int i = 0; i < pet.getFeedingTimes().size(); i++) {
            String time = pet.getFeedingTimes().get(i);
            try {
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                // Notification 10 min before
                cal.add(Calendar.MINUTE, -10);

                if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }

                Intent intent = new Intent(context, EventAlarmReceiver.class);
                intent.putExtra("title", "Feeding Time for " + pet.getName());
                intent.putExtra("category", "Food");
                
                // Unique ID for each alarm: PetHash + Index + Offset
                int alarmId = (pet.getPetID() != null ? pet.getPetID().hashCode() : 0) + i + 2000;
                intent.putExtra("id", alarmId);
                intent.putExtra("userId", pet.getOwnerID());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void cancelEventAlarm(Context context, int eventId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, eventId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
