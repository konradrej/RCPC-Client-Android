package com.konradrej.rcpc.Room.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Connection {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "ip")
    private String ip;

    @ColumnInfo(name = "connect_timestamp")
    private long connectTimestamp;

    public void setConnectTimestamp(long connectTimestamp) {
        this.connectTimestamp = connectTimestamp;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getConnectTimestamp() {
        return connectTimestamp;
    }

    public long getUid() {
        return uid;
    }

    public String getIp() {
        return ip;
    }
}
