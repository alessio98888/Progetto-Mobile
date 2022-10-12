package com.example.guitartrainer.earTraining;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.guitartrainer.R;

public class GuessFunctionOptionsActivity extends AppCompatActivity {
    SwitchCompat automaticAnswersWithVoice;
    ProgressionVelocitySpinner progressionVelocities;
    Button saveAndExit;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ear_training_guess_function_options);

        findViews();

        setStartingValueForTheAutomaticAnswersVoice();
        setProgressionVelocitySpinnerSelection();
        setAcceptButtonsClickListeners();
    }

    private void findViews() {
        automaticAnswersWithVoice = findViewById(R.id.answersWithVoiceSwitch);
        saveAndExit = findViewById(R.id.save_and_exit);
        cancel = findViewById(R.id.cancel);
        progressionVelocities = findViewById(R.id.progressionVelocitySpinner);

    }

    private void setStartingValueForTheAutomaticAnswersVoice() {
        automaticAnswersWithVoice.setChecked(getIntent().getBooleanExtra(
                "automaticAnswersWithVoice",
                false));
    }

    private void setProgressionVelocitySpinnerSelection() {
        progressionVelocities.setSelected(getIntent().getFloatExtra(
                Options.PROGRESSION_VELOCITY_KEY,
                1.0f));
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
            data.putExtra("automaticAnswersWithVoice", automaticAnswersWithVoice.isChecked());
            data.putExtra(Options.PROGRESSION_VELOCITY_KEY, progressionVelocities.getSelectedItem());
            setResult(RESULT_OK, data);
            finish();
        });
    }
}