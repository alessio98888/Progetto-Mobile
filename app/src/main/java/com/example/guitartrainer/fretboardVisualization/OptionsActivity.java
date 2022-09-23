package com.example.guitartrainer.fretboardVisualization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.guitartrainer.R;

public class OptionsActivity extends AppCompatActivity {

    SwitchCompat voiceSynthModeSwitch;
    SwitchCompat fakeGuitarModeSwitch;
    Button saveAndExit;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fretboard_visualization_options);

        initViews();
        initVoiceSynthSwitch();
        initFakeGuitarSwitch();

        setAcceptButtonsClickListeners();
    }

    private void initViews() {
        voiceSynthModeSwitch = findViewById(R.id.fretboard_visualization_answersWithVoiceSwitch);
        fakeGuitarModeSwitch = findViewById(R.id.fretboard_visualization_fakeGuitarSwitch);

        saveAndExit = findViewById(R.id.fretboard_visualization_options_save_and_exit);
        cancel = findViewById(R.id.fretboard_visualization_options_cancel);
    }

    private void initVoiceSynthSwitch() {
        voiceSynthModeSwitch.setChecked(getIntent().getBooleanExtra(
                Options.VOICE_SYNTH_MODE_KEY,
                false));
    }

    private void initFakeGuitarSwitch() {
        fakeGuitarModeSwitch.setChecked(getIntent().getBooleanExtra(
                Options.FAKE_GUITAR_MODE_KEY,
                false));
    }

    public void setAcceptButtonsClickListeners(){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        saveAndExit.setOnClickListener(view -> {
            Intent data = new Intent();
            data.putExtra(Options.VOICE_SYNTH_MODE_KEY, voiceSynthModeSwitch.isChecked());
            data.putExtra(Options.FAKE_GUITAR_MODE_KEY, fakeGuitarModeSwitch.isChecked());
            setResult(RESULT_OK, data);
            finish();
        });
    }
}