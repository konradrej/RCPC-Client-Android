package com.konradrej.rcpc.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.rcpc.client.Network.ConnectionHandler;
import com.konradrej.rcpc.client.View.TouchPadView;
import com.konradrej.rcpc.databinding.FragmentTouchPadBinding;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a {@link Fragment} containing a touchpad and relevant controls.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 1.0
 */
public class TouchPadFragment extends Fragment {
    private static final String TAG = "TouchPadFragment";
    private final ConnectionHandler connectionHandler = App.getNetworkHandler().getConnectionHandler();
    private FragmentTouchPadBinding binding;
    private SharedPreferences sharedPreferences;

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
                try {
                    JSONObject message = new JSONObject();
                    message.put("type", "ACTION_MOVE");
                    message.put("distanceX", distanceX);
                    message.put("distanceY", distanceY);

                    sendMessage(message);
                } catch (JSONException e) {
                    Log.e(TAG, "Could not add key to JSONObject. Error: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onScroll(float distanceX, float distanceY) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("type", "ACTION_SCROLL");

                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        message.put("distanceX", distanceX);
                        message.put("distanceY", 0f);
                    } else {
                        message.put("distanceX", 0f);
                        message.put("distanceY", distanceY);
                    }

                    sendMessage(message);
                } catch (JSONException e) {
                    Log.e(TAG, "Could not add key to JSONObject. Error: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onLeftClick() {
                sendMessage("ACTION_PRIMARY_CLICK");
            }

            @Override
            public void onRightClick() {
                sendMessage("ACTION_SECONDARY_CLICK");
            }

            @Override
            public void onClickDragStart() {
                sendMessage("ACTION_CLICK_AND_DRAG_START");
            }

            @Override
            public void onClickDragMove(float distanceX, float distanceY) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("type", "ACTION_CLICK_AND_DRAG_MOVE");
                    message.put("distanceX", distanceX);
                    message.put("distanceY", distanceY);

                    sendMessage(message);
                } catch (JSONException e) {
                    Log.e(TAG, "Could not add key to JSONObject. Error: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onClickDragEnd() {
                sendMessage("ACTION_CLICK_AND_DRAG_END");
            }
        });
    }

    private void setupScrollBar() {
        binding.scrollBarArea.setOnScrollBarEventListener(((distanceX, distanceY) -> {
            try {
                JSONObject message = new JSONObject();
                message.put("type", "ACTION_SCROLL");
                message.put("distanceX", 0f);
                message.put("distanceY", distanceY);

                sendMessage(message);
            } catch (JSONException e) {
                Log.e(TAG, "Could not add key to JSONObject. Error: " + e.getLocalizedMessage());
            }
        }));
    }

    private void setupButtons() {
        binding.leftButtonArea.setOnClickListener((event) ->
                sendMessage("ACTION_PRIMARY_CLICK"));
        binding.middleButtonArea.setOnClickListener((event) ->
                sendMessage("ACTION_MIDDLE_CLICK"));
        binding.rightButtonArea.setOnClickListener((event) ->
                sendMessage("ACTION_SECONDARY_CLICK"));
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