package com.konradrej.rcpc.client.Room.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a connection entity.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
@Entity
public class Connection {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "ip")
    public String ip;

    @ColumnInfo(name = "connect_timestamp")
    public long connectTimestamp;
}
