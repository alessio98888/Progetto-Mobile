package com.example.guitartrainer.fretboardVisualization;

import com.example.guitartrainer.earTraining.MusicalNote;

import java.io.Serializable;
import java.util.Map;

public abstract class FretboardTuning implements Serializable {
    private static final long serialVersionUID = 0L;

    abstract Map<GuitarFretboardNotesKey, MusicalNote.MusicalNoteName> getMap();

    abstract int getMAX_FRET();

    abstract int getSTRING_NUM();
}
