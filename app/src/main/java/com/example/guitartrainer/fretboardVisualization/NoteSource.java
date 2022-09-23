package com.example.guitartrainer.fretboardVisualization;

import com.example.guitartrainer.earTraining.MusicalNote;


public interface NoteSource extends Observable {
    MusicalNote.MusicalNoteName getNote();
    void startSource();
    void closeSource();
}
