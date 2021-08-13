package com.konradrej.rcpc;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.transition.Transition;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.transition.MaterialSharedAxis;
import com.konradrej.rcpc.databinding.FragmentMediaKeysBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MediaKeysFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaKeysFragment extends Fragment {

    private FragmentMediaKeysBinding binding;

    public MediaKeysFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MediaKeysFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaKeysFragment newInstance() {
        MediaKeysFragment fragment = new MediaKeysFragment();
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

        Transition enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        Transition exitTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);

        setEnterTransition(enterTransition);
        setExitTransition(exitTransition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMediaKeysBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}