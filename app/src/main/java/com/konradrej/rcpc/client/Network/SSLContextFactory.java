package com.konradrej.rcpc.client.Network;

import android.util.Log;

import com.konradrej.rcpc.R;
import com.konradrej.rcpc.client.App;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Handles configuring SSLContext.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public class SSLContextFactory {
    private static final String TAG = SSLContextFactory.class.getName();

    /**
     * Gets SSLContext configured from property file.
     *
     * @return SSLContext with keystore and truststore or null if wrong passwords in property file.
     * @since 2.0
     */
    public static SSLContext getPreconfiguredInstance() {
        Properties properties = new Properties();

        try {
            properties.load(App.getContext().getResources().openRawResource(R.raw.ssl));
        } catch (IOException e) {
            Log.d(TAG, "Could not load ssl properties file. Error: " + e.getLocalizedMessage());
        }

        return getConfiguredInstance(
                properties.getProperty("keystore.password"),
                properties.getProperty("truststore.password")
        );
    }

    /**
     * Gets configured SSLContext.
     *
     * @param keyStorePassword   keystore password
     * @param trustStorePassword truststore password
     * @return SSLContext with keystore and truststore or null if wrong passwords
     * @since 2.0
     */
    public static SSLContext getConfiguredInstance(String keyStorePassword, String trustStorePassword) {
        KeyManager[] keyManagers = getKeyManagers(keyStorePassword);
        TrustManager[] trustManagers = getTrustManagers(trustStorePassword);

        try {
            if (keyManagers == null || trustManagers == null) {
                Log.d(TAG, "Get key- or trustmanagers failed. Using default SSLContext.");

                return SSLContext.getDefault();
            } else {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
                sslContext.init(keyManagers, trustManagers, null);

                return sslContext;
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "Error getting SSLContext. Error: " + e.getLocalizedMessage());
        }

        return null;
    }

    private static KeyManager[] getKeyManagers(String keyStorePassword) {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            char[] password = keyStorePassword.toCharArray();

            keyStore.load(App.getContext().getResources().openRawResource(R.raw.keystore), password);
            keyManagerFactory.init(keyStore, password);

            return keyManagerFactory.getKeyManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException e) {
            Log.d(TAG, "Could not get KeyManagers. Error: " + e.getLocalizedMessage());
        }

        return null;
    }

    private static TrustManager[] getTrustManagers(String trustStorePassword) {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = KeyStore.getInstance("PKCS12");

            char[] password = trustStorePassword.toCharArray();

            trustStore.load(App.getContext().getResources().openRawResource(R.raw.truststore), password);
            trustManagerFactory.init(trustStore);

            return trustManagerFactory.getTrustManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
            Log.d(TAG, "Could not get TrustManagers. Error: " + e.getLocalizedMessage());
        }

        return null;
    }
}
