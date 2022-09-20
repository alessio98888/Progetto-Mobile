package com.example.guitartrainer.fretboardVisualization;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.guitartrainer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FakeGuitarRootNotesTrainerExecutionPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FakeGuitarRootNotesTrainerExecutionPage extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fake_guitar_root_notes_trainer_execution_page, container, false);
    }
}