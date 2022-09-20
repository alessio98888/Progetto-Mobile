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


public class RealGuitarPlayFunctionExecutionPage extends RealGuitarExecutionPage {

    private PlayFunctionsLevel.LevelType levelType;
    private MusicalScale.ScaleMode scaleMode;
    private String cardUniqueId;
    private ArrayList<MusicalNote.MusicalNoteName> rootNotesNames;
    private ArrayList<Integer> functionsToPlay;


    private MusicalNote.MusicalNoteName currentRootNote;
    private TextView rootNoteText;

    private int currentFunctionToPlay;
    private TextView functionToPlayText;


    private TextView scaleModeText;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_function_execution_page, container, false);
    }

    @Override
    public void initViews(){
        super.initViews();

        currentRoundText = requireView().findViewById(R.id.fretboardPlayFunctionCurrentRoundText);

        rootNoteText = requireView().findViewById(R.id.playFunctionsExecutionRootNoteText);
        functionToPlayText = requireView().findViewById(R.id.playFunctionsExecutionFunctionToPlayText);
        scaleModeText = requireView().findViewById(R.id.playFunctionsExecutionScaleMode);
        scaleModeText.setText(scaleMode.toString());
    }

    @Override
    public void setNoteToPlay(){
        Random rand = new Random();
        currentRootNote = rootNotesNames.get(rand.nextInt(rootNotesNames.size()));

        MusicalScaleNote randomNote = MusicalScale.getRandomScaleNote(currentRootNote, scaleMode, functionsToPlay);
        currentFunctionToPlay = randomNote.getMusicalFunction();
        noteToPlay = randomNote.getNoteName();
    }

    @Override
    public String getWhatToSpeak(){
        return MusicalNote.getSpeakableNoteName(currentRootNote) + currentFunctionToPlay;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateUI(){
        functionToPlayText.setText(Integer.toString(currentFunctionToPlay));
        rootNoteText.setText(currentRootNote.toString());
    }

    @Override
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

    @Override
    public void navigateToMainPage(){
        Navigation.findNavController(getView()).navigate(R.id.action_playFunctionExecutionPage_to_fretboardVisualizationMainPage2);
    }

    @Override
    public void firstThingsToDoAfterGameEnds(){
        saveLevelStats(resultTime);
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
}