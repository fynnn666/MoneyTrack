package com.moneytrack;

import android.app.*;
import android.content.*;
import java.util.Calendar;

public class Reminder {
    static final String PREF="reminder";
    public static void schedule(Context c) {
        android.content.SharedPreferences p=c.getSharedPreferences(PREF,0);
        if(!p.getBoolean("enabled",false)) return;
        int h=p.getInt("hour",20), m=p.getInt("minute",0);
        Calendar cal=Calendar.getInstance(); cal.set(Calendar.HOUR_OF_DAY,h); cal.set(Calendar.MINUTE,m); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
        if(cal.getTimeInMillis()<=System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR,1);
        Intent in=new Intent(c,ReminderReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,55,in,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(android.os.Build.VERSION.SDK_INT>=23) {
            if(android.os.Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
            else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
        } else am.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
    }
    public static void cancel(Context c) {
        Intent in=new Intent(c,ReminderReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,55,in,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(pi);
    }
}
