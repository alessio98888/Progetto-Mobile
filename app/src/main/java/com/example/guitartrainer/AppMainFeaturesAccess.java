/*
 * @(#) Home.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.guitartrainer.earTraining.EarTrainingActivity;
import com.example.guitartrainer.fretboardVisualization.FretboardVisualizationActivity;


/**
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class AppMainFeaturesAccess extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (getView().findViewById(R.id.fragment_home_layout));
        FeatureButton metronome_button = new FeatureButton(
                getActivity(),
                layout,
                getResources().getString(R.string.feature_name_metronome),
                R.drawable.metronome_feature_button_icon,
                R.id.action_home2_to_metronome);
        layout.addView(metronome_button);

        FeatureButton ear_training_button = new FeatureButton(
                getActivity(),
                layout,
                getResources().getString(R.string.feature_name_ear_training),
                R.drawable.ic_baseline_hearing_24,
                EarTrainingActivity.class);

        layout.addView(ear_training_button);

        FeatureButton fretboard_visualization_button = new FeatureButton(
                getActivity(),
                layout,
                getResources().getString(R.string.fretboardVisualizationFeatureButton),
                android.R.drawable.ic_menu_mapmode,
                FretboardVisualizationActivity.class);

        layout.addView(fretboard_visualization_button);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}