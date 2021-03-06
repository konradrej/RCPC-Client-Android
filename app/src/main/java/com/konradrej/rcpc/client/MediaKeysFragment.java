package com.konradrej.rcpc.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.rcpc.core.network.Message;
import com.konradrej.rcpc.core.network.MessageType;
import com.konradrej.rcpc.databinding.FragmentMediaKeysBinding;

/**
 * Represents a {@link Fragment} containing media keys.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.2
 * @since 1.0
 */
public class MediaKeysFragment extends Fragment {
    private FragmentMediaKeysBinding binding;
    private final ConnectionHandler.onNetworkMessageListener networkMessageListener = new ConnectionHandler.onNetworkMessageListener() {
        @Override
        public void onReceivedMessage(Message message) {
            if (message.getMessageType() == MessageType.INFO_CURRENT_VOLUME_UPDATE) {
                float volume = ((Float) message.getMessageData()) * 100;

                binding.volumeSlider.setValue(volume);
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
        sendMessage(new Message(MessageType.ACTION_GET_CURRENT_VOLUME));

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
                sendMessage(new Message(MessageType.ACTION_STOP)));
        binding.playPauseButton.setOnClickListener((event) ->
                sendMessage(new Message(MessageType.ACTION_PLAY_PAUSE)));
        binding.previousButton.setOnClickListener((event) ->
                sendMessage(new Message(MessageType.ACTION_PREVIOUS_TRACK)));
        binding.nextButton.setOnClickListener((event) ->
                sendMessage(new Message(MessageType.ACTION_NEXT_TRACK)));
    }

    private void setupSlider() {
        binding.volumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                sendMessage(new Message(MessageType.ACTION_SET_VOLUME, value));
            }
        });
    }

    private void sendMessage(Message message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}