package com.konradrej.rcpc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.konradrej.rcpc.R;
import com.konradrej.rcpc.databinding.ActivityRemoteControlBinding;

public class RemoteControlActivity extends AppCompatActivity {

    private ActivityRemoteControlBinding binding;
    private Thread thread;
    private Fragment[] fragments = new Fragment[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoteControlBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ConnectionHandler connectionHandler = new ConnectionHandler();
        TouchPadFragment touchPadFragment = TouchPadFragment.newInstance();
        touchPadFragment.setConnectionHandler(connectionHandler);

        fragments[0] = touchPadFragment;
        fragments[1] = MediaKeysFragment.newInstance();

        binding.topAppBar.setNavigationOnClickListener((event) -> {
            finish();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab select
                setFragment(fragments[binding.tabLayout.getSelectedTabPosition()]);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselect
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselect
            }
        });


        thread = new Thread(connectionHandler);
        thread.start();

        setFragment(fragments[0]);
    }

    private void setFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}