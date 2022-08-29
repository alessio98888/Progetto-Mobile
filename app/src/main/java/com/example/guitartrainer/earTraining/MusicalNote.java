package com.example.guitartrainer.earTraining;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MusicalNote {
    public enum MusicalNoteName {
        ab("Ab"),a("A"),bb("Bb"),b("B"),c("C"),db("Db"),d("D"),eb("Eb"),e("E"),f("F"),gb("Gb"),g("G"),
        all("All Notes");

        private String noteName;

        MusicalNoteName(String noteName) {
            this.noteName = noteName;
        }

        @NonNull
        @Override
        public String toString() {
            return this.noteName;
        }
    }

    private MusicalNoteName noteName;
    private int octave;

    public MusicalNote(MusicalNoteName noteName, int octave) {
        this.noteName = noteName;
        this.octave = octave;
    }

    public MusicalNoteName getNoteName() {
        return noteName;
    }

    public String getFormattedNoteName() {
        String noteNameString = noteName.toString();
        noteNameString = setCharAtUpper(0, noteNameString);
        return noteNameString;
    }

    public String setCharAtUpper(int index, String s){

        String returnString = s;
        StringBuilder noteNameBuilder = new StringBuilder(returnString);
        noteNameBuilder.setCharAt(index,
                String.valueOf(returnString.charAt(index)).toUpperCase(Locale.ROOT).charAt(0));
        return noteNameBuilder.toString();
    }


    static public List<String> getNoteNames(){
        ArrayList<String> noteNames = new ArrayList<String>();

        for (int i = 0; i < MusicalNoteName.values().length-1; i++) {
            noteNames.add(MusicalNoteName.values()[i].name());
        }

        return noteNames;
    }

    static public List<MusicalNoteName> getMusicalNotes(){
        ArrayList<MusicalNoteName> noteNames = new ArrayList<>();

        for (int i = 0; i < MusicalNoteName.values().length-1; i++) {
            noteNames.add(MusicalNoteName.values()[i]);
        }

        return noteNames;
    }

    static public List<Integer> getMusicalNotesOrdinals(){
        ArrayList<Integer> noteOrdinals = new ArrayList<>();

        for (int i = 0; i < MusicalNoteName.values().length-1; i++) {
            noteOrdinals.add(MusicalNoteName.values()[i].ordinal());
        }

        return noteOrdinals;
    }

    public static ArrayList<MusicalNoteName> toMusicalNotesNames(ArrayList<Integer> noteOrdinals){
        ArrayList<MusicalNoteName> notes = new ArrayList<>();
        for (int i=0; i< noteOrdinals.size(); i++){
            notes.add(MusicalNoteName.values()[noteOrdinals.get(i)]);
        }
        return notes;
    }
    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }
}
