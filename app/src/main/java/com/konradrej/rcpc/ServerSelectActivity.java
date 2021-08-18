package com.konradrej.rcpc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
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

/**
 * Represents the server selection activity.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
public class ServerSelectActivity extends AppCompatActivity {

    private final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();
    private ActivityServerSelectBinding binding;
    private View view;
    private SharedPreferences sharedPreferences;
    private AppDatabase db;

    private final ConnectionHandler.onNetworkEventListener networkEventListener =
            new ConnectionHandler.onNetworkEventListener() {
                @Override
                public void onConnect() {
                    addConnectionToHistory();

                    runOnUiThread(() ->
                            binding.connectionStatusIndicator.hide());

                    startActivity(new Intent(getApplicationContext(), RemoteControlActivity.class));
                    finish();
                }

                @Override
                public void onDisconnect() {
                }

                @Override
                public void onConnectTimeout() {
                    runOnUiThread(() -> {
                        binding.connectionStatusIndicator.hide();

                        new MaterialAlertDialogBuilder(view.getContext())
                                .setTitle(getString(R.string.could_not_connect_title))
                                .setMessage(getString(R.string.could_not_connect_body))
                                .setNeutralButton(getString(R.string.neutral_response_ok), null)
                                .show();
                    });
                }

                @Override
                public void onError(IOException e) {
                }
            };

    /**
     * Setups the activities view and interaction.
     *
     * @param savedInstanceState saved bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServerSelectBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        // Create/get database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "RCPCStorage").build();
        populateConnectionHistory();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        binding.topAppBar.setOnMenuItemClickListener((menuItem) -> {
            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        });

        binding.connectButton.setOnClickListener((event) -> {
            binding.outlinedIpField.setError(null);

            Editable ipAddressEditable = binding.ipField.getText();

            if (ipAddressEditable != null) {
                String ipAddress = ipAddressEditable.toString();

                // Check for valid ip address
                if (Patterns.IP_ADDRESS.matcher(ipAddress).matches()) {
                    connectToServer(ipAddress);

                    return;
                }
            }

            binding.outlinedIpField.setError(getString(R.string.address_input_error));
        });
    }

    /**
     * Removes itself as callback for SocketHandler.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        connectionHandler.removeCallback(networkEventListener);
    }

    private void connectToServer(String ip) {
        binding.connectionStatusIndicator.show();

        connectionHandler.connectToServer(ip, networkEventListener);
    }

    private void addConnectionToHistory() {
        new Thread(() -> {
            ConnectionDAO connectionDAO = db.connectionDAO();

            Connection connection = new Connection();
            connection.ip = connectionHandler.getIP();
            connection.connectTimestamp = System.currentTimeMillis();

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
                addressView.setText(String.format(getString(R.string.connection_history_address_label), connection.ip));

                String dateTime = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(connection.connectTimestamp);
                TextView dateView = childLayout.findViewById(R.id.dateView);
                dateView.setText(String.format(getString(R.string.connection_history_date_label), dateTime));

                Button connectButton = childLayout.findViewById(R.id.connectButton);
                connectButton.setOnClickListener((event) ->
                        connectToServer(connection.ip));

                runOnUiThread(() ->
                        binding.connectionHistoryContainer.addView(childLayout));
            }
        }).start();
    }
}