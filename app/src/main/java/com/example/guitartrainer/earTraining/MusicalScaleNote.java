package com.example.guitartrainer.earTraining;

public class MusicalScaleNote extends MusicalNote{

    private int musicalFunction;

    public MusicalScaleNote(MusicalNote musicalNote, int musicalFunction){
        super(musicalNote.getNoteName(), musicalNote.getOctave());
        this.musicalFunction = musicalFunction;
    }

    public int getMusicalFunction() {
        return musicalFunction;
    }

    public void setMusicalFunction(int musicalFunction) {
        this.musicalFunction = musicalFunction;
    }
}
