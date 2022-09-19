package com.example.guitartrainer.fretboardVisualization;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
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
import com.example.guitartrainer.earTraining.CardStatsProvider;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;
import com.example.guitartrainer.earTraining.MusicalScaleNote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;


public class RealGuitarPlayFunctionExecutionPage extends Fragment {
    int currentRound = 0;
    static final int MAX_ROUND = 3;
    TextView currentRoundText;
    long startTimestamp;
    boolean gameEnded = false;

    private PlayFunctionsLevel.LevelType levelType;
    private MusicalScale.ScaleMode scaleMode;
    private String cardUniqueId;
    private boolean noteNamesWithVoice;
    private ArrayList<MusicalNote.MusicalNoteName> rootNotesNames;
    private ArrayList<Integer> functionsToPlay;
    private boolean competitiveMode;

    private TextToSpeech textToSpeech;
    private UtteranceProgressListener utteranceProgressListener;
    private boolean doneSpeaking;


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

    private MusicalNote.MusicalNoteName currentRootNote;
    private TextView rootNoteText;

    private int currentFunctionToPlay;
    private TextView functionToPlayText;

    private MusicalNote.MusicalNoteName noteToPlay;

    private TextView scaleModeText;

    public void calculateIfMatchedNote(double frequency){
        if(frequency > 190){
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_function_execution_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noteNamesWithVoice = getArguments().getBoolean(
                "automaticAnswersWithVoice",
                false);

        if (noteNamesWithVoice) {
            initTextToSpeech();
        }

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

        if (!noteNamesWithVoice) {

        }
        currentRoundText = requireView().findViewById(R.id.fretboardPlayFunctionCurrentRoundText);

        rootNoteText = requireView().findViewById(R.id.playFunctionsExecutionRootNoteText);
        functionToPlayText = requireView().findViewById(R.id.playFunctionsExecutionFunctionToPlayText);
        scaleModeText = requireView().findViewById(R.id.playFunctionsExecutionScaleMode);
        scaleModeText.setText(scaleMode.toString());
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

        if(competitiveMode){
            startTimestamp = System.currentTimeMillis()/1000;
        }
        nextRound();
    }

    public void saveLevelStats(int successSeconds){
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                PlayFunctionsCardStatsProvider.CONTENT_URI);

        PlayFunctionsCardStatsProvider provider =
                (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();

        provider.insertOrUpdateCard(new CardStats(
                cardUniqueId,
                successSeconds,
                levelType
        ));
    }

    public void nextRound(){

        if(competitiveMode){
            if(currentRound == MAX_ROUND){
                int resultTime = (int) (System.currentTimeMillis()/1000 - startTimestamp);

                saveLevelStats(resultTime);
                showCompetitiveResults(resultTime);
                gameEnded = true;
                return;
            }
            currentRound += 1;
            updateCurrentRoundText();
        }

        generateNextRootNoteAndFunctionToPlay();

        if (noteNamesWithVoice) {
            doneSpeaking = false;
            textToSpeech.speak(MusicalNote.getSpeakableNoteName(currentRootNote) + currentFunctionToPlay,
                    TextToSpeech.QUEUE_FLUSH, null,
                    TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);

        }
    }

    private void showCompetitiveResults(int resultTime){
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

    private void updateCurrentRoundText(){
        String text = String.format(getResources().getString(R.string.ear_training_round_text),
                Integer.toString(currentRound), Integer.toString(MAX_ROUND));
        currentRoundText.setText(text);

    }

    private void navigateToMainPage(){
        Navigation.findNavController(getView()).navigate(R.id.action_playFunctionExecutionPage_to_fretboardVisualizationMainPage2);
    }

    private void generateNextRootNoteAndFunctionToPlay() {
        Random rand = new Random();
        currentRootNote = rootNotesNames.get(rand.nextInt(rootNotesNames.size()));

        MusicalScaleNote randomNote = MusicalScale.getRandomScaleNote(currentRootNote, scaleMode, functionsToPlay);
        currentFunctionToPlay = randomNote.getMusicalFunction();
        noteToPlay = randomNote.getNoteName();

        updateWhatToPlayInUI();
    }

    @SuppressLint("SetTextI18n")
    public void updateWhatToPlayInUI(){
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

        if(noteNamesWithVoice) {
            textToSpeech.shutdown();
        }
    }
}