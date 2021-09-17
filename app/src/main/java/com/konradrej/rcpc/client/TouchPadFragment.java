package com.konradrej.rcpc.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.rcpc.client.View.TouchPadView;
import com.konradrej.rcpc.core.network.Message;
import com.konradrej.rcpc.core.network.MessageType;
import com.konradrej.rcpc.databinding.FragmentTouchPadBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a {@link Fragment} containing a touchpad and relevant controls.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.1
 * @since 1.0
 */
public class TouchPadFragment extends Fragment {

    private FragmentTouchPadBinding binding;
    private SharedPreferences sharedPreferences;
    private ConnectionHandler connectionHandler;

    /**
     * Required empty constructor.
     *
     * @since 1.0
     */
    public TouchPadFragment() {
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        Transition enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        Transition exitTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);

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
        binding = FragmentTouchPadBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Get setting and set layout accordingly
        if (sharedPreferences.getBoolean("flip_touchpad_buttons", false)) {
            binding.buttonContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        setupTouchPad();
        setupScrollBar();
        setupButtons();

        return view;
    }

    /**
     * Resets binding on view destroy.
     *
     * @since 1.0
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupTouchPad() {
        binding.touchPadArea.setOnTouchPadEventListener(new TouchPadView.OnTouchPadEventListener() {
            @Override
            public void onMove(float distanceX, float distanceY) {
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("distanceX", distanceX);
                additionalData.put("distanceY", distanceY);

                Message message = new Message(MessageType.ACTION_MOVE, null, additionalData);

                sendMessage(message);
            }

            @Override
            public void onScroll(float distanceX, float distanceY) {
                Map<String, Object> additionalData = new HashMap<>();
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    additionalData.put("distanceX", distanceX);
                    additionalData.put("distanceY", 0f);
                } else {
                    additionalData.put("distanceX", 0f);
                    additionalData.put("distanceY", distanceY);
                }


                Message message = new Message(MessageType.ACTION_SCROLL, null, additionalData);

                sendMessage(message);
            }

            @Override
            public void onLeftClick() {
                sendMessage(new Message(MessageType.ACTION_PRIMARY_CLICK));
            }

            @Override
            public void onRightClick() {
                sendMessage(new Message(MessageType.ACTION_SECONDARY_CLICK));
            }
        });
    }

    private void setupScrollBar() {
        binding.scrollBarArea.setOnScrollBarEventListener(((distanceX, distanceY) -> {
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("distanceX", 0f);
            additionalData.put("distanceY", distanceY);

            Message message = new Message(MessageType.ACTION_SCROLL, null, additionalData);

            sendMessage(message);
        }));
    }

    private void setupButtons() {
        binding.leftButtonArea.setOnClickListener((event) ->
                sendMessage(new Message(MessageType.ACTION_PRIMARY_CLICK)));
        binding.middleButtonArea.setOnClickListener((event) ->
                sendMessage(new Message(MessageType.ACTION_MIDDLE_CLICK)));
        binding.rightButtonArea.setOnClickListener((event) ->
                sendMessage(new Message(MessageType.ACTION_SECONDARY_CLICK)));
    }

    private void sendMessage(Message message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}