package com.allen218.roombackupandrestore.db.function;

import android.database.Cursor;
import android.util.Log;

import androidx.room.RoomDatabase;

import com.allen218.roombackupandrestore.db.OnWorkFinishListener;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.LongUnaryOperator;

public class Restore {
    private static String STRING_FOR_NULL_VALUE = "!!!string_for_null_value!!!";

    public static class Init {
        private RoomDatabase database;
        private String backupFilePath;
        private String secretKey;
        private OnWorkFinishListener onWorkFinishListener;

        public Init database(RoomDatabase database) {
            this.database = database;
            return this;
        }

        public Init backupFilePath(String backupFilePath) {
            this.backupFilePath = backupFilePath;
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
            try {
                if (database == null) {
                    onWorkFinishListener.onFinished(false, "Database not specified");
                    return;
                }
                if (backupFilePath == null) {
                    onWorkFinishListener.onFinished(false, "Backup file path not specified");
                    return;
                }
                File fl = new File(backupFilePath);
                FileInputStream fin = new FileInputStream(fl);
                String data = convertStreamToString(fin);
                fin.close();
                String jsonTextDB;
                if (secretKey != null) {
                    jsonTextDB = AESUtils.decrypt(data, secretKey);
                } else {
                    jsonTextDB = data;
                }

                JsonAdapter<HashMap<String, ArrayList<HashMap<String, String>>>> jsonAdapter =
                        new JsonAdapter<HashMap<String, ArrayList<HashMap<String, String>>>>() {

                            @Override
                            public HashMap<String, ArrayList<HashMap<String, String>>> fromJson(JsonReader reader) throws IOException {
                                HashMap<String, ArrayList<HashMap<String, String>>> result = new HashMap<>();
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String tableName = reader.nextName();
                                    reader.beginArray();
                                    ArrayList<HashMap<String, String>> rowList = new ArrayList<>();
                                    while (reader.hasNext()) {
                                        reader.beginObject();
                                        HashMap<String, String> columnMap = new HashMap<>();
                                        while (reader.hasNext()) {
                                            columnMap.put(reader.nextName(), reader.readJsonValue() + "");
                                        }
                                        rowList.add(columnMap);
                                        reader.endObject();
                                    }
                                    result.put(tableName, rowList);
                                    reader.endArray();
                                }
                                reader.endObject();
                                return result;
                            }

                            @Override
                            public void toJson(JsonWriter writer, HashMap<String, ArrayList<HashMap<String, String>>> value) throws IOException {

                            }
                        };


                HashMap<String, ArrayList<HashMap<String, String>>> jsonDB = jsonAdapter.fromJson(jsonTextDB);

                Iterator<Map.Entry<String, ArrayList<HashMap<String, String>>>> tableIterator = jsonDB.entrySet().iterator();
                while (tableIterator.hasNext()) {
                    Map.Entry<String, ArrayList<HashMap<String, String>>> tableEntry = tableIterator.next();
                    String tableKey = tableEntry.getKey();
                    ArrayList<HashMap<String, String>> tableValue = tableEntry.getValue();
                    Cursor c = database.query("delete from " + tableKey, null);
                    int p = c.getCount();
                    Log.e("TAG", String.valueOf(p));

                    for (int i = 0; i < tableValue.size(); i++) {
                        Iterator<Map.Entry<String, String>> rowIterator = tableValue.get(i).entrySet().iterator();
                        String query = "insert into " + tableKey + " (";
                        while (rowIterator.hasNext()) {
                            Map.Entry<String, String> rowEntry = rowIterator.next();
                            String rowKey = rowEntry.getKey();
                            query = query.concat(rowKey).concat(",");
                        }
                        query = query.substring(0, query.lastIndexOf(","));
                        query = query.concat(") ");
                        query = query.concat("values(");

                        Iterator<Map.Entry<String, String>> rowIterator1 = tableValue.get(i).entrySet().iterator();
                        while (rowIterator1.hasNext()) {
                            Map.Entry<String, String> rowEntry = rowIterator1.next();
                            String rowValue = rowEntry.getValue();
                            if (rowValue.equals(STRING_FOR_NULL_VALUE)) {
                                query = query.concat("NULL").concat(",");
                            } else {
                                query = query.concat("\'").concat(rowValue).concat("\'").concat(",");
                            }
                        }

                        query = query.substring(0, query.lastIndexOf(","));
                        query = query.concat(")");
                        Cursor cc = database.query(query, null);
                        int pp = cc.getCount();
                        Log.e("TAG", String.valueOf(pp));
                    }
                }
                Log.i("TAG", "restore completed");
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

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}
