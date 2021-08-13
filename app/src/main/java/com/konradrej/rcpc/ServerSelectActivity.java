package com.konradrej.rcpc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.konradrej.rcpc.R;
import com.konradrej.rcpc.databinding.ActivityServerSelectBinding;

public class ServerSelectActivity extends AppCompatActivity {

    private ActivityServerSelectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServerSelectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.topAppBar.setOnMenuItemClickListener((menuItem) -> {
            if (menuItem.getItemId() == R.id.settings){
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
            }

            return true;
        });

        binding.button.setOnClickListener((event) -> {
            Intent intent = new Intent(this, RemoteControlActivity.class);
            startActivity(intent);
        });

        //binding.outlinedIpField.setError("Test");
    }
}