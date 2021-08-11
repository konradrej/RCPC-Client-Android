package com.konradrej.remotemouseclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.text.method.Touch;
import android.util.Log;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.konradrej.remotemouseclient.databinding.ActivityRemoteControlBinding;

public class RemoteControlActivity extends AppCompatActivity {

    private ActivityRemoteControlBinding binding;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoteControlBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.topAppBar.setNavigationOnClickListener((event) -> {
            finish();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab select
                switch (binding.tabLayout.getSelectedTabPosition()){
                    case 0:
                        setFragment(TouchPadFragment.newInstance());
                        break;
                    case 1:
                        setFragment(MediaKeysFragment.newInstance());
                        break;
                }
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

        setFragment(new TouchPadFragment());

        thread = new Thread(new ConnectionHandler());
        thread.start();
    }

    private void setFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}