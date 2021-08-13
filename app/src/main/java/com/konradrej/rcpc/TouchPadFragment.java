package com.konradrej.rcpc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.rcpc.View.TouchPadView;
import com.konradrej.rcpc.databinding.FragmentTouchPadBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TouchPadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TouchPadFragment extends Fragment {

    private FragmentTouchPadBinding binding;
    private SharedPreferences sharedPreferences;
    private ConnectionHandler connectionHandler;

    public TouchPadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TouchPadFragment.
     */
    public static TouchPadFragment newInstance() {
        TouchPadFragment fragment = new TouchPadFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO
        }

        Transition enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        Transition exitTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);

        setEnterTransition(enterTransition);
        setExitTransition(exitTransition);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTouchPadBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        if (sharedPreferences.getBoolean("flip_touchpad_buttons", false)) {
            binding.buttonContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        setupTouchPad();
        setupScrollBar();
        setupButtons();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        sharedPreferences = null;
    }

    private void setupTouchPad() {
        binding.touchPadArea.setOnTouchPadEventListener(new TouchPadView.OnTouchPadEventListener() {
            @Override
            public void onMove(float distanceX, float distanceY) {
                Log.d("DEBUG", "Touchpad: Move Event " + distanceX + " " + distanceY);
                sendMessage("Touchpad: Move Event " + distanceX + " " + distanceY);
            }

            @Override
            public void onScroll(float distanceX, float distanceY) {
                Log.d("DEBUG", "Touchpad: Scroll Event " + distanceX + " " + distanceY);
                sendMessage("Touchpad: Scroll Event " + distanceX + " " + distanceY);
            }

            @Override
            public void onLeftClick() {
                Log.d("DEBUG", "Touchpad: Left Click");
                sendMessage("Touchpad: Left Click");
            }

            @Override
            public void onRightClick() {
                Log.d("DEBUG", "Touchpad: Right Click");
                sendMessage("Touchpad: Right Click");
            }
        });
    }

    private void setupScrollBar() {
        binding.scrollBarArea.setOnScrollBarEventListener(((distanceX, distanceY) -> {
            Log.d("DEBUG", "Scrollbar: Scroll Event " + distanceX + " " + distanceY);
            sendMessage("Scrollbar: Scroll Event " + distanceX + " " + distanceY);
        }));
    }

    private void setupButtons() {
        binding.leftButtonArea.setOnClickListener((event) -> {
            Log.d("DEBUG", "Button: Left Click");
            sendMessage("Button: Left Click");
        });

        binding.middleButtonArea.setOnClickListener((event) -> {
            Log.d("DEBUG", "Button: Middle Click");
            sendMessage("Button: Middle Click");
        });
        binding.rightButtonArea.setOnClickListener((event) -> {
            Log.d("DEBUG", "Button: Right Click");
            sendMessage("Button: Right Click");
        });
    }

    private void sendMessage(String message) {
        if (connectionHandler != null) {
            connectionHandler.sendMessage(message);
        }
    }
}