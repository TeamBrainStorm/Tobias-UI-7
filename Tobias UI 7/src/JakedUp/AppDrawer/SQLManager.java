package JakedUp.AppDrawer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLManager extends SQLiteOpenHelper {

    public static final String CREATE_HIDDEN_APPS_DB =
            "CREATE TABLE IF NOT EXISTS hiddenApps (packagename TEXT NOT NULL,activityname TEXT NOT NULL)";
    public static final String DROP_HIDDEN_APPS_DB =
            "DROP TABLE IF EXISTS hiddenApps";
    public static final String CREATE_EDITED_APPS_DB =
            "CREATE TABLE IF NOT EXISTS editedApps (activityname TEXT NOT NULL, icon TEXT NOT NULL, name TEXT NOT NULL)";
    public static final String DROP_EDITED_APPS_DB =
            "DROP TABLE IF EXISTS editedApps";
    public static final String CREATE_APP_USES_DB =
            "CREATE TABLE IF NOT EXISTS appUses (activityname TEXT NOT NULL, uses INTEGER NOT NULL)";
    public static final String DROP_APP_USES_DB =
            "DROP TABLE IF EXISTS appUses";
    public static final String CREATE_FOLDERS_DB =
            "CREATE TABLE IF NOT EXISTS folders (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, icon TEXT, apps TEXT)";
    public static final String DROP_FOLDERS_DB =
            "DROP TABLE IF EXISTS folders";
    public static final String DB_NAME = "AppDrawer_DB";
    private static final int DB_VERSION = 1;

    public SQLManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_HIDDEN_APPS_DB);
        db.execSQL(CREATE_EDITED_APPS_DB);
        db.execSQL(CREATE_APP_USES_DB);
        db.execSQL(CREATE_FOLDERS_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        resetDB(db);
    }
    
    public void resetDB(SQLiteDatabase db)
    {
        db.execSQL(DROP_HIDDEN_APPS_DB);
        db.execSQL(DROP_EDITED_APPS_DB);
        db.execSQL(DROP_APP_USES_DB);
        db.execSQL(DROP_FOLDERS_DB);
        onCreate(db);
    }

}
