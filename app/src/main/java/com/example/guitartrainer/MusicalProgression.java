package com.example.guitartrainer;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class MusicalProgression {
   public enum MusicalProgressionId {
      i_iv_i_v("I-IV-I-V");

      private String progressionName;

      MusicalProgressionId(String progressionName) {
         this.progressionName = progressionName;
      }

      @NonNull
      @Override
      public String toString() {
         return this.progressionName;
      }
   }



   public MusicalProgressionId getProgressionEnum() {
      return progressionEnum;
   }

   public boolean isAsc() {
      return asc;
   }

   private MusicalNote.MusicalNoteName rootNote;
   private MusicalProgressionId progressionEnum;
   private boolean asc;
   private MusicalScale.ScaleMode scaleMode;

   public MusicalProgression(MusicalNote.MusicalNoteName rootNote,
                             MusicalProgressionId progressionNumber,
                             boolean asc,
                             MusicalScale.ScaleMode scaleMode){
      this.rootNote = rootNote;
      this.progressionEnum = progressionNumber;
      this.asc = asc;
      this.scaleMode = scaleMode;
   }

   public String getReadableProgressionEnum(){
      String s = getProgressionEnum().toString();
      s = s.toUpperCase(Locale.ROOT);
      return s.replace("_", "-");
   }

   public static int getResId(MusicalNote.MusicalNoteName rootNote,
                              MusicalProgressionId progressionNumber,
                              boolean asc,
                              MusicalScale.ScaleMode scaleMode){
      String progressionAudioFileName = rootNote.name() + "_" + progressionNumber.name() + "_";
      Integer resId = null;
      if (asc) {
         progressionAudioFileName+="asc";
      } else {
         progressionAudioFileName+="desc";
      }
      progressionAudioFileName+="_"+scaleMode.toString().toLowerCase(Locale.ROOT);

      try {
         resId = (Integer) R.raw.class.getField(progressionAudioFileName).getInt(null);
      } catch (IllegalAccessException | NoSuchFieldException e) {
         e.printStackTrace();
      }
      return resId;
   }

   public int getResId(){
      return MusicalProgression.getResId(getRootNote(), getProgressionEnum(), isAsc(), getScaleMode());
   }

   public static ArrayList<MusicalProgressionId> toMusicalProgressions(
           ArrayList<Integer> progressionOrdinals
   ){
     ArrayList<MusicalProgressionId> progressionsIds = new ArrayList<>();
     for(int i = 0; i< progressionOrdinals.size(); i++) {
        progressionsIds.add(MusicalProgressionId.values()[progressionOrdinals.get(i)]);
     }
     return progressionsIds;
   }

   public static ArrayList<Integer> toMusicalProgressionOrdinals(
           ArrayList<MusicalProgressionId> progressionsIds){
      ArrayList<Integer> progressionsOrdinals = new ArrayList<>();
      for(int i = 0; i< progressionsIds.size(); i++) {
         progressionsOrdinals.add(progressionsIds.get(i).ordinal());
      }
      return progressionsOrdinals;
   }

   public MusicalNote.MusicalNoteName getRootNote() {
      return rootNote;
   }

   public MusicalScale.ScaleMode getScaleMode(){
      return scaleMode;
   }
}
