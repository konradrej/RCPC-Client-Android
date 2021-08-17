package com.konradrej.rcpc;

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
import com.konradrej.rcpc.View.TouchPadView;
import com.konradrej.rcpc.databinding.FragmentTouchPadBinding;

/**
 * Represents a {@link Fragment} containing a touchpad and relevant controls.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
public class TouchPadFragment extends Fragment {

    private FragmentTouchPadBinding binding;
    private SharedPreferences sharedPreferences;
    private ConnectionHandler connectionHandler;

    /**
     * Required empty constructor
     */
    public TouchPadFragment() {
    }

    /**
     * Setups the fragments transitions.
     *
     * @param savedInstanceState saved bundle
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
        binding = FragmentTouchPadBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


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
                sendMessage("Touchpad: Move Event " + distanceX + " " + distanceY);
            }

            @Override
            public void onScroll(float distanceX, float distanceY) {
                sendMessage("Touchpad: Scroll Event " + distanceX + " " + distanceY);
            }

            @Override
            public void onLeftClick() {
                sendMessage("Touchpad: Left Click");
            }

            @Override
            public void onRightClick() {
                sendMessage("Touchpad: Right Click");
            }
        });
    }

    private void setupScrollBar() {
        binding.scrollBarArea.setOnScrollBarEventListener(((distanceX, distanceY) ->
                sendMessage("Scrollbar: Scroll Event " + distanceX + " " + distanceY)));
    }

    private void setupButtons() {
        binding.leftButtonArea.setOnClickListener((event) ->
                sendMessage("Button: Left Click"));
        binding.middleButtonArea.setOnClickListener((event) ->
                sendMessage("Button: Middle Click"));
        binding.rightButtonArea.setOnClickListener((event) ->
                sendMessage("Button: Right Click"));
    }

    private void sendMessage(String message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}