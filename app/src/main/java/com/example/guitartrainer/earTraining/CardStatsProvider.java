package com.example.guitartrainer.earTraining;



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

import java.util.ArrayList;
import java.util.Map;

/**
 *
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class CardStatsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.guitartrainer.earTraining.CardStatsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/CardStats";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String CARD_ID_NAME = "cardIdName";
    static final String SUCCESS_PERC_NAME = "successPerc";
    static final String LEVEL_TYPE = "levelType";

    private static Map<String, String> cardStatsMap;

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "CardStats";
    static final String CARD_STATS_TABLE_NAME = "cardStats";
    static final int DATABASE_VERSION = 1;

    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + CARD_STATS_TABLE_NAME + "( " +
                    CARD_ID_NAME + " TEXT PRIMARY KEY, " +
                    LEVEL_TYPE + " INTEGER NOT NULL, " +
                    SUCCESS_PERC_NAME + " INTEGER" +
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
        DBWrapper dbHelper = new DBWrapper(context);
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
        return "com.example.guitartrainer.earTraining.CardStats";
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
        newCardStatsValues.put(CardStatsProvider.CARD_ID_NAME,
                cardStats.getCardUniqueId());

        newCardStatsValues.put(CardStatsProvider.SUCCESS_PERC_NAME,
                cardStats.getSuccessPerc());


        newCardStatsValues.put(CardStatsProvider.LEVEL_TYPE,
                cardStats.getLevelType().ordinal());

        return insertOrUpdate(CardStatsProvider.CONTENT_URI, newCardStatsValues);
    }


    public ProviderReturn.InsertOrUpdateReturn insertOrUpdate(Uri uri, ContentValues values){
        Uri uriOut = null;
        boolean isAnUpdate = existsCardId(values.getAsString(CARD_ID_NAME));

        if (!isAnUpdate) {
            uriOut = insert(uri, values);
            return ProviderReturn.InsertOrUpdateReturn.Inserted;
        }
        if (values.getAsInteger(SUCCESS_PERC_NAME) == -1) {
           values.remove(SUCCESS_PERC_NAME);
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
            int indexSuccessPerc = c.getColumnIndex(SUCCESS_PERC_NAME);
            int indexLevelType = c.getColumnIndex(LEVEL_TYPE);

            if (c.getCount() > 0){
                do{
                    out.add(new CardStats(
                            c.getString(indexCardId),
                            c.getInt(indexSuccessPerc),
                            GuessFunctionLevel.LevelType.values()[
                                    c.getInt(indexLevelType)]));
                }while(c.moveToNext());
            }
        }
        return out;
    }

    public int getSuccessPerc(String cardUniqueId){
        int successPerc = -1;
        String sqlQuery = "SELECT * FROM " + CARD_STATS_TABLE_NAME + " WHERE " + CARD_ID_NAME +
                " LIKE ?";
        String[] selectionArgs = {cardUniqueId};
        Cursor c = db.rawQuery(sqlQuery, selectionArgs);

        if (c!=null){
            c.moveToFirst();
            int indexCardId = c.getColumnIndex(CARD_ID_NAME);
            int indexSuccessPerc = c.getColumnIndex(SUCCESS_PERC_NAME);

            if (c.getCount() > 0){
                do{
                    successPerc = c.getInt(indexSuccessPerc);
                }while(c.moveToNext());
            }
        }
        return successPerc;
    }

}

