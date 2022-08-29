/*
 * @(#) ProgrammableIncrementBpm.java     1.0 05/01/2022
 */

package com.example.guitartrainer.metronome;


import android.os.Handler;

/**
 *
 Implements a programmable metronome that increase the bpm linearly
 over a programmable number of seconds from a starting bpm to an ending bpm.
 The programmable metronome provides a parameter called "mode" that modifies the behaviour of
 the metronome when the bpm reaches the ending bpm. Mode can be in one of the following states:
    - stay: when the ending bpm is reached continue to playing that bpm.
    - stop/reset: when the ending bpm is reached stop playing the metronome
                  and reset the actual bpm to the starting bpm.
    - loop: when the ending bpm is reached start over.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class ProgrammableIncrementBpm implements Runnable{
    private Handler handler;
    private SoundPlayerMetronome metronomePlayer;


    private int fromBpm;
    private int toBpm;
    private final int DEFAULT_FROM_BPM = 120;
    private final int DEFAULT_TO_BPM = 160;


    private boolean inPause = false;
    private int seconds;
    private int actualBpm;


    private boolean isPlaying = false;

    private long millisDelay;

    private Observer actualBpmObserver;


    private ModeName currentMode = ModeName.Loop;

    public enum ModeName{
        Loop,
        StopReset,
        Stay
    }

    public void coreProgrammableIncrementBpm(SoundPlayerMetronome metronomePlayer, int fromBpm,
                                             int toBpm, int seconds, Observer actualBpmObserver){
        this.actualBpmObserver = actualBpmObserver;

        handler = new Handler();
        this.metronomePlayer = metronomePlayer;
        setBpms(fromBpm, toBpm);
        setSeconds(seconds);
        actualBpm = fromBpm;
    }

    public ProgrammableIncrementBpm(SoundPlayerMetronome metronomePlayer, int fromBpm, int toBpm,
                                    int seconds, ModeName currentMode, Observer actualBpmObserver){

        coreProgrammableIncrementBpm(metronomePlayer, fromBpm, toBpm, seconds, actualBpmObserver);
        this.currentMode = currentMode;
    }

    public ProgrammableIncrementBpm(SoundPlayerMetronome metronomePlayer, int fromBpm, int toBpm,
                                    int seconds, Observer actualBpmObserver){
        coreProgrammableIncrementBpm(metronomePlayer, fromBpm, toBpm, seconds, actualBpmObserver);
    }

    @Override
    public void run() {
        if (isPlaying()){
            ObserverData obsData = new ObserverData();
            obsData.value = actualBpm;
            obsData.updatingClassInstance = this;
            actualBpmObserver.update(obsData);

            handler.postDelayed(this, millisDelay );

            if (isInPause()) { }
            else if (actualBpm < toBpm) {
                actualBpm+=1;
                metronomePlayer.setBpm(actualBpm);
            } else if (actualBpm == toBpm ) {
                if (currentMode == ModeName.Loop) {
                    stopAndReset();
                    start();
                } else if (currentMode == ModeName.StopReset) {
                    stopAndReset();
                } else if (currentMode == ModeName.Stay){ }
            } else {
                stopAndReset();
            }


        }
    }

    public void stopAndReset(){
        metronomePlayer.setBpm(fromBpm);
        actualBpm = fromBpm;
        ObserverData obsData = new ObserverData();
        obsData.value = actualBpm;
        obsData.updatingClassInstance = this;
        actualBpmObserver.update(obsData);
        metronomePlayer.pause();
        handler.removeCallbacks(this);
        isPlaying = false;
    }

    public void start(){
        setInPause(false);
        millisDelay = (long) ( ((float) (seconds * 1000)) / ( (float) (getToBpm() - getFromBpm() ) ) );
        isPlaying = true;
        actualBpm = fromBpm;
        handler.post(this);
        metronomePlayer.start(actualBpm, 0);
    }

    // Getters and Setters

    public boolean isInPause() {
        return inPause;
    }

    public void setInPause(boolean inPause) {
        this.inPause = inPause;
    }

    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

    public void setBpms(int fromBpm, int toBpm)
    {
        if (fromBpm >= toBpm) {
            this.fromBpm = DEFAULT_FROM_BPM;
            this.toBpm = DEFAULT_TO_BPM;
        }
        else {
            this.fromBpm = fromBpm;
            this.toBpm = toBpm;
        }
    }

    public int getFromBpm() {
        return fromBpm;
    }

    public int getToBpm() {
        return toBpm;
    }

    public void setFromBpm(int fromBpm) {
        if (fromBpm < this.toBpm) {
            this.fromBpm = fromBpm;
        }
        else {
            this.fromBpm = DEFAULT_FROM_BPM;
        }
    }

    public void setToBpm(int toBpm) {
        if (toBpm > this.fromBpm) {
            this.toBpm = toBpm;
        }
        else {
            this.toBpm = DEFAULT_TO_BPM;
        }
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setCurrentMode(ModeName currentMode) {
        this.currentMode = currentMode;
    }

    public ModeName getCurrentMode() {
        return currentMode;
    }
}
