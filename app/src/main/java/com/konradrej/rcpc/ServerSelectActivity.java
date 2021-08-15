package com.konradrej.rcpc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.konradrej.rcpc.Room.AppDatabase;
import com.konradrej.rcpc.Room.DAO.ConnectionDAO;
import com.konradrej.rcpc.Room.Entity.Connection;
import com.konradrej.rcpc.databinding.ActivityServerSelectBinding;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

public class ServerSelectActivity extends AppCompatActivity {

    private ActivityServerSelectBinding binding;
    private final SocketHandler socketHandler = SocketHandler.getInstance();
    private View view;
    private AppDatabase db;

    // TODO Possibly separate into individual listeners instead of multiple in one
    private final SocketHandler.onNetworkEventListener networkEventListener = new SocketHandler.onNetworkEventListener() {
        @Override
        public void onConnect() {
            addConnectionToHistory();

            runOnUiThread(() -> {
                binding.connectionStatusIndicator.hide();
            });

            Intent intent = new Intent(getApplicationContext(), RemoteControlActivity.class);
            intent.putExtra("title", "[Connected]");

            startActivity(intent);
        }

        @Override
        public void onDisconnect() {
        }

        @Override
        public void onConnectTimeout() {
            runOnUiThread(() -> {
                binding.connectionStatusIndicator.hide();

                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(view.getContext());

                dialogBuilder.setTitle("Could not connect")
                        .setMessage("The IP address could not be reached, make sure the server is started and that the IP address is correct")
                        .setNeutralButton("Ok", (dialog, event) -> {
                            Log.d("DEBUG", "Fate accepted :(");
                        })
                        .show();

                // TODO move into RemoteControlActivity
            });
        }

        @Override
        public void onError(IOException e) {
            runOnUiThread(() -> {
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(view.getContext());

                dialogBuilder.setTitle("An error occurred")
                        .setMessage(e.getLocalizedMessage())
                        .setNeutralButton("Ok", (dialog, event) -> {
                        })
                        .show();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServerSelectBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        // getApplicationContext().deleteDatabase("RCPCStorage");
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "RCPCStorage").build();
        populateConnectionHistory();

        binding.topAppBar.setOnMenuItemClickListener((menuItem) -> {
            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        });

        binding.connectButton.setOnClickListener((event) -> {
            binding.outlinedIpField.setError(null);

            Editable ipAddressEditable = binding.ipField.getText();
            if (ipAddressEditable != null && Patterns.IP_ADDRESS.matcher(ipAddressEditable.toString()).matches()) {
                connectToServer(ipAddressEditable.toString());
            } else {
                binding.outlinedIpField.setError("Incorrect IP address.");
            }
        });
    }

    private void connectToServer(String ip) {
        binding.connectionStatusIndicator.show();

        socketHandler.setIP(ip);
        socketHandler.addCallback(networkEventListener);
        new Thread(socketHandler).start();
    }

    private void addConnectionToHistory() {
        new Thread(() -> {
            ConnectionDAO connectionDAO = db.connectionDAO();

            Connection connection = new Connection();
            connection.setIp(socketHandler.getIP());
            connection.setConnectTimestamp(System.currentTimeMillis());

            connectionDAO.insert(connection);
        }).start();
    }

    private void populateConnectionHistory() {
        new Thread(() -> {
            ConnectionDAO connectionDAO = db.connectionDAO();
            List<Connection> connections = connectionDAO.getLimitedAmount(15);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

            for (Connection connection : connections) {
                View childLayout = inflater.inflate(R.layout.connection_history_item, null);

                TextView addressView = childLayout.findViewById(R.id.addressView);
                addressView.setText("Address: " + connection.getIp());

                TextView dateView = childLayout.findViewById(R.id.dateView);
                dateView.setText("Date: " + DateFormat.getDateTimeInstance().format(connection.getConnectTimestamp()));

                Button connectButton = childLayout.findViewById(R.id.connectButton);
                connectButton.setOnClickListener((event) -> {
                    connectToServer(connection.getIp());
                });

                runOnUiThread(() -> {
                    binding.connectionHistoryContainer.addView(childLayout);
                });
            }
        }).start();
    }
}