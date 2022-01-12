package com.konradrej.rcpc.client;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.rcpc.databinding.FragmentMediaKeysBinding;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a {@link Fragment} containing media keys.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 1.0
 */
public class MediaKeysFragment extends Fragment {
    private static final String TAG = "MediaKeysFragment";

    private FragmentMediaKeysBinding binding;
    private final ConnectionHandler.onNetworkMessageListener networkMessageListener = new ConnectionHandler.onNetworkMessageListener() {
        @Override
        public void onReceivedMessage(JSONObject message) {
            try {
                if (message.getString("type").equals("INFO_CURRENT_VOLUME_UPDATE")) {
                    float volume = (float) (message.getDouble("volume") * 100f);

                    binding.volumeSlider.setValue(volume);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Could not find \"type\" in received message. Error: " + e.getLocalizedMessage());
            }
        }
    };
    private ConnectionHandler connectionHandler;

    /**
     * Required empty constructor.
     *
     * @since 1.0
     */
    public MediaKeysFragment() {
    }

    /**
     * Setups the fragments transitions.
     *
     * @param savedInstanceState saved bundle
     * @since 1.0
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Transition enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        Transition exitTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);

        setEnterTransition(enterTransition);
        setExitTransition(exitTransition);
    }

    /**
     * Sets socketHandler.
     *
     * @param connectionHandler the handler to set
     * @since 1.0
     */
    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    /**
     * Setups the fragments view and interaction.
     *
     * @param inflater           inflater to use
     * @param container          view container
     * @param savedInstanceState saved bundle
     * @return the newly setup view
     * @since 1.0
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMediaKeysBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupButtons();
        setupSlider();

        connectionHandler.addNetworkMessageCallback(networkMessageListener);

        sendMessage("ACTION_GET_CURRENT_VOLUME");

        return view;
    }

    /**
     * Resets binding on view destroy.
     *
     * @since 1.0
     */
    @Override
    public void onDestroyView() {
        connectionHandler.removeNetworkMessageCallback(networkMessageListener);
        super.onDestroyView();
        binding = null;
    }

    private void setupButtons() {
        binding.stopButton.setOnClickListener((event) ->
                sendMessage("ACTION_STOP"));
        binding.playPauseButton.setOnClickListener((event) ->
                sendMessage("ACTION_PLAY_PAUSE"));
        binding.previousButton.setOnClickListener((event) ->
                sendMessage("ACTION_PREVIOUS_TRACK"));
        binding.nextButton.setOnClickListener((event) ->
                sendMessage("ACTION_NEXT_TRACK"));
    }

    private void setupSlider() {
        binding.volumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("type", "ACTION_SET_VOLUME");
                    message.put("volume", value);

                    sendMessage(message);
                } catch (JSONException e) {
                    Log.e(TAG, "Could not add key to JSONObject. Error: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void sendMessage(String type) {
        try {
            JSONObject message = new JSONObject();
            message.put("type", type);

            sendMessage(message);
        } catch (JSONException e) {
            Log.e(TAG, "Could not add type to JSONObject. Error: " + e.getLocalizedMessage());
        }
    }

    private void sendMessage(JSONObject message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}