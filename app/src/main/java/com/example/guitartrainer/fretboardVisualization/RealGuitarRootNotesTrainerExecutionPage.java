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


public class RealGuitarRootNotesTrainerExecutionPage extends RealGuitarExecutionPage {


    TextView noteToPlayText;
    private View readFreqText;


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
    public void initViews(){
        super.initViews();

        readFreqText = requireActivity().findViewById(R.id.testo);
        noteToPlayText = requireActivity().findViewById(R.id.noteToPlayText);
    }

    @Override
    public void setNoteToPlay(){
        noteToPlay = MusicalNote.getRandomNoteWithoutOctave();
    }

    @Override
    public String getWhatToSpeak(){
        return MusicalNote.getSpeakableNoteName(noteToPlay);
    }

    @Override
    public void updateUI(){
        noteToPlayText.setText(noteToPlay.toString());
    }

    @Override
    public void initWithArguments(){
        voiceSynthMode = getArguments().getBoolean(
                "noteNamesWithVoice",
                false);

        competitiveMode = getArguments().getBoolean("rootNamesCompetitiveMode");
    }

    @Override
    public void navigateToMainPage(){
        Navigation.findNavController(getView()).navigate(R.id.action_fretboardVisualizationRootNotesTrainer_to_fretboardVisualizationMainPage2);
    }
}

