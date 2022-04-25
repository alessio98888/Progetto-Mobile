package com.example.guitartrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EarTrainingGuessFunctionOptionsActivity extends AppCompatActivity {
    SwitchCompat automaticAnswersWithVoice;
    Button saveAndExit;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ear_training_guess_function_options);

        automaticAnswersWithVoice = findViewById(R.id.answersWithVoiceSwitch);

        // Set starting value for the switch
        automaticAnswersWithVoice.setChecked(getIntent().getBooleanExtra(
                "automaticAnswersWithVoice",
                false));

        saveAndExit = findViewById(R.id.save_and_exit);
        cancel = findViewById(R.id.cancel);

        setAcceptButtonsClickListeners();
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
            setResult(RESULT_OK, data);
            finish();
        });
    }
}