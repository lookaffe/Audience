package intersistemi.it.afp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by orazio on 21/11/2016.
 */

public class DataBaseDbHelper extends SQLiteOpenHelper
{
    private Context context;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_USER_ENTRIES =
            "CREATE TABLE " + UserAlarmBaseColumns.UserAlarmEntry.TABLE_NAME + " (" +
                    UserAlarmBaseColumns.UserAlarmEntry._ID + " INTEGER PRIMARY KEY," +
                    UserAlarmBaseColumns.UserAlarmEntry.COLUMN_NAME_TIME_IN_MILLIS + TEXT_TYPE + " )";
    private static final String SQL_CREATE_CONFIGURATIONS_ENTRIES =
            "CREATE TABLE " + ConfigurationsBaseColumns.ConfigurationsEntry.TABLE_NAME + " (" +
                    ConfigurationsBaseColumns.ConfigurationsEntry._ID + " INTEGER PRIMARY KEY," +
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_UPLOAD_URL_REGISTRATION + TEXT_TYPE +", "+
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_SPLIT + TEXT_TYPE +", "+
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_REGISTRATION + TEXT_TYPE + " )";

    private static final String SQL_DELETE_USER_ENTRIES =
            "DROP TABLE IF EXISTS " + UserAlarmBaseColumns.UserAlarmEntry.TABLE_NAME;
    private static final String SQL_DELETE_CONFIGURATIONS_ENTRIES =
            "DROP TABLE IF EXISTS " + ConfigurationsBaseColumns.ConfigurationsEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "UserMonitor.db";

    public DataBaseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_ENTRIES);
        db.execSQL(SQL_CREATE_CONFIGURATIONS_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_USER_ENTRIES);
        db.execSQL(SQL_DELETE_CONFIGURATIONS_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }







    //**************************
    //Gestisce le configurazioni
    //**************************
    /*public static HashMap getConfiguration(Context context) {
        HashMap configuration = new HashMap();
        try {
            DataBaseDbHelper mDbHelper = new DataBaseDbHelper(context);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] projection = {
                    ConfigurationsBaseColumns.ConfigurationsEntry._ID,
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_UPLOAD_URL_REGISTRATION,
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_SPLIT,
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_REGISTRATION

            };
            String selection = "";
            String[] selectionArgs = {}; //Where Clause
            String sortOrder = ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_UPLOAD_URL_REGISTRATION + " DESC";
            Cursor cursor = db.query(
                    ConfigurationsBaseColumns.ConfigurationsEntry.TABLE_NAME,  // The table to query
                    projection,                                     // The columns to return
                    selection,                                      // The columns for the WHERE clause
                    selectionArgs,                                  // The values for the WHERE clause
                    null,                                           // don't group the rows
                    null,                                           // don't filter by row groups
                    sortOrder                                       // The sort order
            );
            if (cursor.moveToNext()) {
                configuration.put("COLUMN_NAME_UPLOAD_URL_REGISTRATION",cursor.getString(1));
                configuration.put("COLUMN_NAME_REC_INTERVAL_SPLIT",cursor.getString(2));
                configuration.put("COLUMN_NAME_REC_INTERVAL_REGISTRATION",cursor.getString(3));
            }
        }
        catch(Throwable t) {
            Toast.makeText(context, "Errore "+t.getMessage(), Toast.LENGTH_SHORT).show(); //MainActivity.this
            //t.printStackTrace();
        }
        return configuration;
    }*/
    //**************************
    //Gestisce le configurazioni
    //**************************

}
