package com.konradrej.rcpc.client;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;


/**
 * DNS service discovery handler.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.2
 */
public class ServiceClientHandler {
    private static final String TAG = "ServiceClientHandler";
    private static final String SERVICE_TYPE = "_rcpc._tcp.";
    private static final ServiceResolveListener resolveListener = new ServiceResolveListener();
    private static ServiceListener serviceListener = null;

    private static ServiceDiscoveryListener discoveryListener = null;
    private static NsdManager nsdManager = null;

    private ServiceClientHandler() {

    }

    /**
     * Start listening for service type using the pre-set ServiceListener.
     *
     * @param context application context
     */
    public static void start(Context context) {
        stop();

        if (nsdManager == null) {
            nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        }

        discoveryListener = new ServiceDiscoveryListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    /**
     * Stop all service discovery.
     */
    public static void stop() {
        if (nsdManager != null && discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
            discoveryListener = null;
        }
    }

    /**
     * Set ServiceListener.
     *
     * @param serviceListener listener to use
     */
    public static void setServiceListener(ServiceListener serviceListener) {
        ServiceClientHandler.serviceListener = serviceListener;
    }

    /**
     * Listener interface for service found and lost events.
     */
    public interface ServiceListener {
        void onFound(NsdServiceInfo serviceInfo);

        void onLost(NsdServiceInfo serviceInfo);
    }

    private static class ServiceDiscoveryListener implements NsdManager.DiscoveryListener {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery start failed:\n Service type: " + serviceType + "\n Error code: " + errorCode);
            nsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery stop failed:\n Service type: " + serviceType + "\n Error code: " + errorCode);
            nsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d(TAG, "Service discovery started");
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(TAG, "Discovery stopped: " + serviceType);
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service discovery success" + serviceInfo);
            nsdManager.resolveService(serviceInfo, resolveListener);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.e(TAG, "Service lost: " + serviceInfo);

            if (ServiceClientHandler.serviceListener != null) {
                ServiceClientHandler.serviceListener.onLost(serviceInfo);
            }
        }
    }

    private static class ServiceResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "Service resolve failed:\n Error code: " + errorCode + "\n Service info: " + serviceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service resolved: " + serviceInfo);

            if (ServiceClientHandler.serviceListener != null) {
                ServiceClientHandler.serviceListener.onFound(serviceInfo);
            }
        }
    }
}
