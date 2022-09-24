package com.example.guitartrainer.fretboardVisualization;

import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayFunctionLevelsContent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFunctionLevelsContent extends Fragment {

    private static final String ARG_MUSICAL_SCALE = "musicalScale";
    private static final String ARG_MUSICAL_NOTE = "musicalNote";
    private static final String ARG_FUNCTIONS_TO_PLAY = "functionsToPlay";
    private static final String ARG_CUSTOM_LEVEL = "customLevel";
    private static final String ARG_SUCCESS_SECONDS = "successSeconds";

    private MusicalScale.ScaleMode musicalScale;
    private MusicalNote.MusicalNoteName musicalNote;
    private ArrayList<Integer> functionsToPlay;
    private PlayFunctionsLevel.LevelType levelType;
    private int successSeconds;

    private View.OnClickListener settingsOnClickListener;
    public View view;

    private ImageView settings;

    public PlayFunctionLevelsContent() {
        // Required empty public constructor
    }

    /**
     *
     */
    public static PlayFunctionLevelsContent newInstance(
           PlayFunctionsLevel level
    ) {
        MusicalScale.ScaleMode musicalScale = level.getMusicalScale();
        MusicalNote.MusicalNoteName musicalNote = level.getMusicalNote();
        ArrayList<Integer> functionsToPlay = level.getFunctionsToPlay();
        PlayFunctionsLevel.LevelType customLevel = level.getLevelType();
        int successSeconds = level.getSuccessSeconds();

        PlayFunctionLevelsContent fragment = new PlayFunctionLevelsContent();
        Bundle args = new Bundle();
        args.putInt(ARG_MUSICAL_SCALE, musicalScale.ordinal());
        args.putInt(ARG_MUSICAL_NOTE, musicalNote.ordinal());
        args.putIntegerArrayList(ARG_FUNCTIONS_TO_PLAY, functionsToPlay);
        args.putInt(ARG_CUSTOM_LEVEL, customLevel.ordinal());
        args.putInt(ARG_SUCCESS_SECONDS, successSeconds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            musicalScale = MusicalScale.ScaleMode.values()[getArguments().getInt(ARG_MUSICAL_SCALE)];
            musicalNote = MusicalNote.MusicalNoteName.values()[getArguments().getInt(ARG_MUSICAL_NOTE)];
            functionsToPlay = getArguments().getIntegerArrayList(ARG_FUNCTIONS_TO_PLAY);
            levelType = PlayFunctionsLevel.LevelType.values()[getArguments().getInt(ARG_CUSTOM_LEVEL)];
            successSeconds = getArguments().getInt(ARG_SUCCESS_SECONDS);
        }
    }

    public boolean savedSuccessSeconds(){
        return successSeconds > 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_play_functions_levels_content, container, false);
        TextView rootNoteText = view.findViewById(R.id.rootNotePlayFunctionText);
        TextView scaleText = view.findViewById(R.id.scalePlayFunctionText);
        TextView functionsToPlayText = view.findViewById(R.id.functionsToPlayText);
        TextView successSecondsText = view.findViewById(R.id.successSecondsText);
        if(savedSuccessSeconds()){
            String text = String.format(getResources().getString(R.string.seconds),
                    successSeconds);
            successSecondsText.setText(text);
        }
        settings = view.findViewById(R.id.playFunctionLevelSettings);

        if(levelType == PlayFunctionsLevel.LevelType.Custom){
            settings.setVisibility(View.VISIBLE);
            settings.setOnClickListener(settingsOnClickListener);
        }
        rootNoteText.setText(musicalNote.toString());
        scaleText.setText(musicalScale.toString());

        StringBuilder functionsToPlayString = new StringBuilder();
        for(Integer i : functionsToPlay){
            functionsToPlayString.append(i).append(" ");
        }
        functionsToPlayText.setText(functionsToPlayString.toString());
        return view;
    }

    public void setSettingsOnClickListener(View.OnClickListener listener){
       settingsOnClickListener = listener;
    }

    /*
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


    }

     */
}