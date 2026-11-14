package com.moneytrack;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context c,Intent i) { Reminder.schedule(c); }
}
