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

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.EarTrainingCardStatsProvider;
import com.example.guitartrainer.earTraining.EarTrainingGuessFunctionLevel;
import com.example.guitartrainer.earTraining.EarTrainingMainPage;


public class FretboardVisualizationMainPage extends Fragment {
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
    // Option defaults
    private final boolean DEFAULT_NOTE_NAMES_WITH_VOICE = false;
    private final boolean DEFAULT_ROOT_NAMES_COMPETITIVE_MODE = false;
    private SharedPreferences.Editor editor;
    // Actual option values
    private boolean actualNoteNamesWithVoice = DEFAULT_NOTE_NAMES_WITH_VOICE;
    private boolean actualRootNotesCompetitiveMode = DEFAULT_ROOT_NAMES_COMPETITIVE_MODE;

    ActivityResultLauncher<Intent> optionsActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        boolean noteNamesWithVoiceFromPref = data.getBooleanExtra(
                                "noteNamesWithVoice",
                                DEFAULT_NOTE_NAMES_WITH_VOICE
                        );

                        setActualNoteNamesWithVoice(noteNamesWithVoiceFromPref);

                        editor.putBoolean("noteNamesWithVoice",
                                noteNamesWithVoiceFromPref);
                        editor.apply();
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
        Intent intent = new Intent(getContext(), FretboardVisualizationOptionsActivity.class);
        intent.putExtra("noteNamesWithVoice", actualNoteNamesWithVoice);
        optionsActivityResultLauncher.launch(intent);
    }

    public void setActualNoteNamesWithVoice(boolean actualNoteNamesWithVoice) {
        this.actualNoteNamesWithVoice = actualNoteNamesWithVoice;
    }

    public void setActualRootNotesCompetitiveMode(boolean rootNotesCompetitiveMode) {
        this.actualRootNotesCompetitiveMode = rootNotesCompetitiveMode;

        editor.putBoolean("rootNotesCompetitiveMode", actualRootNotesCompetitiveMode);
        editor.apply();
        updateRootNamesCardBackground();
    }

    public void updateRootNamesCardBackground(){
        ConstraintLayout cardBackground = requireActivity().findViewById(R.id.rootNotesVisualizationCostraintLayout);
        if(actualRootNotesCompetitiveMode){
            cardBackground.setBackgroundResource(R.color.rootNotesCompetitive);
        } else {
            cardBackground.setBackgroundResource(R.color.rootNotesNotCompetitive);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        setActualNoteNamesWithVoice(sharedPref.getBoolean("noteNamesWithVoice", DEFAULT_NOTE_NAMES_WITH_VOICE));
        setActualRootNotesCompetitiveMode(sharedPref.getBoolean("rootNotesCompetitiveMode", DEFAULT_ROOT_NAMES_COMPETITIVE_MODE));
        updateRootNamesCardBackground();

        ImageView rootNotesButton = requireActivity().findViewById(R.id.rootNotesButton);
        rootNotesButton.setClipToOutline(true);
        rootNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("noteNamesWithVoice", actualNoteNamesWithVoice);
                bundle.putBoolean("rootNamesCompetitiveMode", actualRootNotesCompetitiveMode);

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


        overrideBackButtonBehaviour();
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