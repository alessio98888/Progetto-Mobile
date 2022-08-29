package com.example.guitartrainer.earTraining;

import androidx.annotation.NonNull;

public class EarTrainingOctaveOption {

    public enum EarTrainingOctaveOptionEnum {
        One_Octave("One Octave"),
        Many_Octaves("Many Octaves");

        private String octaveOptionName;

        EarTrainingOctaveOptionEnum(String octaveOptionName) {
            this.octaveOptionName = octaveOptionName;
        }

        @NonNull
        @Override
        public String toString() {
            return this.octaveOptionName;
        }
    }


    private EarTrainingOctaveOptionEnum octaveOption;

    public EarTrainingOctaveOption(EarTrainingOctaveOptionEnum octaveOption) {
        this.octaveOption = octaveOption;
    }

    public EarTrainingOctaveOptionEnum getOctaveOption() {
        return octaveOption;
    }

    public String getReadableOctaveOption(){

        String s = octaveOption.toString();
        return s.replace("_", " ");
    }

}
