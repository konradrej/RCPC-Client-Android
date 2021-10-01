package com.konradrej.rcpc.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdServiceInfo;
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
import com.konradrej.rcpc.R;
import com.konradrej.rcpc.client.Room.AppDatabase;
import com.konradrej.rcpc.client.Room.DAO.ConnectionDAO;
import com.konradrej.rcpc.client.Room.Entity.Connection;
import com.konradrej.rcpc.databinding.ActivityServerSelectBinding;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the server selection activity.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.2
 * @since 1.0
 */
public class ServerSelectActivity extends AppCompatActivity {
    private static final String TAG = "ServerSelectActivity";

    private final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();
    private final Map<String, View> nearbyServers = new HashMap<>();
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
                public void onRefused() {
                    runOnUiThread(() -> {
                        binding.connectionStatusIndicator.hide();

                        new MaterialAlertDialogBuilder(view.getContext())
                                .setTitle(getString(R.string.user_refused_connection_title))
                                .setMessage(getString(R.string.user_refused_connection_body))
                                .setNeutralButton(getString(R.string.neutral_response_ok), null)
                                .show();
                    });
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
                public void onError(Exception e) {
                }
            };
    private LayoutInflater layoutInflater;
    private View nearbyServerNoContent = null;

    /**
     * Setups the activities view and interaction.
     *
     * @param savedInstanceState saved bundle
     * @since 1.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServerSelectBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        setContentView(view);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getResources().openRawResource(R.raw.client_keystore), "***REMOVED***".toCharArray());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(getResources().openRawResource(R.raw.client_truststore), "***REMOVED***".toCharArray());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        connectionHandler.setKeyStore(keyStore);
        connectionHandler.setTruststore(trustStore);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        nearbyServerNoContent = findViewById(R.id.nearbyServerNoContent);

        // Start searching for services offering rcpc host and register listener
        ServiceClientHandler.setServiceListener(new ServiceListener());
        ServiceClientHandler.start(getApplicationContext());

        // Create/get database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "RCPCStorage").build();
        populateConnectionHistory();

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
     * Stops searching for service on pause.
     *
     * @since 1.0
     */
    @Override
    protected void onPause() {
        ServiceClientHandler.stop();

        super.onPause();
    }

    /**
     * Starts searching for service on resume.
     *
     * @since 1.0
     */
    @Override
    protected void onResume() {
        super.onResume();

        ServiceClientHandler.start(getApplicationContext());
    }


    /**
     * Removes itself as callback for SocketHandler.
     *
     * @since 1.0
     */
    @Override
    protected void onDestroy() {
        ServiceClientHandler.stop();
        connectionHandler.removeCallback(networkEventListener);

        super.onDestroy();
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

            if (connections.size() > 0) {
                runOnUiThread(() ->
                        binding.connectionHistoryContainer.removeAllViews());
            }

            for (Connection connection : connections) {
                @SuppressLint("InflateParams")
                View childLayout = layoutInflater.inflate(R.layout.connection_history_item, null);

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

    // TODO make connectToServer take port parameter and get it from serviceInfo
    private void addNearbyServer(NsdServiceInfo serviceInfo) {
        String serverName = serviceInfo.getHost().getHostName();
        String serverAddress = serviceInfo.getHost().getHostAddress();
        String serviceName = serviceInfo.getServiceName();

        runOnUiThread(() -> {
            if (nearbyServerNoContent != null) {
                binding.nearbyServersContainer.removeView(nearbyServerNoContent);
            }

            @SuppressLint("InflateParams")
            View childLayout = layoutInflater.inflate(R.layout.nearby_servers_item, null);

            TextView serverNameView = childLayout.findViewById(R.id.serverNameView);
            serverNameView.setText(serverName);

            Button connectButton = childLayout.findViewById(R.id.connectButton);
            connectButton.setOnClickListener((event) ->
                    connectToServer(serverAddress));

            binding.nearbyServersContainer.addView(childLayout);

            nearbyServers.put(serviceName, childLayout);
        });
    }

    private void removeNearbyServer(NsdServiceInfo serviceInfo) {
        View view = nearbyServers.remove(serviceInfo.getServiceName());

        if (view != null) {
            runOnUiThread(() -> {
                binding.nearbyServersContainer.removeView(view);

                if (binding.nearbyServersContainer.getChildCount() == 0) {
                    TextView textView = new TextView(getBaseContext());
                    textView.setText(getResources().getString(R.string.nearby_servers_no_servers_found));
                    textView.setId(R.id.nearbyServerNoContent);

                    binding.nearbyServersContainer.addView(nearbyServerNoContent);
                }
            });
        }
    }

    private class ServiceListener implements ServiceClientHandler.ServiceListener {
        @Override
        public void onFound(NsdServiceInfo serviceInfo) {
            addNearbyServer(serviceInfo);
        }

        @Override
        public void onLost(NsdServiceInfo serviceInfo) {
            removeNearbyServer(serviceInfo);
        }
    }
}