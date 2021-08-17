package com.konradrej.rcpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialSharedAxis;
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
                sendMessage("Stop Button: Click"));
        binding.pauseButton.setOnClickListener((event) ->
                sendMessage("Pause Button: Click"));
        binding.playButton.setOnClickListener((event) ->
                sendMessage("Play Button: Click"));
        binding.previousButton.setOnClickListener((event) ->
                sendMessage("Previous Button: Click"));
        binding.nextButton.setOnClickListener((event) ->
                sendMessage("Next Button: Click"));
    }

    private void setupSlider() {
        binding.volumeSlider.addOnChangeListener((slider, value, fromUser) ->
                sendMessage("Volume Slider: Change Volume to " + value));
    }

    private void sendMessage(String message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}