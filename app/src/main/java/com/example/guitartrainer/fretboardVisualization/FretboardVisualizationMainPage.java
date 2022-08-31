package com.example.guitartrainer.fretboardVisualization;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.guitartrainer.R;


public class FretboardVisualizationMainPage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fretboard_visualization_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView rootNotesButton = requireActivity().findViewById(R.id.rootNotesButton);
        rootNotesButton.setClipToOutline(true);
        rootNotesButton.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_fretboardVisualizationMainPage2_to_fretboardVisualizationRootNotesTrainer));

    }
}