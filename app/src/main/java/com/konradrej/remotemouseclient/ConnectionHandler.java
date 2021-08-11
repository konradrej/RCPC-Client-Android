package com.konradrej.remotemouseclient;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    @Override
    public void run() {
        try(Socket socket = new Socket("192.168.1.65", 666);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())){
            out.writeUTF("Test");
            out.flush();
        }catch(IOException e){
            Log.e("RCPC", "CONNECTION ERROR" + e.getMessage());
        }
    }
}
