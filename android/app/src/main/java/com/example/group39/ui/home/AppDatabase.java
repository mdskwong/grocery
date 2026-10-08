package com.example.group39.ui.home;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {Product.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d("AppDatabase", "Creating new database instance");
//                    context.deleteDatabase("record_db");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "record_db")
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
