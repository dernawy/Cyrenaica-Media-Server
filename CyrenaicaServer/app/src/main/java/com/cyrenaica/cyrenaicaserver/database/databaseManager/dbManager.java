package com.cyrenaica.cyrenaicaserver.database.databaseManager;

import android.content.Context;
import android.database.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.dbManager;

public class dbManager {

    final private Context context;
    private SQLiteDatabase database;
    private ServerSQLHelper sql_db;

    public dbManager(Context c) {
        this.context = c;
    }

    public dbManager open() throws SQLException {

        sql_db = ServerSQLHelper.getInstance(context);
        //dbHelper.encryptDbPassword(context);
        System.out.println("PASSWORD: " + sql_db.DB_PASSWORD);
        database = ServerSQLHelper.getInstance(context).getWritableDatabase(sql_db.DB_PASSWORD);
        return this;
    }
}
