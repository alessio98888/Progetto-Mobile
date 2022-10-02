package com.example.guitartrainer.fretboardVisualization;

import java.io.Serializable;

public class GuitarFretboardNotesKey implements Serializable {

    private final int fret; // From 0
    private final int string; // From 1

    public GuitarFretboardNotesKey(int fret, int string) {
        this.fret = fret;
        this.string = string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuitarFretboardNotesKey)) return false;
        GuitarFretboardNotesKey key = (GuitarFretboardNotesKey) o;
        return fret == key.fret && string == key.string;
    }

    @Override
    public int hashCode() {
        int result = fret;
        result = 31 * result + string;
        return result;
    }

}
