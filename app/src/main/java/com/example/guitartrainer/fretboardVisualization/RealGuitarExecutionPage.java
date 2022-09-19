package com.example.guitartrainer.fretboardVisualization;

import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
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
import androidx.navigation.Navigation;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;
import com.example.guitartrainer.earTraining.MusicalScaleNote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;


public class RealGuitarExecutionPage extends Fragment {
    final int minFrequencyAllowed = 190;
    int currentRound = 0;
    static final int MAX_ROUND = 3;
    TextView currentRoundText;
    long startTimestamp;
    boolean gameEnded = false;

    PlayFunctionsLevel.LevelType levelType;
    MusicalScale.ScaleMode scaleMode;
    String cardUniqueId;
    boolean voiceSynthMode;
    ArrayList<MusicalNote.MusicalNoteName> rootNotesNames;
    ArrayList<Integer> functionsToPlay;
    boolean competitiveMode;

    TextToSpeech textToSpeech;
    UtteranceProgressListener utteranceProgressListener;
    boolean doneSpeaking;


    TunerEngine tuner;
    final Handler mHandler = new Handler();
    final Runnable callback = new Runnable() {
        public void run() {
            //readFreqText.setText(Double.toString(tuner.currentVolume));

            if(!gameEnded){
                if(voiceSynthMode){
                    if(doneSpeaking){
                        calculateIfMatchedNote(tuner.currentFrequency);
                    }
                } else {
                    calculateIfMatchedNote(tuner.currentFrequency);
                }
            }
        }

    };

    MusicalNote.MusicalNoteName currentRootNote;
    TextView rootNoteText;

    int currentFunctionToPlay;
    TextView functionToPlayText;

    MusicalNote.MusicalNoteName noteToPlay;

    TextView scaleModeText;

    public void calculateIfMatchedNote(double frequency){
        if(frequency > minFrequencyAllowed){
            frequency = FrequencyOperations.normaliseFreq(frequency);
            int note = FrequencyOperations.closestNote(frequency);
            double matchFreq = FrequencyOperations.FREQUENCIES[note];

            if(FrequencyOperations.NOTES[note].equals(noteToPlay)){
                nextRound();
            }
            //readFreqText.setText(FrequencyOperations.NOTES[note].toString());
        }
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initWithArguments();

        initViews();

        if (voiceSynthMode)
            initTextToSpeech();

        checkPermissionsAndStartGame();
    }

    public void checkPermissionsAndStartGame() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.fretboardVisualizationRecordAudioPermissionText),
                    Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            if(voiceSynthMode){
                // Already started after the initialization of the voice synth
                return;
            }
            startGame();
        }
    }

    public void initWithArguments(){
        voiceSynthMode = getArguments().getBoolean(
                "automaticAnswersWithVoice",
                false);

        scaleMode = MusicalScale.ScaleMode.values()[
                getArguments().getInt("scaleMode")];

        cardUniqueId = getArguments().getString("cardUniqueId");

        levelType = PlayFunctionsLevel.LevelType.values()[
                getArguments().getInt("levelType")];

        int[] rootNotesArray = getArguments().getIntArray("rootNotes");
        rootNotesNames = MusicalNote.toMusicalNotesNames(
                (ArrayList<Integer>) Arrays.stream(rootNotesArray).boxed().collect(Collectors.toList()));

        functionsToPlay = (ArrayList<Integer>) Arrays.stream(getArguments().getIntArray("functionsToPlay")).boxed().collect(Collectors.toList());

        competitiveMode = getArguments().getBoolean("competitiveMode");
    }

    public void initViews() {
        currentRoundText = requireView().findViewById(R.id.fretboardPlayFunctionCurrentRoundText);

        rootNoteText = requireView().findViewById(R.id.playFunctionsExecutionRootNoteText);
        functionToPlayText = requireView().findViewById(R.id.playFunctionsExecutionFunctionToPlayText);
        scaleModeText = requireView().findViewById(R.id.playFunctionsExecutionScaleMode);
        scaleModeText.setText(scaleMode.toString());
    }

    public final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
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
            initGame();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(competitiveMode){
            startTimestamp = System.currentTimeMillis()/1000;
        }
        nextRound();
    }

    public void initGame() {
        tuner = new TunerEngine(mHandler,callback);
        tuner.start();
    }

    public void firstThingsToDoAfterGameEnds()
    {

    }

    public boolean gameIsEnded(){
        return currentRound == MAX_ROUND;
    }
    public void nextRound(){

        if(competitiveMode){
            if(gameIsEnded()){
                int resultTime = (int) (System.currentTimeMillis()/1000 - startTimestamp);
                firstThingsToDoAfterGameEnds();

                showCompetitiveResults(resultTime);
                gameEnded = true;
                return;
            }
            currentRound += 1;
            updateCurrentRoundText();
        }

        initRound();


    }

    public void showCompetitiveResults(int resultTime){
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

    public void updateCurrentRoundText(){
        String text = String.format(getResources().getString(R.string.ear_training_round_text),
                Integer.toString(currentRound), Integer.toString(MAX_ROUND));
        currentRoundText.setText(text);

    }

    public void navigateToMainPage(){
        Navigation.findNavController(getView()).navigate(R.id.action_playFunctionExecutionPage_to_fretboardVisualizationMainPage2);
    }

    public void initRound() {
        generateRoundQuestions();

        updateUI();

        if (voiceSynthMode) {

            String whatToSpeak = getWhatToSpeak();
            speak(whatToSpeak);
        }
    }

    public String getWhatToSpeak() {
        return MusicalNote.getSpeakableNoteName(currentRootNote) + currentFunctionToPlay;
    }

    public void generateRoundQuestions() {
        Random rand = new Random();
        currentRootNote = rootNotesNames.get(rand.nextInt(rootNotesNames.size()));

        MusicalScaleNote randomNote = MusicalScale.getRandomScaleNote(currentRootNote, scaleMode, functionsToPlay);
        currentFunctionToPlay = randomNote.getMusicalFunction();
        noteToPlay = randomNote.getNoteName();
    }

    public void speak(String whatToSpeak) {
        doneSpeaking = false;
        textToSpeech.speak(whatToSpeak,
                TextToSpeech.QUEUE_FLUSH, null,
                TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);

    }

    @SuppressLint("SetTextI18n")
    public void updateUI(){
        functionToPlayText.setText(Integer.toString(currentFunctionToPlay));
        rootNoteText.setText(currentRootNote.toString());
    }

    public void initTextToSpeech(){
        utteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                Log.i("TextToSpeech","On Done");

                doneSpeaking = true;
                //getActivity().runOnUiThread(() -> playNextRound());
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
    @Override
    public void onDetach() {
        super.onDetach();

        if(voiceSynthMode) {
            textToSpeech.shutdown();
        }
    }
}