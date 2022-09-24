package com.example.guitartrainer.fretboardVisualization;

public class NoteSourceFactory {
    NoteSource create(NoteSourceTypes.NoteSourceType noteSourceType){
       if(noteSourceType == NoteSourceTypes.NoteSourceType.FakeGuitarStandardTuning22Frets){
           return FakeGuitar.newInstance(new FretboardTuningStandard(22));
       } else if(noteSourceType == NoteSourceTypes.NoteSourceType.NoteRecognizer){
           return new NoteRecognizer();
       } else{
           return null;
       }
    }
}
