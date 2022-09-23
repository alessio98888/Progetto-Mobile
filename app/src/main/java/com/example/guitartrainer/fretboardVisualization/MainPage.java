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
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.PopupMenu;

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

    private static final boolean DEFAULT_ROOT_NAMES_COMPETITIVE_MODE = false;
    private static final boolean DEFAULT_PLAY_FUNCTIONS_COMPETITIVE_MODE = false;

    // Actual option values
    private boolean actualNoteNamesWithVoice = DEFAULT_NOTE_NAMES_WITH_VOICE;
    private boolean actualFakeGuitarMode = DEFAULT_FAKE_GUITAR_MODE;
    private boolean actualRootNotesCompetitiveMode = DEFAULT_ROOT_NAMES_COMPETITIVE_MODE;
    private boolean actualPlayFunctionsCompetitiveMode = DEFAULT_PLAY_FUNCTIONS_COMPETITIVE_MODE;

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

                        setActualFakeGuitarMode(fakeGuitarModeFromOptions);
                        setActualNoteNamesWithVoice(noteNamesWithVoiceFromOptions);
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
        intent.putExtra(Options.VOICE_SYNTH_MODE_KEY, actualNoteNamesWithVoice);
        intent.putExtra(Options.FAKE_GUITAR_MODE_KEY, actualFakeGuitarMode);
        optionsActivityResultLauncher.launch(intent);
    }

    public void setActualFakeGuitarMode(boolean actualFakeGuitarMode) {
       this.actualFakeGuitarMode = actualFakeGuitarMode;

       editor.putBoolean(Options.FAKE_GUITAR_MODE_KEY,
                actualFakeGuitarMode);
       editor.apply();
    }

    public void setActualNoteNamesWithVoice(boolean actualNoteNamesWithVoice) {
        this.actualNoteNamesWithVoice = actualNoteNamesWithVoice;

        editor.putBoolean(Options.VOICE_SYNTH_MODE_KEY,
                actualNoteNamesWithVoice);
        editor.apply();
    }

    public void setActualRootNotesCompetitiveMode(boolean rootNotesCompetitiveMode) {
        this.actualRootNotesCompetitiveMode = rootNotesCompetitiveMode;

        editor.putBoolean(Options.ROOT_NOTES_COMPETITIVE_MODE_KEY, actualRootNotesCompetitiveMode);
        editor.apply();
        updateRootNamesCardBackground();
    }

    public void setActualPlayFunctionsCompetitiveMode(boolean playFunctionsCompetitiveMode){
       this.actualPlayFunctionsCompetitiveMode = playFunctionsCompetitiveMode;

       editor.putBoolean(Options.PLAY_FUNCTIONS_COMPETITIVE_MODE_KEY, actualPlayFunctionsCompetitiveMode);
       editor.apply();
       updatePlayFunctionsCardBackground();
    }

    public void updatePlayFunctionsCardBackground(){
        ConstraintLayout cardBackground = requireActivity().findViewById(R.id.playFunctionsConstraintLayout);
        if(actualPlayFunctionsCompetitiveMode){
            cardBackground.setBackgroundResource(R.color.fretboard_visualization_competitive);
        } else {
            cardBackground.setBackgroundResource(R.color.fretboard_visualization_not_competitive);
        }
    }

    public void updateRootNamesCardBackground(){
        ConstraintLayout cardBackground = requireActivity().findViewById(R.id.rootNotesVisualizationCostraintLayout);
        if(actualRootNotesCompetitiveMode){
            cardBackground.setBackgroundResource(R.color.fretboard_visualization_competitive);
        } else {
            cardBackground.setBackgroundResource(R.color.fretboard_visualization_not_competitive);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        setActualNoteNamesWithVoice(sharedPref.getBoolean(Options.VOICE_SYNTH_MODE_KEY, DEFAULT_NOTE_NAMES_WITH_VOICE));
        setActualFakeGuitarMode(sharedPref.getBoolean(Options.FAKE_GUITAR_MODE_KEY, DEFAULT_FAKE_GUITAR_MODE));
        setActualRootNotesCompetitiveMode(sharedPref.getBoolean(Options.ROOT_NOTES_COMPETITIVE_MODE_KEY, DEFAULT_ROOT_NAMES_COMPETITIVE_MODE));
        setActualPlayFunctionsCompetitiveMode(sharedPref.getBoolean(Options.PLAY_FUNCTIONS_COMPETITIVE_MODE_KEY, DEFAULT_PLAY_FUNCTIONS_COMPETITIVE_MODE));

        updateRootNamesCardBackground();

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
                Bundle bundle = new Bundle();
                bundle.putBoolean(Options.VOICE_SYNTH_MODE_KEY, actualNoteNamesWithVoice);
                bundle.putBoolean(Options.FAKE_GUITAR_MODE_KEY, actualFakeGuitarMode);
                bundle.putBoolean(Options.ROOT_NOTES_COMPETITIVE_MODE_KEY, actualRootNotesCompetitiveMode);

                Navigation.findNavController(view).navigate(
                        R.id.action_fretboardVisualizationMainPage2_to_fretboardVisualizationRootNotesTrainer, bundle);
            }
        });


        ImageView rootNotesSettingsButton = requireActivity().findViewById(R.id.rootNotesVisualizationSettings);
        rootNotesSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRootNotesSettingsPopupMenu(rootNotesSettingsButton);
            }
        });
    }

    private void initPlayFunctionsButton(){
        ImageView playFunctionButton = requireActivity().findViewById(R.id.playFunctionsButton);
        playFunctionButton.setClipToOutline(true);
        playFunctionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Options.VOICE_SYNTH_MODE_KEY, actualNoteNamesWithVoice);
                bundle.putBoolean(Options.PLAY_FUNCTIONS_COMPETITIVE_MODE_KEY, actualPlayFunctionsCompetitiveMode);

                Navigation.findNavController(view).navigate(
                        R.id.action_fretboardVisualizationMainPage2_to_playFunctionsMainPage, bundle);
            }
        });


        ImageView playFunctionSettingsButton = requireActivity().findViewById(R.id.playFunctionsVisualizationSettings);
        playFunctionSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlayFunctionSettingsPopupMenu(playFunctionSettingsButton);
            }
        });
    }

    private void showPlayFunctionSettingsPopupMenu(View v){
        PopupMenu popup = new PopupMenu(requireActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.fretboard_visualization_play_functions_button_menu, popup.getMenu());

        MenuItem competitiveModeTitle = popup.getMenu().findItem(R.id.playFunctionsCompetitiveModeMenuText);
        String title;
        if(actualPlayFunctionsCompetitiveMode){
            title = getResources().getString(R.string.disableRootNotesCompetitiveMode);
        } else {
            title = getResources().getString(R.string.enableRootNotesCompetitiveMode);
        }
        competitiveModeTitle.setTitle(title);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.playFunctionsCompetitiveModeMenuText) {
                    setActualPlayFunctionsCompetitiveMode(!actualPlayFunctionsCompetitiveMode);
                }
                return false;
            }
        });
        popup.show();
    }

    private void showRootNotesSettingsPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(requireActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.fretboard_visualization_root_notes_button_menu, popup.getMenu());

        MenuItem competitiveModeTitle = popup.getMenu().findItem(R.id.rootNotesCompetitiveModeMenuText);
        String title;
        if(actualRootNotesCompetitiveMode){
            title = getResources().getString(R.string.disableRootNotesCompetitiveMode);
        } else {
            title = getResources().getString(R.string.enableRootNotesCompetitiveMode);
        }
        competitiveModeTitle.setTitle(title);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.rootNotesCompetitiveModeMenuText) {
                    setActualRootNotesCompetitiveMode(!actualRootNotesCompetitiveMode);
                }
                return false;
            }
        });
        popup.show();

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