package com.example.guitartrainer.fretboardVisualization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.guitartrainer.R;

public class OptionsActivity extends AppCompatActivity {
    SwitchCompat noteNamesWithVoice;
    Button saveAndExit;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fretboard_visualization_options);

        findViews();
        setStartingValueForTheAutomaticAnswersVoiceToFalse();
        setAcceptButtonsClickListeners();
    }

    private void findViews() {
        noteNamesWithVoice = findViewById(R.id.fretboard_visualization_answersWithVoiceSwitch);
        saveAndExit = findViewById(R.id.fretboard_visualization_options_save_and_exit);
        cancel = findViewById(R.id.fretboard_visualization_options_cancel);
    }

    private void setStartingValueForTheAutomaticAnswersVoiceToFalse() {
        noteNamesWithVoice.setChecked(getIntent().getBooleanExtra(
                "noteNamesWithVoice",
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
            data.putExtra("noteNamesWithVoice", noteNamesWithVoice.isChecked());
            setResult(RESULT_OK, data);
            finish();
        });
    }
}