package com.autoparts.catalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "parts_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_PARTS = "parts";

    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PARTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTS);
        onCreate(db);
    }

    public long insertPart(Part part) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, part.getTitle());
        cv.put(COL_DESCRIPTION, part.getDescription());
        cv.put(COL_DATE, part.getDate());
        return db.insert(TABLE_PARTS, null, cv);
    }

    public int updatePart(Part part) {
        if (part.getId() < 0) return 0;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, part.getTitle());
        cv.put(COL_DESCRIPTION, part.getDescription());
        cv.put(COL_DATE, part.getDate());
        return db.update(TABLE_PARTS, cv, COL_ID + " = ?",
                new String[]{String.valueOf(part.getId())});
    }

    public int deletePart(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PARTS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Part getPart(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PARTS, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) {
            Part p = cursorToPart(c);
            c.close();
            return p;
        }
        c.close();
        return null;
    }

    public List<Part> getAllParts() {
        List<Part> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PARTS, null, null, null, null, null, COL_ID + " DESC");
        while (c.moveToNext()) {
            list.add(cursorToPart(c));
        }
        c.close();
        return list;
    }

    private Part cursorToPart(Cursor c) {
        return new Part(
                c.getLong(c.getColumnIndex(COL_ID)),
                c.getString(c.getColumnIndex(COL_TITLE)),
                c.getString(c.getColumnIndex(COL_DESCRIPTION)),
                c.getString(c.getColumnIndex(COL_DATE))
        );
    }
}
