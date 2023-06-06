package com.epmi_edu.terreplurielle.MVC.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DAOModel extends BasicModel {
    private SQLiteDatabase database;
    private String tableName;
    private Functions.ErrorDispatcher errorDispatcheror;
    private Context context;

    public DAOModel(BasicActivity controller, Context context, String dbName, String tableName, final String createTableQuery, int version,
                    Functions.ErrorDispatcher errorDispatcheror) {

        super(controller);
        this.context = context;
        this.errorDispatcheror = errorDispatcheror;

        try {
            this.tableName = tableName;
            SQLiteOpenHelper sqLiteHelper = new SQLiteOpenHelper(context, dbName, null, version) {

                public void onCreate(final SQLiteDatabase db) {
                    db.execSQL(createTableQuery);
                }

                public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

                }
            };

            database = sqLiteHelper.getWritableDatabase();
            while (database.isDbLockedByCurrentThread()) {
                //database is locked, keep looping
            }
        } catch (Exception e) {
            catchError(e);
        }
    }


    protected void finalize() throws Throwable {
        try {
            if (database != null) database.close();
            database = null;

            super.finalize();
        } catch (Exception e) {
            catchError(e);
        }
    }

    public long insert(ContentValues row) {
        long ret = 0;
        try {
            ret = database.insert(tableName, null, row);
        } catch (Exception e) {
            catchError(e);
        }

        return ret;
    }

    public long insert(HashMap<String, String> row) {
        ContentValues row1 = new ContentValues();
        for (Map.Entry<String, String> entry : row.entrySet()) {
            row1.put(entry.getKey(), entry.getValue());
        }

        long ret = 0;
        try {
            ret = database.insert(tableName, null, row1);
        } catch (Exception e) {
            catchError(e);
        }

        return ret;
    }

    public void insert(List<ContentValues> rows) {
        try {
            String sql = "INSERT INTO " + tableName;
            String fields = "";
            String valueSet = "";

            int row_count = rows.size();
            for (int i = 0; i < row_count; i++) {
                ContentValues row = rows.get(i);

                String valueString = "";

                Set<Map.Entry<String, Object>> values = row.valueSet();
                for (Map.Entry<String, Object> entry : values) {
                    if (i == 0) fields += (fields.isEmpty() ? "" : ",") + entry.getKey();
                    valueString = (valueString.isEmpty() ? "" : ",") + entry.getValue().toString();
                }

                valueSet += (valueSet.isEmpty() ? "" : ",") + "(" + valueString + ")";
            }

            sql += "(" + fields + ")" + "VALUES" + valueSet;
            database.execSQL(sql);
        } catch (Exception e) {
            catchError(e);
        }
    }

    public long update(ContentValues whereEntry, ContentValues columns) {
        long ret = 0;
        try {
            String[] whereKeyValue = getKeyValue(whereEntry);
            ret = database.update(tableName, columns, whereKeyValue[0] + " = " + whereKeyValue[1], null);
        } catch (Exception e) {
            catchError(e);
        }

        return ret;
    }

    public int delete(ContentValues whereEntry) {
        int ret = 0;
        try {
            String[] whereKeyValue = getKeyValue(whereEntry);
            ret = database.delete(tableName, whereKeyValue[0] + " = ?", new String[]{whereKeyValue[1]});
        } catch (Exception e) {
            catchError(e);
        }

        return ret;
    }

    public ContentValues getRow(ContentValues whereEntry) {
        ContentValues contentValues = null;
        try {
            String[] whereKeyValue = getKeyValue(whereEntry);
            Cursor c = database.query(tableName, new String[]{"*"}, whereKeyValue[0] + " LIKE " + whereKeyValue[1],
                    null, null, null, whereKeyValue[0]);

            if (c.getCount() > 0) {
                c.moveToFirst();
                contentValues = getCursorData(c);
            }

            c.close();
        } catch (Exception e) {
            catchError(e);
        }

        return contentValues;
    }

    public ArrayList<ContentValues> getAll(String[] columns, String orderBy) {
        ArrayList<ContentValues> list = new ArrayList();
        try {
            Cursor c = database.query(tableName, columns, null, null, null, null, orderBy);
            if (c.getCount() > 0) {
                c.moveToFirst();
                do {
                    list.add(getCursorData(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            catchError(e);
        }

        return list;
    }

    public long rowCount() {
        long count = 0;
        try {
            count = DatabaseUtils.queryNumEntries(database, tableName);
            return count;
        } catch (Exception e) {
            catchError(e);
        }

        return count;
    }

    public void empty() {
        try {
            //database.execSQL("TRUNCATE " + tableName);
            database.execSQL("DELETE FROM " + tableName);
        } catch (Exception e) {
            catchError(e);
        }
    }

    private String[] getKeyValue(ContentValues contentValues) {
        String key = "";
        String value = "";
        Set<Map.Entry<String, Object>> valueSet = contentValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            key = entry.getKey();
            value = entry.getValue().toString();
        }

        return new String[]{key, value};
    }

    private ContentValues getCursorData(Cursor c) {
        ContentValues contentValues = new ContentValues();
        if (c.getCount() > 0) {
            int columnCount = c.getColumnCount();
            for (int i = 0; i < columnCount; i++)
                contentValues.put(c.getColumnName(i), c.getString(i));
        }

        return contentValues;
    }

    private void catchError(Exception e) {
        if (errorDispatcheror == null) new ErrorReporting(context, e, this.getClass().getName());
        else errorDispatcheror.error(e.getMessage());
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }
}