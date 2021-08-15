package com.konradrej.rcpc.Room.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.konradrej.rcpc.Room.Entity.Connection;

import java.util.List;

@Dao
public interface ConnectionDAO {
    @Query("SELECT * FROM connection")
    List<Connection> getAll();

    @Query("SELECT * FROM connection ORDER BY connect_timestamp DESC LIMIT :limit")
    List<Connection> getLimitedAmount(int limit);

    @Insert
    void insert(Connection connection);

    @Delete
    void delete(Connection connection);
}
