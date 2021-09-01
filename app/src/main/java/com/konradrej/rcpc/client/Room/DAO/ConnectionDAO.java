package com.konradrej.rcpc.client.Room.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.konradrej.rcpc.client.Room.Entity.Connection;

import java.util.List;

/**
 * Represents a connection entity data access object.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
@Dao
public interface ConnectionDAO {

    @Query("SELECT * FROM connection ORDER BY connect_timestamp DESC LIMIT :limit")
    List<Connection> getLimitedAmount(int limit);

    @Insert
    void insert(Connection connection);
}
