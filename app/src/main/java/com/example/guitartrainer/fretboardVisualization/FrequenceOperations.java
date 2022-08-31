package com.example.guitartrainer.fretboardVisualization;

import com.example.guitartrainer.earTraining.MusicalNote;

public class FrequenceOperations {
    static final double[] FREQUENCIES = { 77.78, 82.41, 87.31, 92.50, 98.00, 103.83, 110.00, 116.54, 123.47, 130.81, 138.59, 146.83, 155.56, 164.81 ,174.61};
    static final String[] NAME        = {  "D#",  "E",   "F",   "F#"  , "G" ,  "G#",   "A",    "A#",   "B",   "C",     "C#",   "D",   "D#"   ,"E"  ,   "F" };
    static final MusicalNote.MusicalNoteName[] NOTES = {
            MusicalNote.MusicalNoteName.eb,
            MusicalNote.MusicalNoteName.e,
            MusicalNote.MusicalNoteName.f,
            MusicalNote.MusicalNoteName.gb,
            MusicalNote.MusicalNoteName.g ,
            MusicalNote.MusicalNoteName.ab,
            MusicalNote.MusicalNoteName.a,
            MusicalNote.MusicalNoteName.bb,
            MusicalNote.MusicalNoteName.b,
            MusicalNote.MusicalNoteName.c,
            MusicalNote.MusicalNoteName.db,
            MusicalNote.MusicalNoteName.d,
            MusicalNote.MusicalNoteName.eb,
            MusicalNote.MusicalNoteName.e,
            MusicalNote.MusicalNoteName.f };
    static double normaliseFreq(double hz) {
        // get hz into a standard range to make things easier to deal with

        while ( hz < 82.41 ) {
            hz = 2*hz;
        }
        while ( hz > 164.81 ) {
            hz = 0.5*hz;
        }
        return hz;
    }

    static int closestNote(double hz) {
        double minDist = Double.MAX_VALUE;
        int minFreq = -1;
        for ( int i = 0; i < FREQUENCIES.length; i++ ) {
            double dist = Math.abs(FREQUENCIES[i]-hz);
            if ( dist < minDist ) {
                minDist=dist;
                minFreq=i;
            }
        }
//        minFreq = minFreq == 13 ? 1 : minFreq;
        return minFreq;
    }
}
