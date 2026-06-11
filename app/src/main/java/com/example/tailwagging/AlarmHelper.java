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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.set(event.date.getYear(), event.date.getMonthValue() - 1, event.date.getDayOfMonth(), event.fromTime.getHour(), event.fromTime.getMinute(), 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public static void cancelEventAlarm(Context context, int eventId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, eventId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}