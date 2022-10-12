package com.example.guitartrainer.fretboardVisualization;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;


public class RootNotesTrainerExecutionPage extends ExecutionPage {


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

        currentRoundText = requireView().findViewById(R.id.fretboardRootVisualizationCurrentRoundText);
        noteToPlayText = requireActivity().findViewById(R.id.noteToPlayText);
    }

    @Override
    public void generateNoteToPlay(){
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
                Options.VOICE_SYNTH_MODE_KEY,
                false);

        competitiveMode = getArguments().getBoolean(Options.COMPETITIVE_MODE_KEY);

        fakeGuitarMode = getArguments().getBoolean(Options.FAKE_GUITAR_MODE_KEY);
    }

    @Override
    public void initFakeGuitar() {
        TextView t = requireActivity().findViewById(R.id.noteToPlayText);
        //t.setTextSize(30);
    }

}

