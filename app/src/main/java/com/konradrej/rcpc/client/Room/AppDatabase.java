package com.konradrej.rcpc.client.Room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.konradrej.rcpc.client.Room.DAO.ConnectionDAO;
import com.konradrej.rcpc.client.Room.Entity.Connection;

/**
 * Represents the application database.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
@Database(entities = {Connection.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ConnectionDAO connectionDAO();
}
