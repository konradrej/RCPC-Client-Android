package com.konradrej.rcpc;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.konradrej.rcpc.databinding.ActivityRemoteControlBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteControlActivity extends AppCompatActivity {

    private ActivityRemoteControlBinding binding;
    private View view;

    private final List<Fragment> fragments = new ArrayList<>();
    private final SocketHandler socketHandler = SocketHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoteControlBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        binding.topAppBar.setTitle(String.format(getString(R.string.remote_control_title), socketHandler.getIP()));

        setupFragments();
        setupErrorHandling();
        setupNavigation();
    }

    private void setupFragments() {
        TouchPadFragment touchPadFragment = new TouchPadFragment();
        touchPadFragment.setConnectionHandler(socketHandler);
        fragments.add(touchPadFragment);

        MediaKeysFragment mediaKeysFragment = new MediaKeysFragment();
        mediaKeysFragment.setConnectionHandler(socketHandler);
        fragments.add(mediaKeysFragment);

        setFragment(fragments.get(0));
    }

    private void setupErrorHandling() {
        socketHandler.addCallback(new SocketHandler.onNetworkEventListener() {
            @Override
            public void onConnect() {
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onConnectTimeout() {
            }

            @Override
            public void onError(IOException e) {
                runOnUiThread(() -> {
                    MaterialAlertDialogBuilder dialogBuilder =
                            new MaterialAlertDialogBuilder(view.getContext());

                    dialogBuilder.setTitle(getString(R.string.an_error_occurred_title))
                            .setMessage(e.getLocalizedMessage())
                            .setNeutralButton(getString(R.string.neutral_response_ok), (dialog, event) -> endRemoteControl())
                            .show();
                });
            }
        });
    }

    private void setupNavigation() {
        binding.topAppBar.setNavigationOnClickListener((event) ->
                endRemoteControl());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                endRemoteControl();
            }
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setFragment(fragments.get(binding.tabLayout.getSelectedTabPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void endRemoteControl() {
        socketHandler.disconnect();
        finish();
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}