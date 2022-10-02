package com.example.guitartrainer.fretboardVisualization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.guitartrainer.R;


public class MainPage extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fretboard_visualization_main_page, container, false);
    }

    private SharedPreferences.Editor editor;

    // Option defaults
    private static final boolean DEFAULT_NOTE_NAMES_WITH_VOICE = false;
    private static final boolean DEFAULT_FAKE_GUITAR_MODE = false;
    private static final boolean DEFAULT_COMPETITIVE_MODE = false;

    // Actual option values
    private boolean actualNoteNamesWithVoice = DEFAULT_NOTE_NAMES_WITH_VOICE;
    private boolean actualFakeGuitarMode = DEFAULT_FAKE_GUITAR_MODE;
    private boolean actualCompetitiveMode = DEFAULT_COMPETITIVE_MODE;

    ActivityResultLauncher<Intent> optionsActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        boolean noteNamesWithVoiceFromOptions = data.getBooleanExtra(
                                Options.VOICE_SYNTH_MODE_KEY,
                                DEFAULT_NOTE_NAMES_WITH_VOICE
                        );

                        boolean fakeGuitarModeFromOptions = data.getBooleanExtra(
                                Options.FAKE_GUITAR_MODE_KEY,
                                DEFAULT_FAKE_GUITAR_MODE
                        );

                        boolean competitiveModeFromOptions = data.getBooleanExtra(
                                Options.COMPETITIVE_MODE_KEY,
                                DEFAULT_COMPETITIVE_MODE
                        );
                        setActualFakeGuitarMode(fakeGuitarModeFromOptions);
                        setActualNoteNamesWithVoice(noteNamesWithVoiceFromOptions);
                        setActualCompetitiveMode(competitiveModeFromOptions);
                    }
                }
            });

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.ear_training_guess_function_options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.guess_function_options) {
            openOptionsActivityForResult();
        }
        return false;
    }

    public void openOptionsActivityForResult() {
        Intent intent = new Intent(getContext(), OptionsActivity.class);

        intent.putExtras(getOptionsBundle());
        optionsActivityResultLauncher.launch(intent);
    }

    public void setActualFakeGuitarMode(boolean actualFakeGuitarMode) {
       this.actualFakeGuitarMode = actualFakeGuitarMode;

       editor.putBoolean(Options.FAKE_GUITAR_MODE_KEY,
                actualFakeGuitarMode);
       editor.apply();
    }

    public void setActualCompetitiveMode(boolean actualCompetitiveMode) {
        this.actualCompetitiveMode = actualCompetitiveMode;

        editor.putBoolean(Options.COMPETITIVE_MODE_KEY,
                actualCompetitiveMode);
        editor.apply();
    }

    public void setActualNoteNamesWithVoice(boolean actualNoteNamesWithVoice) {
        this.actualNoteNamesWithVoice = actualNoteNamesWithVoice;

        editor.putBoolean(Options.VOICE_SYNTH_MODE_KEY,
                actualNoteNamesWithVoice);
        editor.apply();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        setActualNoteNamesWithVoice(sharedPref.getBoolean(Options.VOICE_SYNTH_MODE_KEY, DEFAULT_NOTE_NAMES_WITH_VOICE));
        setActualFakeGuitarMode(sharedPref.getBoolean(Options.FAKE_GUITAR_MODE_KEY, DEFAULT_FAKE_GUITAR_MODE));
        setActualCompetitiveMode(sharedPref.getBoolean(Options.COMPETITIVE_MODE_KEY, DEFAULT_COMPETITIVE_MODE));

        initRootNotesButton();
        initPlayFunctionsButton();

        overrideBackButtonBehaviour();
    }

    private void initRootNotesButton(){
        ImageView rootNotesButton = requireActivity().findViewById(R.id.rootNotesButton);
        rootNotesButton.setClipToOutline(true);
        rootNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(
                        R.id.action_fretboardVisualizationMainPage2_to_fretboardVisualizationRootNotesTrainer, getOptionsBundle());
            }
        });
    }

    private void initPlayFunctionsButton(){
        ImageView playFunctionButton = requireActivity().findViewById(R.id.playFunctionsButton);
        playFunctionButton.setClipToOutline(true);
        playFunctionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(
                        R.id.action_fretboardVisualizationMainPage2_to_playFunctionsMainPage, getOptionsBundle());
            }
        });
    }

    public Bundle getOptionsBundle(){
        Bundle bundle = new Bundle();
        bundle.putBoolean(Options.VOICE_SYNTH_MODE_KEY, actualNoteNamesWithVoice);
        bundle.putBoolean(Options.FAKE_GUITAR_MODE_KEY, actualFakeGuitarMode);
        bundle.putBoolean(Options.COMPETITIVE_MODE_KEY, actualCompetitiveMode);
        return bundle;
    }

    public void overrideBackButtonBehaviour() {
        // Lines necessary in order to set the key listener
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();

        this.getView().setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    ((Activity) getView().getContext()).finish();
                    return true;
                }
                return false;
            }
        });
    }
}