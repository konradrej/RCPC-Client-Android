package com.konradrej.rcpc.client;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.konradrej.rcpc.R;
import com.konradrej.rcpc.databinding.ActivityRemoteControlBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the remote control activity.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.3
 * @since 1.0
 */
public class RemoteControlActivity extends AppCompatActivity {

    private final List<Fragment> fragments = new ArrayList<>();
    private final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();
    private ActivityRemoteControlBinding binding;
    private View view;
    private boolean endRemoteControl;

    private final ConnectionHandler.onNetworkEventListener networkEventListener =
            new ConnectionHandler.onNetworkEventListener() {
                @Override
                public void onConnect() {
                }

                @Override
                public void onRefused() {
                }

                @Override
                public void onDisconnect() {
                    endRemoteControl();
                }

                @Override
                public void onConnectTimeout() {
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        if(!endRemoteControl){
                            MaterialAlertDialogBuilder dialogBuilder =
                                    new MaterialAlertDialogBuilder(view.getContext());

                            dialogBuilder.setTitle(getString(R.string.an_error_occurred_title))
                                    .setMessage(e.getLocalizedMessage())
                                    .setNeutralButton(getString(R.string.neutral_response_ok), (dialog, event) -> endRemoteControl())
                                    .show();
                        }
                    });
                }
            };

    /**
     * Setups the activities view and interaction.
     *
     * @param savedInstanceState saved bundle
     * @since 1.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoteControlBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        endRemoteControl = false;
        setContentView(view);

        ServiceClientHandler.stop();
        binding.topAppBar.setTitle(String.format(getString(R.string.remote_control_title), connectionHandler.getIP()));

        // Remove tab icons if in landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            for (int i = 0, tabCount = binding.tabLayout.getTabCount(); i < tabCount; i++) {
                TabLayout.Tab tab = binding.tabLayout.getTabAt(i);

                if (tab != null) {
                    tab.setIcon(null);
                }
            }
        }

        setupFragments(savedInstanceState);
        setupErrorHandling();
        setupNavigation();
    }

    /**
     * Removes itself as callback for SocketHandler.
     *
     * @since 1.0
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        connectionHandler.removeNetworkEventCallback(networkEventListener);
    }

    /**
     * Saves the current instances state to bundle.
     *
     * @param savedInstanceState bundle to save to
     * @since 1.0
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("fragmentIndex", binding.tabLayout.getSelectedTabPosition());

        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupFragments(Bundle savedInstanceState) {
        TouchPadFragment touchPadFragment = new TouchPadFragment();
        touchPadFragment.setConnectionHandler(connectionHandler);
        fragments.add(touchPadFragment);

        MediaKeysFragment mediaKeysFragment = new MediaKeysFragment();
        mediaKeysFragment.setConnectionHandler(connectionHandler);
        fragments.add(mediaKeysFragment);

        // Checks if fragmentIndex was saved and restores it if so
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("fragmentIndex")) {
                int fragmentIndex = savedInstanceState.getInt("fragmentIndex");
                TabLayout.Tab tab = binding.tabLayout.getTabAt(fragmentIndex);

                setFragment(fragments.get(fragmentIndex));

                if (tab != null)
                    tab.select();

                return;
            }
        }

        setFragment(fragments.get(0));
    }

    private void setupErrorHandling() {
        connectionHandler.addNetworkEventCallback(networkEventListener);
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
        endRemoteControl = true;

        connectionHandler.disconnect();
        finish();
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}