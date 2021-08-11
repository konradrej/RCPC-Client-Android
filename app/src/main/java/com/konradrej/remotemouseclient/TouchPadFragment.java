package com.konradrej.remotemouseclient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.transition.Transition;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.remotemouseclient.databinding.FragmentTouchPadBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TouchPadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TouchPadFragment extends Fragment {

    private FragmentTouchPadBinding binding;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTouchPadBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.touchPadArea.setOnTouchPadEventListener(new TouchPadView.OnTouchPadEventListener() {
            @Override
            public void onMove(float distanceX, float distanceY) {
                Log.d("DEBUG", "Move Event " + distanceX + " " + distanceY);
            }

            @Override
            public void onScroll(float distanceX, float distanceY) {
                Log.d("DEBUG", "Scroll Event " + distanceX + " " + distanceY);
            }

            @Override
            public void onLeftClick() {
                Log.d("DEBUG" , "Left Click");
            }

            @Override
            public void onRightClick() {
                Log.d("DEBUG" , "Right Click");
            }
        });

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }
}