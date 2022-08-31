package com.example.guitartrainer.fretboardVisualization;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;

import java.util.StringTokenizer;


public class FretboardVisualizationRootNotesTrainer extends Fragment {

    MusicalNote.MusicalNoteName noteToPlay;
    TextView noteToPlayText;

    TextView readFreqText;

    TunerEngine tuner;
    final Handler mHandler = new Handler();
    final Runnable callback = new Runnable() {
        public void run() {
            readFreqText.setText(Double.toString(tuner.currentFrequency));
            calculateIfMatchedNote(tuner.currentFrequency);
//            System.out.println("tuner.currentFrequency = " + tuner.currentFrequency);
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

        generateNextNoteToPlay();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        readFreqText = getActivity().findViewById(R.id.testo);
        noteToPlayText = requireActivity().findViewById(R.id.noteToPlayText);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.fretboardVisualizationRecordAudioPermissionText),
                    Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            startGame();
        }

    }

    public void calculateIfMatchedNote(double frequency){
        readFreqText.setText(Double.toString(frequency));
        if(frequency > 70){
            frequency = FrequenceOperations.normaliseFreq(frequency);
            int note = FrequenceOperations.closestNote(frequency);
            double matchFreq = FrequenceOperations.FREQUENCIES[note];

            if(FrequenceOperations.NOTES[note].equals(noteToPlay)){
                generateNextNoteToPlay();
            }
        }


        //readFreqText.setText(FrequenceOperations.NOTES[note].toString());

    }

    public void generateNextNoteToPlay(){
        noteToPlay = MusicalNote.getRandomNoteWithoutOctave();
        noteToPlayText.setText(noteToPlay.toString());

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
    }

    public void closeTuner(){
            tuner.close();
    }
}