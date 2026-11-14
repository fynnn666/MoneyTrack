package com.moneytrack;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class DB extends SQLiteOpenHelper {
    public DB(Context c) { super(c, "moneytrack.db", null, 1); }
    public void onCreate(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE tx(id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, amount REAL NOT NULL, category TEXT NOT NULL, note TEXT, day TEXT NOT NULL)");
        d.execSQL("CREATE TABLE cats(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL)");
        for(String s : new String[]{"Jajan","Transportasi","Makan","Gaji"}) d.execSQL("INSERT OR IGNORE INTO cats(name) VALUES(?)", new Object[]{s});
    }
    public void onUpgrade(SQLiteDatabase d,int o,int n) {}
    public long addTx(String type,double amount,String cat,String note,String day) {
        ContentValues v=new ContentValues(); v.put("type",type); v.put("amount",amount); v.put("category",cat); v.put("note",note); v.put("day",day);
        return getWritableDatabase().insert("tx",null,v);
    }
    public void updateTx(long id,String type,double amount,String cat,String note,String day) {
        ContentValues v=new ContentValues(); v.put("type",type); v.put("amount",amount); v.put("category",cat); v.put("note",note); v.put("day",day);
        getWritableDatabase().update("tx",v,"id=?",new String[]{String.valueOf(id)});
    }
    public void deleteTx(long id) { getWritableDatabase().delete("tx","id=?",new String[]{String.valueOf(id)}); }
    public Cursor allTx() { return getReadableDatabase().rawQuery("SELECT id,type,amount,category,note,day FROM tx ORDER BY day DESC,id DESC",null); }
    public Cursor txForMonth(String month) { return getReadableDatabase().rawQuery("SELECT type, SUM(amount) FROM tx WHERE substr(day,1,7)=? GROUP BY type",new String[]{month}); }
    public double total(String type,String dayPrefix) {
        Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM tx WHERE type=? AND day LIKE ?",new String[]{type,dayPrefix+"%"});
        double v=0; if(c.moveToFirst()) v=c.getDouble(0); c.close(); return v;
    }
    public ArrayList<String> categories() {
        ArrayList<String> a=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT name FROM cats ORDER BY name",null);
        while(c.moveToNext()) a.add(c.getString(0)); c.close(); return a;
    }
    public boolean addCategory(String s) {
        ContentValues v=new ContentValues(); v.put("name",s);
        return getWritableDatabase().insert("cats",null,v)>0;
    }
    public void deleteCategory(String s) { getWritableDatabase().delete("cats","name=?",new String[]{s}); }
}
