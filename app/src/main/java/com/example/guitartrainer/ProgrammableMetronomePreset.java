/*
 * @(#) ProgrammableMetronomePreset.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import androidx.annotation.NonNull;

/**
 *
 Defines a programmable metronome preset.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class ProgrammableMetronomePreset {

    private String presetName;
    private int fromBpm;
    private int toBpm;
    private int seconds;
    private ProgrammableIncrementBpm.ModeName mode;

    public ProgrammableMetronomePreset(String presetName, int fromBpm, int toBpm, int seconds,
                                       ProgrammableIncrementBpm.ModeName mode) {
        this.presetName = presetName;
        this.fromBpm = fromBpm;
        this.toBpm = toBpm;
        this.seconds = seconds;
        this.mode = mode;
    }

    @NonNull
    @Override
    public String toString(){
        return presetName + ": (" + fromBpm + "->" + toBpm + " in " + seconds + " seconds"
                + ", mode: " + mode.toString() + ")";
    }
    public String getPresetName() {
        return presetName;
    }

    public int getFromBpm() {
        return fromBpm;
    }

    public int getToBpm() {
        return toBpm;
    }

    public int getSeconds() {
        return seconds;
    }

    public ProgrammableIncrementBpm.ModeName getMode() {
        return mode;
    }
}
