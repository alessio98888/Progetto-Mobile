/*
 * @(#) metronome_basic.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 *
 UI for the basic metronome.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class MetronomeBasic extends Fragment{

    private SoundPlayerMetronome metronomeSoundPlayer;
    private final int MAX_BPM = 300;
    private final int MIN_BPM = 40;


    private boolean metronomePlaying = false;


    private ImageButton playButton;
    private SharedPreferences sharedPref;
    private EditNumberManager editNumberManager;

    public MetronomeBasic(){
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_metronome_basic, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int defaultBpm = Integer.parseInt((getResources().getString(R.string.metronome_default_bpm)));
        int newBpm = sharedPref.getInt(getString(R.string.basic_metronome_bpm_key), defaultBpm);

        playButton = view.findViewById(R.id.basic_metronome_play);

        metronomeSoundPlayer = new SoundPlayerMetronome( getActivity(), newBpm);
        editNumberManager = new EditNumberManager(getView(), R.id.editBpm,
                R.id.basic_metronome_minus, R.id.basic_metronome_plus, newBpm,
                metronomeSoundPlayer, MAX_BPM, MIN_BPM);
        setListeners();
    }



    private void setListeners() {
        playButton.setOnClickListener((View v) -> playButtonClick());
    }

    public void setMetronomePlaying(boolean metronomePlaying){
        this.metronomePlaying = metronomePlaying;
    }

    public void playButtonClick() {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.basic_metronome_bpm_key),
                getEditNumberManager().getNumber());
        editor.apply();

        if (!metronomePlaying) {
            setMetronomePlaying(true);
            playButton.setImageResource(R.drawable.pause);
            metronomeSoundPlayer.start(getEditNumberManager().getNumber(), 0);
        } else {
            setMetronomePlaying(false);
            metronomeSoundPlayer.pause();
            playButton.setImageResource(R.drawable.play);
        }
    }

    public SoundPlayerMetronome getMetronomeSoundPlayer() {
        return metronomeSoundPlayer;
    }

    public EditNumberManager getEditNumberManager() {
        return editNumberManager;
    }

    public ImageButton getPlayButton() {
        return playButton;
    }

    public boolean isMetronomePlaying() {
        return metronomePlaying;
    }
}