package com.example.guitartrainer.fretboardVisualization;

import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;

import java.util.Locale;


public class ExecutionPage extends Fragment implements Observer {
    int currentRound = 0;
    static final int MAX_ROUND = 3;
    TextView currentRoundText;
    long startTimestamp;
    boolean gameEnded = false;

    boolean voiceSynthMode;
    boolean competitiveMode;
    boolean fakeGuitarMode;

    MusicalNote.MusicalNoteName noteToPlay;
    TextToSpeech textToSpeech;
    UtteranceProgressListener utteranceProgressListener;
    boolean doneSpeaking;

    int resultTime;

    NoteSource noteSource;

    // initNoteSource() -> if fake: ... else if real ...
    // If fake fretboard in arguments
    // Set landscape, enable fake fretboard (enable the fragment that contains the fake fretboard)
    // Observe the fake fretboard (instead of running the tuner)
    // When a click on the fretboard happens, inform listeners of new note and check if equal to the note to play

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initWithArguments();
        initViews();

        NoteSourceFactory noteSourceFactory = new NoteSourceFactory();
        if(fakeGuitarMode){

            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            noteSource = noteSourceFactory.create(NoteSourceTypes.NoteSourceType.FakeGuitarStandardTuning22Frets);

            FrameLayout fakeGuitarFragmentContainer =
                    requireActivity().findViewById(R.id.fretboard_visualization_root_notes_fragment_container);

            requireActivity().getFragmentManager().beginTransaction().
                    add(fakeGuitarFragmentContainer.getId(), (FakeGuitar) noteSource, "someTag2").commit();
            //setLayout(landscape)
            //fragmentContainer.add(noteSource);

        } else {
            noteSource = noteSourceFactory.create(NoteSourceTypes.NoteSourceType.NoteRecognizer);
        }

        noteSource.register(this);

        if (voiceSynthMode)
            initTextToSpeech();

        checkPermissionsAndStartGame();
    }

    @Override
    public void update(Object o) {
        MusicalNote.MusicalNoteName playedNote = (MusicalNote.MusicalNoteName) o;
        if(!gameEnded && playedNote != null){
            if(voiceSynthMode){
                if(doneSpeaking){
                    calculateIfMatchedNote(playedNote);
                }
            } else {
                calculateIfMatchedNote(playedNote);
            }
        }
    }

    public void calculateIfMatchedNote(MusicalNote.MusicalNoteName playedNote){
        if(playedNote.equals(noteToPlay)){
            nextRound();
        }
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
        throw new UnsupportedOperationException();
    }

    public void initViews() {
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
    }

    public void firstThingsToDoAfterGameEnds() {
        throw new UnsupportedOperationException();
    }

    public void nextRound(){

        if(competitiveMode){
            if(gameIsEnded()){
                resultTime = (int) (System.currentTimeMillis()/1000 - startTimestamp);
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

    public boolean gameIsEnded(){
        return currentRound == MAX_ROUND;
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
        throw new UnsupportedOperationException();
    }

    public void initRound() {
        setNoteToPlay();

        updateUI();

        if (voiceSynthMode) {
            String whatToSpeak = getWhatToSpeak();
            speak(whatToSpeak);
        }
    }

    public String getWhatToSpeak() {
         throw new UnsupportedOperationException();
    }

    public void setNoteToPlay() {
        throw new UnsupportedOperationException();
    }

    public void speak(String whatToSpeak) {
        doneSpeaking = false;
        textToSpeech.speak(whatToSpeak,
                TextToSpeech.QUEUE_FLUSH, null,
                TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
    }

    @SuppressLint("SetTextI18n")
    public void updateUI(){
        throw new UnsupportedOperationException();
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

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onDetach() {
        super.onDetach();

        closeResources();
        if(voiceSynthMode) {
            textToSpeech.shutdown();
        }
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void closeResources(){
        noteSource.closeSource();
    }


}