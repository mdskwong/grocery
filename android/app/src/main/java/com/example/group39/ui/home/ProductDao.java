package com.example.group39.ui.home;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    long insertRecord(Product product);

    @Update
    int updateRecord(Product product);

    @Delete
    int deleteRecord(Product product);

    @Query("SELECT * FROM records WHERE recordId = :id")
    Product getRecordById(int id);

    @Query("SELECT * FROM records")
    LiveData<List<Product>> getAllRecords();

    @Query("SELECT DISTINCT productName FROM records")
    LiveData<List<String>> getUniqueRecords();

    @Query("SELECT * FROM records WHERE productName LIKE :productName")
    LiveData<List<Product>> getRecordsByName(String productName);

    @Query("SELECT MAX(recordId) FROM records")
    int getLastRecordId();
}
