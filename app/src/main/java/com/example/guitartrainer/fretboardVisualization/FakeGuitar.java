package com.example.guitartrainer.fretboardVisualization;

import android.content.res.Resources;
import android.os.Bundle;


import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FakeGuitar extends Fragment implements NoteSource {


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_FRETBOARD_TUNING = "fretboardTuning";

    FretboardTuning tuning;

    public View view;

    List<Observer> observers = new ArrayList<>();
    private ImageView settings;

    public FakeGuitar() {
        // Required empty public constructor
    }

    /**
     *
     */
    public static FakeGuitar newInstance(FretboardTuning fretboardTuning) {

        FakeGuitar fragment = new FakeGuitar();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FRETBOARD_TUNING, (Serializable) fretboardTuning);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tuning = (FretboardTuning) getArguments().getSerializable(ARG_FRETBOARD_TUNING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fake_guitar_scaled, container, false);

        setButtonNoteListeners(view);

        return view;
    }

    private void setButtonNoteListeners(View view) {
        for(int fret = 0; fret <= tuning.getMAX_FRET(); fret++){
            for(int string = 1; string <= tuning.getSTRING_NUM(); string++){

                Button buttonNote = getButtonNote(view, fret, string);

                int finalFret = fret;
                int finalString = string;
                buttonNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(Observer o: observers){

                            MusicalNote.MusicalNoteName playedNote =
                                    tuning.getMap().get(new GuitarFretboardNotesKey(finalFret, finalString));

                            o.update(playedNote);
                        }
                    }
                });
            }
        }
    }

    private Button getButtonNote(View view, int fret, int string) {
        Resources res = getResources();
        String id = "button_note_" + fret + "_" + string;
        int resourceId = res.getIdentifier(id, "id", getContext().getPackageName());

        Button buttonNote = view.findViewById(resourceId);
        if(buttonNote == null){
            Log.e("Fake Guitar Error", "Button note not found -> fret: " + fret + ", string: " + string);
            String idDefault = "button_note_" + 0 + "_" + 1;
            int resourceIdDefault = res.getIdentifier(idDefault, "id", getContext().getPackageName());
            return view.findViewById(resourceIdDefault);
        }
        return buttonNote;
    }

    @Override
    public MusicalNote.MusicalNoteName getNote() {
        return null;
    }

    @Override
    public void startSource() {

    }

    @Override
    public void closeSource() {

    }

    @Override
    public void register(Observer o) {
        observers.add(o);
    }
}