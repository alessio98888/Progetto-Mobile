package com.example.guitartrainer.fretboardVisualization;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;


public class PlayFunctionExecutionPage extends Fragment {

    private TextToSpeech textToSpeech;
    private UtteranceProgressListener utteranceProgressListener;
    private PlayFunctionsLevel.LevelType levelType;
    private MusicalScale.ScaleMode scaleMode;
    private String cardUniqueId;
    private boolean automaticAnswersWithVoice;
    private ArrayList<MusicalNote.MusicalNoteName> rootNotesNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_function_execution_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        automaticAnswersWithVoice = getArguments().getBoolean(
                "automaticAnswersWithVoice",
                false);

        if (automaticAnswersWithVoice) {
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

        ArrayList<Integer> functionsToPlay = getArguments().getIntegerArrayList("functionsToPlay");

        if (!automaticAnswersWithVoice) {

        }
    }
    public void initTextToSpeech(){
        utteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                Log.i("TextToSpeech","On Done");

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
                } else {
                    Log.e("TTS", "Failed");
                }
            }
        });
    }
    @Override
    public void onDetach() {
        super.onDetach();

        if(automaticAnswersWithVoice) {
            textToSpeech.shutdown();
        }
    }
}