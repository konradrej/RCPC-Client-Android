package com.konradrej.rcpc.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.konradrej.rcpc.R;
import com.konradrej.rcpc.databinding.ActivitySettingsBinding;

/**
 * Represents the settings activity.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Setups the activities view and interaction.
     *
     * @param savedInstanceState saved bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding =
                ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        binding.topAppBar.setNavigationOnClickListener((event) -> endSettings());
        binding.topAppBar.setNavigationContentDescription(R.string.settings_app_bar_navigation);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                endSettings();
            }
        });
    }

    private void endSettings() {
        startActivity(new Intent(getApplicationContext(), ServerSelectActivity.class));
        finish();
    }

    /**
     * Populates and setups preferences from file,
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);


            androidx.preference.EditTextPreference editTextPreference =
                    getPreferenceManager().findPreference("connection_history_entries_amount");

            // Sets keyboard for editTextPreference to numbers only
            if (editTextPreference != null)
                editTextPreference.setOnBindEditTextListener((editText) ->
                        editText.setInputType(
                                InputType.TYPE_CLASS_NUMBER |
                                        InputType.TYPE_NUMBER_FLAG_SIGNED
                        ));
        }
    }
}