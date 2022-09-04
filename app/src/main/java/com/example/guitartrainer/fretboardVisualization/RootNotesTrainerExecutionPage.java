package com.example.guitartrainer.fretboardVisualization;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;

import java.util.Locale;


public class RootNotesTrainerExecutionPage extends Fragment {

    boolean noteNamesWithVoice;

    boolean competitiveMode;
    int currentRound = 0;
    static final int MAX_ROUND = 3;
    TextView currentRoundText;
    long startTimestamp;
    boolean gameEnded = false;

    MusicalNote.MusicalNoteName noteToPlay;
    TextView noteToPlayText;
    boolean doneSpeaking;
    TextView readFreqText;

    TextToSpeech textToSpeech;
    UtteranceProgressListener utteranceProgressListener;

    TunerEngine tuner;
    final Handler mHandler = new Handler();
    final Runnable callback = new Runnable() {
        public void run() {
            //readFreqText.setText(Double.toString(tuner.currentVolume));

            if(!gameEnded){
                if(noteNamesWithVoice){
                    if(doneSpeaking){
                        calculateIfMatchedNote(tuner.currentFrequency);
                    }
                } else {
                    calculateIfMatchedNote(tuner.currentFrequency);
                }
            }
        }

    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fretboard_visualization_root_notes_trainer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noteNamesWithVoice = getArguments().getBoolean(
                "noteNamesWithVoice",
                false);

        competitiveMode = getArguments().getBoolean(
                "rootNamesCompetitiveMode",
                false);
        currentRoundText = requireActivity().findViewById(R.id.fretboardRootVisualizationCurrentRoundText);

        if (noteNamesWithVoice) {
            initTextToSpeech();
        }

        readFreqText = getActivity().findViewById(R.id.testo);
        noteToPlayText = requireActivity().findViewById(R.id.noteToPlayText);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.fretboardVisualizationRecordAudioPermissionText),
                    Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            if(noteNamesWithVoice){
                // Already started after the initialization of the voice synth
                return;
            }
            startGame();
        }

    }

    public void initTextToSpeech() {
        utteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {

                doneSpeaking = true;
                //getActivity().runOnUiThread(() -> startGame());
            }

            @Override
            public void onError(String s) {

            }
        };
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech.setLanguage(Locale.ENGLISH);
                        Log.e("TTS", "Language not supported");
                    }

                    textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                    startGame();
                } else {
                    Log.e("TTS", "Failed");
                }
            }
        });
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean granted) {
                    if (granted) {
                        startGame();
                    } else {
                        requireActivity().finish();
                    }
                }
            }
    );

    public void startGame(){

        try {
            tuner = new TunerEngine(mHandler,callback);
            tuner.start();
        } catch (Exception e) {
            e.printStackTrace();
        }



        startTimestamp = System.currentTimeMillis()/1000;
        nextRound();
    }


    public void calculateIfMatchedNote(double frequency){
        if(frequency > 70){
            frequency = FrequencyOperations.normaliseFreq(frequency);
            int note = FrequencyOperations.closestNote(frequency);
            double matchFreq = FrequencyOperations.FREQUENCIES[note];

            if(FrequencyOperations.NOTES[note].equals(noteToPlay)){
                nextRound();
            }
            readFreqText.setText(FrequencyOperations.NOTES[note].toString());
        }



    }

    public void nextRound(){

        if(competitiveMode){
            if(currentRound == MAX_ROUND){
                Long resultTime = System.currentTimeMillis()/1000 - startTimestamp;
                showCompetitiveResults(resultTime);
                gameEnded = true;
                return;
            }
            currentRound += 1;
            updateCurrentRoundText();
        }
        noteToPlay = MusicalNote.getRandomNoteWithoutOctave();
        noteToPlayText.setText(noteToPlay.toString());
        if (noteNamesWithVoice) {
            doneSpeaking = false;
            textToSpeech.speak(MusicalNote.getSpeakableNoteName(noteToPlay),
                    TextToSpeech.QUEUE_FLUSH, null,
                    TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);

        }
    }

    private void showCompetitiveResults(Long resultTime){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        final View POPUP_VIEW =
                ((LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE )).
                        inflate(R.layout.end_of_guess_function_level_popup, null);

        Button continueButton = POPUP_VIEW.findViewById(R.id.continue_button);

        TextView resultTimeText = POPUP_VIEW.findViewById(R.id.success_perc_text_popup);
        String text = String.format(getResources().getString(R.string.seconds),
                resultTime);
        resultTimeText.setText(text);

        dialogBuilder.setView(POPUP_VIEW);
        AlertDialog dialog = dialogBuilder.create();
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                navigateToMainPage();
            }
        });

        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                navigateToMainPage();
            }
        });
    }

    private void navigateToMainPage(){
        Navigation.findNavController(getView()).navigate(R.id.action_fretboardVisualizationRootNotesTrainer_to_fretboardVisualizationMainPage2);
    }

    private void updateCurrentRoundText(){
        String text = String.format(getResources().getString(R.string.ear_training_round_text),
                Integer.toString(currentRound), Integer.toString(MAX_ROUND));
        currentRoundText.setText(text);

    }

    @Override
    public void onPause(){
        closeTuner();
        super.onPause();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        closeTuner();
        if(noteNamesWithVoice) {
            textToSpeech.shutdown();
        }
    }

    public void closeTuner(){
            tuner.close();
    }
}