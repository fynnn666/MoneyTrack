package com.moneytrack;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.os.Build;
import android.view.*;
import android.widget.*;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DB db; LinearLayout base, content; int bg=Color.rgb(8,13,16), panel=Color.rgb(22,30,31), white=Color.rgb(245,250,248), muted=Color.rgb(157,174,168), green=Color.rgb(0,220,133), red=Color.rgb(255,83,104);
    String date(){ return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()); }
    String month(){ return date().substring(0,7); }
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg); getWindow().getDecorView().setSystemUiVisibility(0); askNotif(); home();}
    @Override protected void onResume(){super.onResume(); if(db!=null) Reminder.schedule(this);}
    void askNotif(){ if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},9); }
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(4),dp(7),dp(4),dp(7));return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(12),dp(16),dp(18));return l;}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    void shell(String title){base=column();base.setBackgroundColor(bg);TextView h=text(title,24,white);h.setTypeface(null,Typeface.BOLD);h.setPadding(dp(2),dp(8),dp(2),dp(14));base.addView(h);ScrollView sc=new ScrollView(this);content=column();content.setPadding(0,dp(4),0,dp(14));sc.addView(content);base.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(4),dp(5),dp(4),dp(5));nav.setBackground(shape(Color.rgb(15,22,23),22));String[][] items={{"⌂","Beranda"},{"⇄","Transaksi"},{"▥","Statistik"},{"☻","Profil"}};for(String[] item:items){LinearLayout tab=new LinearLayout(this);tab.setGravity(Gravity.CENTER);tab.setOrientation(1);TextView ic=text(item[0],19,green);ic.setGravity(Gravity.CENTER);TextView tx=text(item[1],10,muted);tx.setGravity(Gravity.CENTER);tab.addView(ic);tab.addView(tx);tab.setOnClickListener(v->navigate(item[1]));nav.addView(tab,new LinearLayout.LayoutParams(0,dp(54),1));}base.addView(nav);setContentView(base);}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(white);b.setAllCaps(false);b.setTextSize(14);b.setBackground(shape(Color.rgb(28,43,40),16));b.setPadding(dp(12),dp(8),dp(12),dp(8));return b;}
    TextView card(String s){TextView t=text(s,16,white);t.setBackground(shape(panel,18));t.setPadding(dp(16),dp(15),dp(16),dp(15));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);content.addView(t,p);return t;}
    void navigate(String s){switch(s){case "Beranda":home();break;case "Transaksi":history();break;case "Statistik":report();break;case "Profil":profile();break;case "Home":home();break;case "Masuk":form("IN",null);break;case "Keluar":form("OUT",null);break;case "Riwayat":history();break;case "Kategori":categories();break;case "Laporan":report();break;case "Pengingat":reminder();}}
    void home(){shell("MoneyTrack");double in=db.total("IN",month()),out=db.total("OUT",month());double allIn=db.total("IN",""),allOut=db.total("OUT","");TextView balance=card("TOTAL SALDO\nRp "+money(allIn-allOut)+"\n↗ Kelola uang lebih teratur");balance.setTextSize(21);balance.setTypeface(null,Typeface.BOLD);balance.setBackground(shape(Color.rgb(0,105,68),22));card("PEMASUKAN BULAN INI\nRp "+money(in)+"     ↑");card("PENGELUARAN BULAN INI\nRp "+money(out)+"     ↓");card("RINGKASAN BULAN INI\nSisa: Rp "+money(in-out));TextView section=text("Transaksi terbaru",18,white);section.setTypeface(null,Typeface.BOLD);content.addView(section);Cursor c=db.allTx();int count=0;while(c.moveToNext()&&count<4){String type=c.getString(1);TextView row=card((type.equals("IN")?"↗  ":"↘  ")+c.getString(3)+"\n"+(type.equals("IN")?"+":"-")+"Rp "+money(c.getDouble(2))+" · "+c.getString(5));row.setTextColor(type.equals("IN")?green:red);count++;}c.close();Button a=button("+ Tambah pemasukan");a.setOnClickListener(v->form("IN",null));content.addView(a);Button z=button("− Tambah pengeluaran");z.setOnClickListener(v->form("OUT",null));content.addView(z);}
    String money(double n){return String.format(Locale.US,"%,.0f",n).replace(',','.');}
    void form(String type, Cursor edit){shell(type.equals("IN")?"Pemasukan":"Pengeluaran"); final long id=edit==null?-1:edit.getLong(0); EditText amount=new EditText(this);amount.setSingleLine();amount.setHint("Nominal (contoh 25000)");amount.setInputType(8194);amount.setTextColor(white);amount.setHintTextColor(muted);content.addView(amount); if(edit!=null)amount.setText(String.valueOf(edit.getDouble(2)));
        ArrayList<String> cats=db.categories();Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cats));content.addView(sp);if(edit!=null){int ix=cats.indexOf(edit.getString(3));if(ix>=0)sp.setSelection(ix);}
        EditText note=new EditText(this);note.setHint("Keterangan (opsional)");note.setTextColor(white);note.setHintTextColor(muted);content.addView(note);if(edit!=null)note.setText(edit.getString(4));
        EditText day=new EditText(this);day.setHint("Tanggal YYYY-MM-DD");day.setText(date());day.setTextColor(white);content.addView(day);if(edit!=null)day.setText(edit.getString(5));
        Button save=button("Simpan transaksi");content.addView(save);save.setOnClickListener(v->{try{double val=Double.parseDouble(amount.getText().toString());if(val<=0)throw new Exception();String cat=sp.getSelectedItem().toString();if(id<0)db.addTx(type,val,cat,note.getText().toString(),day.getText().toString());else db.updateTx(id,type,val,cat,note.getText().toString(),day.getText().toString());home();}catch(Exception e){new AlertDialog.Builder(this).setMessage("Nominal harus lebih dari 0 dan tanggal harus valid.").setPositiveButton("OK",null).show();}});
    }
    void history(){shell("Riwayat transaksi");Cursor c=db.allTx();String current="";while(c.moveToNext()){long id=c.getLong(0);String type=c.getString(1);String d=c.getString(5);if(!d.equals(current)){current=d;card("📅 "+d);}String sign=type.equals("IN")?"+":"-";TextView row=card((type.equals("IN")?"PEMASUKAN ":"PENGELUARAN ")+sign+"Rp"+money(c.getDouble(2))+"\n"+c.getString(3)+" · "+c.getString(4));row.setTextColor(type.equals("IN")?green:red);LinearLayout actions=new LinearLayout(this);Button edit=button("Edit"),del=button("Hapus");actions.addView(edit,new LinearLayout.LayoutParams(0,dp(44),1));actions.addView(del,new LinearLayout.LayoutParams(0,dp(44),1));content.addView(actions);edit.setOnClickListener(v->{Cursor x=db.allTx();while(x.moveToNext())if(x.getLong(0)==id){form(type,x);break;}x.close();});del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Hapus transaksi ini?").setNegativeButton("Batal",null).setPositiveButton("Hapus",(a,b)->{db.deleteTx(id);history();}).show());}c.close();}
    void categories(){shell("Kategori");EditText name=new EditText(this);name.setHint("Nama kategori baru");name.setTextColor(white);name.setHintTextColor(muted);content.addView(name);Button add=button("Tambah kategori");content.addView(add);add.setOnClickListener(v->{String s=name.getText().toString().trim();if(!s.isEmpty()&&db.addCategory(s))categories();else card("Kategori kosong atau sudah ada.");});for(String s:db.categories()){LinearLayout r=new LinearLayout(this);r.addView(text(s,16,white),new LinearLayout.LayoutParams(0,-2,1));Button del=button("Hapus");r.addView(del);content.addView(r);del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Hapus kategori "+s+"? Transaksi lama tetap menyimpan nama kategori.").setNegativeButton("Batal",null).setPositiveButton("Hapus",(a,b)->{db.deleteCategory(s);categories();}).show());}}
    void report(){shell("Laporan bulanan");double in=db.total("IN",month()),out=db.total("OUT",month());card(month());card("Pemasukan: Rp"+money(in));card("Pengeluaran: Rp"+money(out));card("Selisih: Rp"+money(in-out));card("Grafik perbandingan");bar("Pemasukan",in,green);bar("Pengeluaran",out,red);}
    void bar(String label,double val,int color){LinearLayout r=new LinearLayout(this);r.setOrientation(1);r.addView(text(label+" · Rp"+money(val),14,white));View v=new View(this);v.setBackgroundColor(color);int width=(int)(getResources().getDisplayMetrics().widthPixels*.75*Math.min(1,val/Math.max(1,Math.max(db.total("IN",month()),db.total("OUT",month())))));r.addView(v,new LinearLayout.LayoutParams(Math.max(dp(4),width),dp(12)));content.addView(r);}
    void profile(){shell("Profil");card("MoneyTrack\nPengelola keuangan pribadi");Button cats=button("Kelola kategori");content.addView(cats);cats.setOnClickListener(v->categories());Button rem=button("Pengaturan notifikasi");content.addView(rem);rem.setOnClickListener(v->reminder());Button hist=button("Riwayat transaksi");content.addView(hist);hist.setOnClickListener(v->history());card("MoneyTrack · Android 10+");}
    void reminder(){
    shell("Pengingat harian");
    SharedPreferences p=getSharedPreferences(Reminder.PREF,0);

    Switch sw=new Switch(this);
    sw.setText("Aktifkan pengingat harian");
    sw.setTextColor(white);
    sw.setChecked(p.getBoolean("enabled",false));
    content.addView(sw);

    TimePicker tp=new TimePicker(this);
    tp.setIs24HourView(true);
    tp.setHour(p.getInt("hour",20));
    tp.setMinute(p.getInt("minute",0));
    content.addView(tp);

    Button save=button("Simpan jadwal pengingat");
    content.addView(save);

    save.setOnClickListener(v->{
        boolean enabled=sw.isChecked();
        p.edit()
            .putBoolean("enabled",enabled)
            .putInt("hour",tp.getHour())
            .putInt("minute",tp.getMinute())
            .apply();

        if(enabled){
            askNotif();
            Reminder.schedule(this);

            if(Build.VERSION.SDK_INT>=31){
                AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
                if(am!=null&&!am.canScheduleExactAlarms()){
                    new AlertDialog.Builder(this)
                        .setTitle("Izinkan alarm tepat waktu")
                        .setMessage("Agar pengingat lebih tepat sesuai jam pilihan, izinkan MoneyTrack menjadwalkan alarm di halaman pengaturan Android.\nJika dilewati, notifikasi tetap dijadwalkan tetapi bisa terlambat.")
                        .setNegativeButton("Nanti",(d,w)->{})
                        .setPositiveButton("Buka pengaturan",(d,w)->{
                            try{
                                Intent it=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:"+getPackageName()));
                                startActivity(it);
                            }catch(Exception ex){}
                        }).show();
                    return;
                }
            }
        }else{
            Reminder.cancel(this);
        }

        new AlertDialog.Builder(this)
            .setMessage(enabled?"Jadwal disimpan.\nPastikan izin notifikasi aktif.":"Pengingat dinonaktifkan.")
            .setPositiveButton("OK",null).show();
    });

    card("Notifikasi harian\nUang keluar hari ini + total pengeluaran hari ini\nkalo masi miskin jangan boros deh");

    if(Build.VERSION.SDK_INT>=31){
        AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
        card("Izin alarm tepat waktu: "+
            (am!=null&&am.canScheduleExactAlarms()?"Aktif":"Belum aktif"));
    }

    NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    card("Izin notifikasi: "+
        (nm!=null&&nm.areNotificationsEnabled()?"Aktif":"Perlu diaktifkan di Pengaturan Android"));
}

}
