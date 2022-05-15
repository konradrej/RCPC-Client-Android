package com.konradrej.rcpc.client;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.konradrej.rcpc.client.Network.NetworkHandler;

import java.util.UUID;

/**
 * App wrapper to provide "app global" functions.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public class App extends Application {

    private static Context context;
    private static NetworkHandler networkHandler;

    /**
     * Gets application context.
     *
     * @return application context instance
     * @since 2.0
     */
    public static Context getContext() {
        return context;
    }

    /**
     * Get NetworkHandler instance.
     *
     * @return NetworkHandler instance
     * @since 2.0
     */
    public static NetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    /**
     * Get device UUID.
     *
     * @return device UUID
     * @since 2.0
     */
    public static String getDeviceUUID() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        String uuidKey = "app_instance_uuid";
        String uuid = sharedPreferences.getString(uuidKey, null);

        if (uuid == null) {
            uuid = UUID.randomUUID().toString();

            sharedPreferences.edit().putString(uuidKey, uuid).apply();
        }

        return uuid;
    }

    /**
     * Overrides onCreate to setup context and NetworkHandler.
     *
     * @since 2.0
     */
    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        networkHandler = new NetworkHandler();
    }
}
