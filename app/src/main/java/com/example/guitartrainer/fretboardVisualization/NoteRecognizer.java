package com.example.guitartrainer.fretboardVisualization;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.example.guitartrainer.earTraining.MusicalNote;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Yarden
 * Date: Oct 19, 2009
 * Time: 10:48:47 PM
 */
public class NoteRecognizer extends Thread implements NoteSource{


    static {
        System.loadLibrary("FFT");
    }

    public native double processSampleData(byte[] sample, int sampleRate);

    private static final int[] OPT_SAMPLE_RATES = {11025, 8000, 22050, 44100};
    private static final int[] BUFFERSIZE_PER_SAMPLE_RATE = {8 * 1024, 4 * 1024, 16 * 1024, 32 * 1024};

    private static final int MIN_FREQUENCY_ALLOWED = 190;
    public double currentFrequency = 0.0;
    public double currentVolume = 0.0;

    List<Observer> observers = new ArrayList<>();

    int SAMPLE_RATE = 8000;
    int READ_BUFFERSIZE = 4 * 1024;

    AudioRecord targetDataLine_;

    final Handler mHandler = new Handler();
    final Runnable callback = new Runnable() {
        public void run() {
            //readFreqText.setText(Double.toString(tuner.currentVolume));

           for(Observer o : observers){
               o.update(getNote());
           }
        }
    };

    public NoteRecognizer() {

    }

    @Override
    public MusicalNote.MusicalNoteName getNote(){
        if(currentFrequency > MIN_FREQUENCY_ALLOWED) {
            double frequency = FrequencyOperations.normaliseFreq(currentFrequency);
            int noteIdx = FrequencyOperations.closestNote(frequency);
            MusicalNote.MusicalNoteName playedNote = FrequencyOperations.NOTES[noteIdx];
            return playedNote;
        }
        return null;
    }

    @Override
    public void register(Observer o){
        observers.add(o);
    }

    private void initAudioRecord() {
        int counter = 0;
        for (int sampleRate : OPT_SAMPLE_RATES) {
            initAudioRecord(sampleRate);
            if (targetDataLine_.getState() == AudioRecord.STATE_INITIALIZED) {
                SAMPLE_RATE = sampleRate;
                READ_BUFFERSIZE = BUFFERSIZE_PER_SAMPLE_RATE[counter];
                break;
            }
            counter++;
        }
    }

    @SuppressLint("MissingPermission")
    private void initAudioRecord(int sampleRate) {

        targetDataLine_ = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                android.media.AudioFormat.CHANNEL_CONFIGURATION_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                sampleRate * 6
        );
    }

    byte[] bufferRead;
    //    long l;
    public void run(){       // fft

        targetDataLine_.startRecording();
        bufferRead = new byte[READ_BUFFERSIZE];
        int n = -1;
        while ( (n = targetDataLine_.read(bufferRead, 0,READ_BUFFERSIZE)) > 0 ) {
//            l = System.currentTimeMillis();
            currentFrequency = processSampleData(bufferRead,SAMPLE_RATE);
//            System.out.println("process time  = " + (System.currentTimeMillis() - l));



            if(currentFrequency > 0){
                mHandler.post(callback);
                try {
                    targetDataLine_.stop();
                    Log.e("Volume: ", Double.toString(calculateVolume(bufferRead)));
                    Log.e("Freq: ", Double.toString(currentFrequency));
                    //currentVolume = calculateVolume(bufferRead);
                    Thread.sleep(20);
                    targetDataLine_.startRecording();
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    return;
                }
            }
        }

    }

    private Double calculateVolume(byte[] bufferRead){
        double average = 0.0;
        double bufferSize = READ_BUFFERSIZE;
        for (byte s : bufferRead)
        {
            if(s>0)
            {
                average += Math.abs(s);
            }
            else
            {
                bufferSize--;
            }
        }
        //x=max;
        double x = average/bufferSize;
        Log.d("Volume tuner", "Average"+x );
        double db=0;
        if (x==0){
            //ERROR
        }
        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        double pressure = x/51805.5336; //the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
        Log.d("Volume tuner", "x="+pressure +" Pa");

        double REFERENCE = 0.00002;
        db = (20 * Math.log10(pressure/REFERENCE));
        Log.d("Volume tuner", "db="+db);
        if(db>0)
        {
            return x;
        }
        else{
            throw new RuntimeException();
        }
    }

    @Override
    public void closeSource(){
        //targetDataLine_.stop();
        targetDataLine_.release();
    }

    @Override
    public void startSource(){

        initAudioRecord();
        start();
    }

}

