package com.moneytrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public final class Reminder {
    public static final String PREF = "reminder";
    private static final int REQUEST_CODE = 5501;

    private static PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.moneytrack.DAILY_REMINDER");
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }

    /** Schedules the next local occurrence. Exact alarms are used when Android permits them. */
    public static void schedule(Context context) {
        Context app = context.getApplicationContext();
        android.content.SharedPreferences prefs = app.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false)) {
            cancel(app);
            return;
        }

        int hour = Math.max(0, Math.min(23, prefs.getInt("hour", 20)));
        int minute = Math.max(0, Math.min(59, prefs.getInt("minute", 0)));
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager manager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        PendingIntent pi = alarmIntent(app);
        try {
            if (Build.VERSION.SDK_INT >= 31 && !manager.canScheduleExactAlarms()) {
                // Fallback so a reminder is still scheduled; Android may deliver it later.
                if (Build.VERSION.SDK_INT >= 23) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
                } else {
                    manager.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
                }
            } else if (Build.VERSION.SDK_INT >= 23) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
            } else {
                manager.setExact(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
            }
            prefs.edit().putLong("next_alarm", next.getTimeInMillis()).apply();
        } catch (SecurityException denied) {
            // Exact-alarm access can be revoked between the permission check and scheduling.
            try {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
            } catch (Exception ignored) { }
        }
    }

    public static void cancel(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) manager.cancel(alarmIntent(context));
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove("next_alarm").apply();
    }
}
