/*
 * @(#) Metronome.java     1.0 05/01/2022
 */

package com.example.guitartrainer.metronome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.guitartrainer.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;


/**
 *
 Fragment that contains the view pager for the various type of metronomes: basic and programmable.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class Metronome extends Fragment {

    MetronomePagerAdapter metronomePagerAdapter;
    ViewPager2 viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_metronome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (metronomePagerAdapter==null) {
            metronomePagerAdapter = new MetronomePagerAdapter(this);
        }

        viewPager = view.findViewById(R.id.metronome_pager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(metronomePagerAdapter);


        ArrayList<String> metronomeTabNames = new ArrayList<String>();
        metronomeTabNames.add(getResources().getString(R.string.metronome_tab_programmable_metronome));
        metronomeTabNames.add(getResources().getString(R.string.metronome_tab_basic_metronome));

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(metronomeTabNames.get(position))
        ).attach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MetronomeBasic basic = ( (MetronomePagerAdapter) viewPager.getAdapter()).getMetronomeBasic();
        MetronomeProgrammable programmable = ( (MetronomePagerAdapter) viewPager.getAdapter()).getMetronomeProgrammable();

        programmable.getProgrammableIncrementBpm().stopAndReset();
        basic.getMetronomeSoundPlayer().pause();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(getString(R.string.basic_metronome_bpm_key), basic.getEditNumberManager().getNumber());
        editor.putInt(getString(R.string.programmable_metronome_from_bpm_key), programmable.getFromBpmManager().getNumber());
        editor.putInt(getString(R.string.programmable_metronome_to_bpm_key), programmable.getToBpmManager().getNumber());
        editor.putInt(getString(R.string.programmable_metronome_seconds_key), programmable.getSecondsManager().getNumber());
        editor.putLong(getString(R.string.programmable_metronome_mode_key), programmable.getModesSpinner().getSelectedItemId());
        if (!programmable.getCurrentPresetName().isEmpty()){
            editor.putString(getString(R.string.programmable_metronome_currentPreset_key), programmable.getCurrentPresetName());
        }
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        MetronomeBasic basic = ( (MetronomePagerAdapter) viewPager.getAdapter()).getMetronomeBasic();
        MetronomeProgrammable programmable = ( (MetronomePagerAdapter) viewPager.getAdapter()).getMetronomeProgrammable();

        programmable.getProgrammableIncrementBpm().stopAndReset();

        if(basic.isMetronomePlaying()){
            basic.playButtonClick();
        }

    }

}