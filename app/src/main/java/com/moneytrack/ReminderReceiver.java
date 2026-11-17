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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "moneytrack_daily_v2";

    // Pesan sengaja dipetakan berdasarkan tanggal kalender, bukan dipilih acak.
    private static final String[] DAILY_MESSAGES = {
        "GILA KAMUU!!!",
        "duit lu ilang kemana dah?",
        "Gaji sebulan buat apa, pinter banget",
        "Lu kerja capek-capek cuma buat beli barang nggak penting?",
        "Saldo lu makin kurus, gaya hidup lu makin gemuk",
        "Berhenti checkout jink lu cuma fomo",
        "Seminggu doang udah boros, asli kelakuan lu kayak gini?",
        "Katanya mau nabung, bacot doang ternyata",
        "Duit lu bukan tumbuh di pohon yaa jink",
        "Tanggal segini udah panik? Gobloknya konsisten juga",
        "lagi? Lu nggak punya hobi selain ngabisin duit?",
        "Barangnya nggak penting, nafsu belinya aja yang kegedean",
        "Diskon bukan alasan buat jadi miskin oke?",
        "Dua minggu berlalu, tabungan masih nol gaya tetap CEO hebattt",
        "Tengah bulan, saldo sekarat, selamat menikmati kebodohan sendiri",
        "Kemarin sok mau hemat, hari ini kambuh lagi",
        "Lu tuh butuh kontrol diri, bukan barang baru",
        "Berhenti beli kebahagiaan pakai uang yang nggak lu punya",
        "Coba hitung, berapa duit yang lu bakar buat hal nggak guna?",
        "nyawit ni orang",
        "Dompet udah nangis, masi gatau diri",
        "Mau checkout lagi? otaknya dimna?",
        "Lu miskin bukan karena kurang diskon, tapi banyak alesan",
        "Tinggal dikit duitnya, masih mau sok kaya?",
        "Gajian masih lama, goblok masih sama",
        "Nggak usah gengsi kalau emang belum mampu beli",
        "Tanggal tua nih, aman?",
        "Makan boleh, foya-foya jangan ya dek ya",
        "Dua hari lagi gajian. pls banget ini mah jangan sesi potret dulu di pinjol",
        "Besok gajian, stop kebiasaan buruk lu",
        "Sebulan kerja, duit habis, katanya mau jadi pewaris"
    };

    private static String messageForToday() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        return DAILY_MESSAGES[day - 1];
    }

    @Override public void onReceive(Context context, Intent intent) {
        android.content.SharedPreferences prefs =
                context.getSharedPreferences(Reminder.PREF, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false)) return;

        // Jadwalkan hari berikutnya sebelum menampilkan notifikasi.
        Reminder.schedule(context);

        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || !manager.areNotificationsEnabled()) return;

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat harian MoneyTrack",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Pengingat keuangan harian sesuai tanggal kalender");
            manager.createNotificationChannel(channel);
            NotificationChannel current = manager.getNotificationChannel(CHANNEL_ID);
            if (current != null && current.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                return;
            }
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String dailyMessage = messageForToday();

        DB db = new DB(context);
        double totalExpense = db.total("OUT", today);
        String amount = "Rp" + String.format(Locale.US, "%,.0f", totalExpense);

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent content = PendingIntent.getActivity(context, 77, open, flags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);

        String detail = "Tanggal " + dayOfMonth + " • Pengeluaran hari ini: " + amount
                + "\n" + dailyMessage;

        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("MoneyTrack • Pengingat harian")
                .setContentText(dailyMessage)
                .setStyle(new Notification.BigTextStyle().bigText(detail))
                .setContentIntent(content)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT);

        manager.notify(1001, builder.build());
    }
}
