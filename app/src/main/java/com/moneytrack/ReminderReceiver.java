package com.moneytrack;

import android.app.*;
import android.content.*;
import android.os.Build;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReminderReceiver extends BroadcastReceiver {
    public void onReceive(Context c, Intent i) {
        DB db=new DB(c);
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
        double out=db.total("OUT",today);
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        String channel="moneytrack_daily";
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(channel,"Pengingat MoneyTrack",NotificationManager.IMPORTANCE_DEFAULT));
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,channel):new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_dialog_info)
         .setContentTitle("Uang keluar hari ini")
         .setContentText("Rp"+String.format(Locale.US,"%,.0f",out)+" · Kalo masi miskin jangan boros deh")
         .setStyle(new Notification.BigTextStyle().bigText("Uang keluar hari ini: Rp"+String.format(Locale.US,"%,.0f",out)+". Kalo masi miskin jangan boros deh"))
         .setAutoCancel(true);
        nm.notify(1001,b.build());
        Reminder.schedule(c);
    }
}
