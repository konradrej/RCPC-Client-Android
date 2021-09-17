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
 * Represents a {@link Fragment} containing a media keys.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
public class MediaKeysFragment extends Fragment {

    private FragmentMediaKeysBinding binding;
    private ConnectionHandler connectionHandler;

    /**
     * Required empty constructor
     */
    public MediaKeysFragment() {
    }

    /**
     * Setups the fragments transitions.
     *
     * @param savedInstanceState saved bundle
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
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMediaKeysBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupButtons();
        setupSlider();

        return view;
    }

    /**
     * Resets binding on view destroy.
     */
    @Override
    public void onDestroyView() {
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
        binding.volumeSlider.addOnChangeListener((slider, value, fromUser) ->
                sendMessage(new Message(MessageType.ACTION_SET_VOLUME, value)));
    }

    private void sendMessage(Message message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}