/*
 * @(#) ProgrammableMetronomePresetProvider.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 Provider for the SQL lite database that stores a programmable metronome preset.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class ProgrammableMetronomePresetsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.guitartrainer.ProgrammableMetronomePresetsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/ProgrammableMetronomePresets";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String PRESET_NAME = "presetName";
    static final String FROM_BPM = "fromBpm";
    static final String TO_BPM = "toBpm";
    static final String SECONDS = "seconds";
    static final String MODE = "mode";

    private static Map<String, String> presetsMap;

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "ProgrammableMetronomePresets";
    static final String PRESETS_TABLE_NAME = "presets";
    static final int DATABASE_VERSION = 1;

    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + PRESETS_TABLE_NAME + "( " +
              PRESET_NAME + " TEXT PRIMARY KEY, " +
              FROM_BPM + " INTEGER NOT NULL, " +
              TO_BPM + " INTEGER NOT NULL, " +
              SECONDS + " INTEGER NOT NULL, " +
              MODE + " INTEGER NOT NULL" +
              ")";

    private static class DBWrapper extends SQLiteOpenHelper {
        DBWrapper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + PRESETS_TABLE_NAME);
            onCreate(db);
        }
    }

    SQLiteQueryBuilder qb;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        DBWrapper dbHelper = new DBWrapper(context);
        db = dbHelper.getWritableDatabase();
        qb = new SQLiteQueryBuilder();
        qb.setProjectionMap(presetsMap);
        qb.setTables(PRESETS_TABLE_NAME);
        return (db == null) ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null,
                            sortOrder);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "com.example.guitartrainer.ProgrammableMetronomePreset";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
       long rowID = -1;
       rowID = db.insert(PRESETS_TABLE_NAME, null, values);

       if (rowID > 0) {
           Uri uriOut = ContentUris.withAppendedId(CONTENT_URI, rowID);
           return uriOut;
       } else {
           return null;
       }
    }

    public enum InsertOrUpdateReturn{
       Inserted,
       Updated,
       Error
    }

    public InsertOrUpdateReturn insertOrUpdate(Uri uri, ContentValues values){
        Uri uriOut = insert(uri, values);
        if (uriOut == null) { // maybe to update
            String presetNameToUpdate = (String) values.get(PRESET_NAME);
            String selection = PRESET_NAME + " LIKE ?";
            String[] selectionArgs = {presetNameToUpdate};
            int count = db.update(PRESETS_TABLE_NAME, values, selection, selectionArgs);
            if (count <= 0) {
                Log.d("TAG", "Error when inserting new preset");
                return InsertOrUpdateReturn.Error;
            } else {
                return InsertOrUpdateReturn.Updated;
            }
        } else {
            return InsertOrUpdateReturn.Inserted;
        }
    }

    public int deletePresetByName(Uri uri, String presetNameToDelete){
        String selection = PRESET_NAME + " LIKE ?";
        String[] selectionArgs = {presetNameToDelete};
        return delete(uri, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return db.delete(PRESETS_TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        return db.update(PRESETS_TABLE_NAME, values, selection, selectionArgs);
    }

    public long getNumberOfPresets(){
        return DatabaseUtils.queryNumEntries(db, PRESETS_TABLE_NAME);
    }

    public ArrayList<ProgrammableMetronomePreset> getOrderedPresets(){
        ArrayList<ProgrammableMetronomePreset> out = new ArrayList<>();

        String sqlQuery = "SELECT * FROM " + PRESETS_TABLE_NAME + " ORDER BY " + PRESET_NAME
                        + " ASC ";

        Cursor c = db.rawQuery(sqlQuery, null);

        if (c!=null){
            c.moveToFirst();
            int indexPresetName = c.getColumnIndex(PRESET_NAME);
            int indexFromBpm = c.getColumnIndex(FROM_BPM);
            int indexToBpm = c.getColumnIndex(TO_BPM);
            int indexSeconds = c.getColumnIndex(SECONDS);
            int indexMode = c.getColumnIndex(MODE);
            if (c.getCount() > 0){
                do{
                    out.add(new ProgrammableMetronomePreset(
                            c.getString(indexPresetName),
                            c.getInt(indexFromBpm),
                            c.getInt(indexToBpm),
                            c.getInt(indexSeconds),
                            ProgrammableIncrementBpm.ModeName.values()[c.getInt(indexMode)]));
                }while(c.moveToNext());
            }
        }
        return out;
    }

}
