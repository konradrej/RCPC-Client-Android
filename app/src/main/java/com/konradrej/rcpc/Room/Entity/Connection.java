package com.konradrej.rcpc.Room.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Connection {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "ip")
    public String ip;

    @ColumnInfo(name = "connect_timestamp")
    public long connectTimestamp;
}
