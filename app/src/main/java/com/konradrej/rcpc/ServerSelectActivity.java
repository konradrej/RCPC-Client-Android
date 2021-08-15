package com.konradrej.rcpc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
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
    private SharedPreferences sharedPreferences;
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

            startActivity(new Intent(getApplicationContext(), RemoteControlActivity.class));
        }

        @Override
        public void onDisconnect() {
        }

        @Override
        public void onConnectTimeout() {
            runOnUiThread(() -> {
                binding.connectionStatusIndicator.hide();

                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(view.getContext());

                dialogBuilder.setTitle(getString(R.string.could_not_connect_title))
                        .setMessage(getString(R.string.could_not_connect_body))
                        .setNeutralButton(getString(R.string.neutral_response_ok), (dialog, event) -> {
                            // TODO
                        })
                        .show();

                // TODO move into RemoteControlActivity
            });
        }

        @Override
        public void onError(IOException e) {
            runOnUiThread(() -> {
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(view.getContext());

                dialogBuilder.setTitle(getString(R.string.an_error_occurred_title))
                        .setMessage(e.getLocalizedMessage())
                        .setNeutralButton(getString(R.string.neutral_response_ok), (dialog, event) -> {
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
                binding.outlinedIpField.setError(getString(R.string.address_input_error));
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

            int entriesAmount = Integer.parseInt(sharedPreferences.getString("connection_history_entries_amount", "15"));
            List<Connection> connections = connectionDAO.getLimitedAmount(entriesAmount);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

            for (Connection connection : connections) {
                View childLayout = inflater.inflate(R.layout.connection_history_item, null);

                TextView addressView = childLayout.findViewById(R.id.addressView);
                addressView.setText(String.format(getString(R.string.connection_history_address_label), connection.getIp()));

                // TODO: Maybe different datetime formatting
                String dateTime = DateFormat.getDateTimeInstance().format(connection.getConnectTimestamp());
                TextView dateView = childLayout.findViewById(R.id.dateView);
                dateView.setText(String.format(getString(R.string.connection_history_date_label), dateTime));

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