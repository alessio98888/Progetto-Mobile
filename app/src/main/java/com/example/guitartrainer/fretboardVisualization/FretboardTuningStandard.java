package com.example.guitartrainer.fretboardVisualization;

import com.example.guitartrainer.earTraining.MusicalNote;

import java.util.HashMap;
import java.util.Map;

public class FretboardTuningStandard extends FretboardTuning{
    private final int MAX_FRET;
    private final int STRING_NUM;
    private static final int DEFAULT_STRING_NUM = 6;

    private boolean getMapAlreadyCalled;
    private Map<GuitarFretboardNotesKey, MusicalNote.MusicalNoteName> fretBoardMap;


    public FretboardTuningStandard(int maxFret){
        MAX_FRET = maxFret;
        STRING_NUM = DEFAULT_STRING_NUM;
    }
    public FretboardTuningStandard(int maxFret, int stringNum){
        MAX_FRET = maxFret;
        STRING_NUM = stringNum;
    }

    Map<Integer, MusicalNote.MusicalNoteName> fretboardOpenStringsMapStandardTuning =
            new HashMap<Integer, MusicalNote.MusicalNoteName>() {
                {
                    put(1, MusicalNote.MusicalNoteName.e);
                    put(2, MusicalNote.MusicalNoteName.b);
                    put(3, MusicalNote.MusicalNoteName.g);
                    put(4, MusicalNote.MusicalNoteName.d);
                    put(5, MusicalNote.MusicalNoteName.a);
                    put(6, MusicalNote.MusicalNoteName.e);
                }
            };

    @Override
    public Map<GuitarFretboardNotesKey, MusicalNote.MusicalNoteName> getMap() {
        if(getMapAlreadyCalled)
            return fretBoardMap;
        getMapAlreadyCalled = true;

        Map<GuitarFretboardNotesKey, MusicalNote.MusicalNoteName> standardMap
                = new HashMap<GuitarFretboardNotesKey, MusicalNote.MusicalNoteName>();

        for(int fret = 0; fret <= MAX_FRET; fret++){
            for(int string = 1; string <= STRING_NUM; string++){

                MusicalNote.MusicalNoteName openStringNote =
                        fretboardOpenStringsMapStandardTuning.get(string);

                MusicalNote.MusicalNoteName noteToPut =
                        MusicalNote.MusicalNoteName.values()[
                                (openStringNote.ordinal() + fret) % 11];

                standardMap.put(new GuitarFretboardNotesKey(fret, string), noteToPut);
            }
        }

        fretBoardMap = standardMap;
        return standardMap;
    }

    public int getMAX_FRET() {
        return MAX_FRET;
    }

    public int getSTRING_NUM() {
        return STRING_NUM;
    }
}
