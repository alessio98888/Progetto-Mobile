package com.example.guitartrainer.fretboardVisualization;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.guitartrainer.ProviderReturn;
import com.example.guitartrainer.earTraining.GuessFunctionLevel;

import java.util.ArrayList;
import java.util.Map;

public class PlayFunctionsCardStatsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.guitartrainer.fretboardVisualization.PlayFunctionsCardStatsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/PlayFunctionsCardStatsProvider";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String CARD_ID_NAME = "cardIdName";
    static final String SUCCESS_SECONDS_NAME = "successSeconds";
    static final String LEVEL_TYPE = "levelType";

    private static Map<String, String> cardStatsMap;

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "PlayFunctionsCardStats";
    static final String CARD_STATS_TABLE_NAME = "cardStats";
    static final int DATABASE_VERSION = 1;

    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + CARD_STATS_TABLE_NAME + "( " +
                    CARD_ID_NAME + " TEXT PRIMARY KEY, " +
                    LEVEL_TYPE + " INTEGER NOT NULL, " +
                    SUCCESS_SECONDS_NAME + " INTEGER" +
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
            db.execSQL("DROP TABLE IF EXISTS " + CARD_STATS_TABLE_NAME);
            onCreate(db);
        }
    }

    SQLiteQueryBuilder qb;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        com.example.guitartrainer.fretboardVisualization.PlayFunctionsCardStatsProvider.DBWrapper dbHelper =
                new com.example.guitartrainer.fretboardVisualization.PlayFunctionsCardStatsProvider.DBWrapper(context);
        db = dbHelper.getWritableDatabase();
        qb = new SQLiteQueryBuilder();
        qb.setProjectionMap(cardStatsMap);
        qb.setTables(CARD_STATS_TABLE_NAME);
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
        return "com.example.guitartrainer.fretboardVisualization.PlayFunctionsCardStatsProvider";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)  {
        long rowID = -1;
        rowID = db.insert(CARD_STATS_TABLE_NAME, null, values);


        if (rowID > 0) {
            Uri uriOut = ContentUris.withAppendedId(CONTENT_URI, rowID);
            return uriOut;
        } else {
            return null;
        }
    }

    public boolean existsCardId(String cardId){

        boolean exists = false;
        String sqlQuery = "SELECT * FROM " + CARD_STATS_TABLE_NAME + " WHERE " + CARD_ID_NAME +
                " LIKE ?";
        String[] selectionArgs = {cardId};
        Cursor c = db.rawQuery(sqlQuery, selectionArgs);

        if (c!=null){
            c.moveToFirst();
            if (c.getCount() > 0){
                do{
                    exists = true;
                    break;
                }while(c.moveToNext());
            }
        }
        return exists;
    }


    public ProviderReturn.InsertOrUpdateReturn insertOrUpdateCard(CardStats cardStats){
        ContentValues newCardStatsValues = new ContentValues();
        newCardStatsValues.put(CARD_ID_NAME,
                cardStats.getCardUniqueId());

        newCardStatsValues.put(SUCCESS_SECONDS_NAME,
                cardStats.getSuccessSeconds());


        newCardStatsValues.put(LEVEL_TYPE,
                cardStats.getLevelType().ordinal());

        return insertOrUpdate(CONTENT_URI, newCardStatsValues);
    }


    public ProviderReturn.InsertOrUpdateReturn insertOrUpdate(Uri uri, ContentValues values){
        Uri uriOut = null;
        boolean isAnUpdate = existsCardId(values.getAsString(CARD_ID_NAME));

        if (!isAnUpdate) {
            uriOut = insert(uri, values);
            return ProviderReturn.InsertOrUpdateReturn.Inserted;
        }
        if (values.getAsInteger(SUCCESS_SECONDS_NAME) == -1) {
            values.remove(SUCCESS_SECONDS_NAME);
        }
        String cardToUpdate = (String) values.get(CARD_ID_NAME);
        String selection = CARD_ID_NAME + " LIKE ?";
        String[] selectionArgs2 = {cardToUpdate};
        int count = db.update(CARD_STATS_TABLE_NAME, values, selection, selectionArgs2);
        if (count <= 0) {
            Log.d("TAG", "Error when inserting new card stats");
            return ProviderReturn.InsertOrUpdateReturn.Error;
        }
        return ProviderReturn.InsertOrUpdateReturn.Updated;
    }

    public int deleteCardStatsByName(Uri uri, String cardId){
        String selection = CARD_ID_NAME + " LIKE ?";
        String[] selectionArgs = {cardId};
        return delete(uri, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return db.delete(CARD_STATS_TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        return db.update(CARD_STATS_TABLE_NAME, values, selection, selectionArgs);
    }

    public long getNumberOfCardStats(){
        return DatabaseUtils.queryNumEntries(db, CARD_STATS_TABLE_NAME);
    }

    public ArrayList<CardStats> getCardStats(){
        ArrayList<CardStats> out = new ArrayList<>();

        String sqlQuery = "SELECT * FROM " + CARD_STATS_TABLE_NAME;

        Cursor c = db.rawQuery(sqlQuery, null);

        if (c!=null){
            c.moveToFirst();
            int indexCardId = c.getColumnIndex(CARD_ID_NAME);
            int indexSuccessSeconds = c.getColumnIndex(SUCCESS_SECONDS_NAME);
            int indexLevelType = c.getColumnIndex(LEVEL_TYPE);

            if (c.getCount() > 0){
                do{
                    out.add(new CardStats(
                            c.getString(indexCardId),
                            c.getInt(indexSuccessSeconds),
                            PlayFunctionsLevel.LevelType.values()[
                                    c.getInt(indexLevelType)]));
                }while(c.moveToNext());
            }
        }
        return out;
    }

    public int getSuccessSeconds(String cardUniqueId){
        int successSeconds = -1;
        String sqlQuery = "SELECT * FROM " + CARD_STATS_TABLE_NAME + " WHERE " + CARD_ID_NAME +
                " LIKE ?";
        String[] selectionArgs = {cardUniqueId};
        Cursor c = db.rawQuery(sqlQuery, selectionArgs);

        if (c!=null){
            c.moveToFirst();
            int indexCardId = c.getColumnIndex(CARD_ID_NAME);
            int indexSuccessSeconds = c.getColumnIndex(SUCCESS_SECONDS_NAME);

            if (c.getCount() > 0){
                do{
                    successSeconds = c.getInt(indexSuccessSeconds);
                }while(c.moveToNext());
            }
        }
        return successSeconds;
    }

}
