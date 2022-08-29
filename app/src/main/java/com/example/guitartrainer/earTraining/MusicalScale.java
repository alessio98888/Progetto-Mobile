package com.example.guitartrainer.earTraining;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MusicalScale {
    public enum ScaleMode {
        Major,
        Minor
    }


    private MusicalNote rootNote;
    private ScaleMode scaleMode;

    public static Map<ScaleMode, int[]> scaleFormulas = new HashMap<ScaleMode, int[]>() {
        {
            put(ScaleMode.Major, new int[]{0, 2, 2, 1, 2, 2, 2, 1});
            put(ScaleMode.Minor, new int[]{0, 2, 1, 2, 2, 1, 2, 2});
        }
    };

    public MusicalScale(MusicalNote rootNote, ScaleMode scaleMode) {
        this.rootNote = rootNote;
        this.scaleMode = scaleMode;
    }

    @NonNull
    public String toString(){
        String s = "";
        s+=rootNote.getFormattedNoteName();
        s+=" ";
        s+=scaleMode.toString();
        return s;
    }

    public MusicalScaleNote getRandomScaleNote(){
        return MusicalScale.getRandomScaleNote(getRootNote().getNoteName(), getScaleMode());
    }

    public static MusicalScaleNote getRandomScaleNote(
            MusicalNote.MusicalNoteName rootNote, ScaleMode scaleMode){

        Random rand = new Random();
        int[] formula = scaleFormulas.get(scaleMode);
        assert formula != null;

        // scale function generation (-1: not root octave)
        int formulaIndex = rand.nextInt(formula.length-1);

        int distanceFromRoot = 0;

        for (int i = 0; i<=formulaIndex; i++){
           distanceFromRoot+=formula[i];
        }

        ArrayList<MusicalNote.MusicalNoteName> noteNames =
                (ArrayList<MusicalNote.MusicalNoteName>) MusicalNote.getMusicalNotes();

        MusicalNote note = new MusicalNote(
                noteNames.get((rootNote.ordinal() + distanceFromRoot) % noteNames.size()), 0);

        return new MusicalScaleNote(note, formulaIndex + 1);
    }

    public static ArrayList<MusicalNote.MusicalNoteName> getScaleNotes(MusicalNote.MusicalNoteName rootNote,
                                                                       ScaleMode scaleMode) {
        ArrayList<MusicalNote.MusicalNoteName> scaleNotes = new ArrayList<>();
        int[] formula = scaleFormulas.get(scaleMode);
        assert formula != null;

        int distanceFromRoot = 0;

        for(int formulaIndex=0; formulaIndex<formula.length;formulaIndex++) {
            distanceFromRoot += formula[formulaIndex];

            ArrayList<MusicalNote.MusicalNoteName> noteNames =
                    (ArrayList<MusicalNote.MusicalNoteName>) MusicalNote.getMusicalNotes();

            scaleNotes.add(noteNames.get((rootNote.ordinal() + distanceFromRoot) % noteNames.size()));
        }
        return scaleNotes;
    }

    public MusicalNote getRootNote() {
        return rootNote;
    }

    public void setRootNote(MusicalNote rootNote) {
        this.rootNote = rootNote;
    }

    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
    }
}
