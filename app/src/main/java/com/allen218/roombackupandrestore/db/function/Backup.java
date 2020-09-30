package com.allen218.roombackupandrestore.db.function;

import android.database.Cursor;
import android.util.Log;

import androidx.room.RoomDatabase;

import com.allen218.roombackupandrestore.db.OnWorkFinishListener;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Backup {
    private static ArrayList<String> SQLITE_TABLES = new ArrayList<String>() {{
        add("android_metadata");
        add("room_master_table");
        add("sqlite_sequence");
    }};

    private static boolean isInSQLiteTables(String table) {
        return SQLITE_TABLES.contains(table);
    }

    private static String STRING_FOR_NULL_VALUE = "!!!string_for_null_value!!!";

    public static class Init {
        private RoomDatabase database;
        private String path;
        private String fileName;
        private String secretKey;
        private OnWorkFinishListener onWorkFinishListener;
        private Moshi moshi = new Moshi.Builder().build();

        public Init database(RoomDatabase database) {
            this.database = database;
            return this;
        }

        public Init path(String path) {
            this.path = path;
            return this;
        }

        public Init fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Init secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Init onWorkFinishListener(OnWorkFinishListener onWorkFinishListener) {
            this.onWorkFinishListener = onWorkFinishListener;
            return this;
        }

        public void execute() {
            if (database == null) {
                onWorkFinishListener.onFinished(false, "Database not specified");
                return;
            }
            if (path == null) {
                onWorkFinishListener.onFinished(false, "Backup path not specified");
                return;
            }
            if (fileName == null) {
                onWorkFinishListener.onFinished(false, "Backup file name not specified");
                return;
            }
            Cursor tablesCursor = database.query("SELECT name FROM sqlite_master WHERE type='table'", null);
            ArrayList<String> tables = new ArrayList<>();
            HashMap<String, ArrayList<HashMap<String, String>>> dbData = new HashMap<>();
            if (tablesCursor.moveToFirst()) {
                while (!tablesCursor.isAfterLast()) {
                    tables.add(tablesCursor.getString(0));
                    tablesCursor.moveToNext();
                }
                for (String table : tables) {
                    if (isInSQLiteTables(table)) {
                        continue;
                    }
                    ArrayList<HashMap<String, String>> rows = new ArrayList<>();
                    Cursor rowsCursor = database.query("select * from " + table, null);
                    Cursor tableSqlCursor = database.query("select sql from sqlite_master where name= \'" + table + "\'", null);
                    tableSqlCursor.moveToFirst();
                    String tableSql = tableSqlCursor.getString(0);
                    tableSql = tableSql.substring(tableSql.indexOf("("));
                    String aic = "";
                    if (tableSql.contains("AUTOINCREMENT")) {
                        tableSql = tableSql.substring(0, tableSql.indexOf("AUTOINCREMENT"));
                        tableSql = tableSql.substring(0, tableSql.lastIndexOf("`"));
                        aic = tableSql.substring(tableSql.lastIndexOf("`") + 1);
                    }
                    if (rowsCursor.moveToFirst()) {
                        do {
                            int columnCount = rowsCursor.getColumnCount();
                            HashMap<String, String> column = new HashMap<>();
                            for (int i = 0; i < columnCount; i++) {
                                String columnName = rowsCursor.getColumnName(i);
                                if (columnName.equals(aic)) {
                                    continue;
                                }
                                column.put(columnName, (rowsCursor.getString(i) != null) ? rowsCursor.getString(i) : STRING_FOR_NULL_VALUE);
                            }
                            rows.add(column);
                        } while (rowsCursor.moveToNext());
                    }
                    dbData.put(table, rows);
                }

                JsonAdapter<HashMap<String, ArrayList<HashMap<String, String>>>> jsonAdapter = new JsonAdapter<HashMap<String, ArrayList<HashMap<String, String>>>>() {
                    @Override
                    public HashMap<String, ArrayList<HashMap<String, String>>> fromJson(JsonReader reader) throws IOException {
                        return null;
                    }

                    @Override
                    public void toJson(JsonWriter writer, HashMap<String, ArrayList<HashMap<String, String>>> value) throws IOException {
                        writer.beginObject();
                        Iterator<Map.Entry<String, ArrayList<HashMap<String, String>>>> iterator = value.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, ArrayList<HashMap<String, String>>> next = iterator.next();
                            writer.name(next.getKey());
                            writer.beginArray();
                            ArrayList<HashMap<String, String>> rowValue = next.getValue();
                            for (int i = 0; i < rowValue.size(); i++) {
                                writer.beginObject();
                                HashMap<String, String> columnValue = rowValue.get(i);
                                Iterator<Map.Entry<String, String>> columnIterator = columnValue.entrySet().iterator();
                                while (columnIterator.hasNext()) {
                                    Map.Entry<String, String> columnNext = columnIterator.next();
                                    writer.name(columnNext.getKey());
                                    writer.value(columnNext.getValue());
                                }
                                writer.endObject();
                            }
                            writer.endArray();
                        }
                        writer.endObject();
                    }
                };
                String jsonTextDB = jsonAdapter.toJson(dbData);
                try {
                    byte[] data;
                    if (secretKey != null) {
                        String encryptedJsonTextDB = AESUtils.encrypt(jsonTextDB, secretKey);
                        data = encryptedJsonTextDB.getBytes("UTF8");
                    } else {
                        data = jsonTextDB.getBytes("UTF8");
                    }
                    File root = new File(path);
                    if (!root.exists()) {
                        root.mkdirs();
                    }
                    File dFile = new File(root, fileName);
                    FileWriter writer = new FileWriter(dFile);
                    writer.append(new String(data));
                    writer.flush();
                    writer.close();

                    Log.i("TAG", "backup completed");
                    if (onWorkFinishListener != null) {
                        onWorkFinishListener.onFinished(true, "success");
                    }
                } catch (Exception e) {
                    if (onWorkFinishListener != null) {
                        onWorkFinishListener.onFinished(false, e.toString());
                    }
                }
            }
        }
    }
}

