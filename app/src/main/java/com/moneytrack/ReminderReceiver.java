package com.moneytrack;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "moneytrack_daily_v2";

    @Override public void onReceive(Context context, Intent intent) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(Reminder.PREF, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false)) return;

        // Schedule the next day first, so the recurring reminder survives notification issues.
        Reminder.schedule(context);

        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || !manager.areNotificationsEnabled()) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Pengingat harian MoneyTrack", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Ringkasan pengeluaran harian sesuai jadwal yang dipilih");
            manager.createNotificationChannel(channel);
            if (manager.getNotificationChannel(CHANNEL_ID).getImportance() == NotificationManager.IMPORTANCE_NONE) return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        DB db = new DB(context);
        double total = db.total("OUT", today);
        String amount = "Rp" + String.format(Locale.US, "%,.0f", total);

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent content = PendingIntent.getActivity(context, 77, open, flags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Uang keluar hari ini")
                .setContentText(amount + " · kalo masi miskin jangan boros deh")
                .setStyle(new Notification.BigTextStyle().bigText("Total pengeluaran hari ini: " + amount + "\nKalo masi miskin jangan boros deh"))
                .setContentIntent(content)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT);
        manager.notify(1001, builder.build());
    }
}
