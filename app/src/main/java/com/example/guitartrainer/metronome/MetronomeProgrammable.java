/*
 * @(#) metronome_programmable.java     1.0 05/01/2022
 */

package com.example.guitartrainer.metronome;

import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.guitartrainer.ProviderReturn.InsertOrUpdateReturn;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guitartrainer.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 *
 UI for the programmable metronome.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class MetronomeProgrammable extends Fragment implements Observer,
        AdapterView.OnItemSelectedListener {

    private final int DEFAULT_INITIAL_FROM_BPM = 120;
    private final int DEFAULT_INITIAL_TO_BPM = 160;
    private final int DEFAULT_INITIAL_SECONDS= 30;
    private int initialFromBpm = DEFAULT_INITIAL_FROM_BPM;
    private int initialToBpm = DEFAULT_INITIAL_TO_BPM;
    private int initialSeconds = DEFAULT_INITIAL_SECONDS;
    private ProgrammableIncrementBpm.ModeName initialMode = ProgrammableIncrementBpm.ModeName.Loop;

    private final int MAX_BPM = 300;
    private final int MIN_BPM = 40;
    private final int MAX_SECONDS = 300;
    private final int MIN_SECONDS = 10;

    private Button goButton;
    private Button stopButton;
    private Button pauseResumeButton;

    private SoundPlayerMetronome soundPlayerMetronome ;

    public ProgrammableIncrementBpm getProgrammableIncrementBpm() {
        return programmableIncrementBpm;
    }

    private ProgrammableIncrementBpm programmableIncrementBpm;


    private EditNumberManager fromBpmManager;
    private EditNumberManager toBpmManager;
    private EditNumberManager secondsManager;

    private TextView actualBpmTextView;

    private Spinner modesSpinner;

    // -- Presets --
    private TextView currentPresetTextView;

    private String currentPresetName;
    private ArrayList<ProgrammableMetronomePreset> presets;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    // Save Preset Menu
    private EditText savePresetNameEditText;
    private Button savePresetCancel;
    private Button savePresetConfirm;

    // Delete Preset Menu
    private Button deletePresetCancel;
    private Button deletePresetConfirm;

    // Load Preset Menu
    private Button loadPresetCancel;
    private Button loadPresetConfirm;

    private ProgrammableMetronomePreset presetToLoad;
    private ProgrammableMetronomePreset presetToDelete;
    // Popup list of preset
    private Spinner presetSpinner;

    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_metronome_programmable, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.programmable_metronome_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        soundPlayerMetronome = new SoundPlayerMetronome(getActivity(), DEFAULT_INITIAL_FROM_BPM);

        goButton = view.findViewById(R.id.GoButton);

        stopButton = view.findViewById(R.id.StopButton);

        pauseResumeButton = view.findViewById(R.id.pauseResumeButton);

        disableStopResetAndPauseResume();

        actualBpmTextView = view.findViewById(R.id.ActualBpmTextView);

        currentPresetTextView = view.findViewById(R.id.currentPresetTextView);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        currentPresetName = sharedPref.getString(getString(
                R.string.programmable_metronome_currentPreset_key), "");
        updateCurrentPreset(currentPresetName);

        initialFromBpm = sharedPref.getInt(getString(R.string.programmable_metronome_from_bpm_key),
                initialFromBpm);

        initialToBpm = sharedPref.getInt(getString(R.string.programmable_metronome_to_bpm_key),
                initialToBpm);

        initialSeconds = sharedPref.getInt(getString(R.string.programmable_metronome_seconds_key),
                initialSeconds);

        initialMode = ProgrammableIncrementBpm.ModeName.values()[
                (int) sharedPref.getLong(getString(R.string.programmable_metronome_mode_key),
                        initialMode.ordinal())];

        fromBpmManager = new EditNumberManager(
                getView(), R.id.from_bpm_editText, R.id.programmable_metronome_from_minus,
                R.id.programmable_metronome_from_plus, initialFromBpm, this, MAX_BPM, MIN_BPM);
        toBpmManager = new EditNumberManager(
                getView(), R.id.to_bpm_editText, R.id.programmable_metronome_to_minus,
                R.id.programmable_metronome_to_plus, initialToBpm, MAX_BPM, MIN_BPM);
        secondsManager = new EditNumberManager(
                getView(), R.id.seconds_editText, R.id.programmable_metronome_seconds_minus,
                R.id.programmable_metronome_seconds_plus, initialSeconds, MAX_SECONDS, MIN_SECONDS);

        programmableIncrementBpm = new ProgrammableIncrementBpm(
                soundPlayerMetronome, fromBpmManager.getNumber(), toBpmManager.getNumber(),
                secondsManager.getNumber(), initialMode, this);

        actualBpmTextView.setText(Integer.toString(fromBpmManager.getNumber()));

        modesSpinner = view.findViewById(R.id.ModesSpinner);
        ArrayAdapter<ProgrammableIncrementBpm.ModeName>adapter =
                new ArrayAdapter<ProgrammableIncrementBpm.ModeName>(getActivity(),
                android.R.layout.simple_spinner_item, ProgrammableIncrementBpm.ModeName.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modesSpinner.setAdapter(adapter);
        modesSpinner.setOnItemSelectedListener(this);
        modesSpinner.setSelection(initialMode.ordinal());

        setListeners();
    }

    public void disableStopResetAndPauseResume(){
        stopButton.setEnabled(false);
        pauseResumeButton.setEnabled(false);
    }

    public void enableStopResetAndPauseResume(){
        stopButton.setEnabled(true);
        pauseResumeButton.setEnabled(true);
    }

    // -- Start of Presets Management Methods --
    public ProgrammableMetronomePresetsProvider getProgrammableMetronomePresetsProvider(){
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                ProgrammableMetronomePresetsProvider.CONTENT_URI);

        return (ProgrammableMetronomePresetsProvider) client.getLocalContentProvider();
    }

    public void fillSpinnerWithPresets(Spinner spinner, Collection<ProgrammableMetronomePreset> presets){
        ArrayAdapter<ProgrammableMetronomePreset> adapter;
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                (List<ProgrammableMetronomePreset>) presets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void loadPreset(ProgrammableMetronomePreset presetToLoad){
        setModesSpinnerSelectedItem(presetToLoad.getMode());

        fromBpmManager.setNumber(presetToLoad.getFromBpm(), true);
        toBpmManager.setNumber(presetToLoad.getToBpm(), true);
        secondsManager.setNumber(presetToLoad.getSeconds(), true);
        updateCurrentPreset(presetToLoad.getPresetName());
    }

    public void setModesSpinnerSelectedItem(ProgrammableIncrementBpm.ModeName newSelectedMode){
        modesSpinner.setSelection(presetToLoad.getMode().ordinal());
        programmableIncrementBpm.setCurrentMode(presetToLoad.getMode());
    }

    public void closePresetManagementPopupWindow(AlertDialog dialog, View view){
        hideSoftKeyboard(view);
        dialog.dismiss();
    }

    public void updateCurrentPreset(String newPresetName){
        if (newPresetName.isEmpty()) {
            currentPresetTextView.setVisibility(View.INVISIBLE);
        } else {
            currentPresetName = newPresetName;
            currentPresetTextView.setText(getString(R.string.programmable_metronome_current_preset,
                    newPresetName) );
            currentPresetTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_preset) {
            createNewSavePresetDialog();
        } else if (id == R.id.load_preset) {
            createNewLoadPresetDialog();
        } else if (id == R.id.delete_preset) {
            createNewDeletePresetDialog();
        }
        return false;
    }

    public void createNewDeletePresetDialog(){
        dialogBuilder = new AlertDialog.Builder(getContext());

        final View DELETE_PRESET_VIEW = getLayoutInflater().inflate(R.layout.delete_preset_popup, null);
        deletePresetConfirm = DELETE_PRESET_VIEW.findViewById(R.id.delete_preset_button);
        deletePresetCancel = DELETE_PRESET_VIEW.findViewById(R.id.cancel_button);
        presetSpinner = DELETE_PRESET_VIEW.findViewById(R.id.preset_toDelete_spinner);

        ProgrammableMetronomePresetsProvider presetsProvider = getProgrammableMetronomePresetsProvider();

        if (presetsProvider.getNumberOfPresets() == 0) {
            Toast.makeText(getContext(), R.string.toast_no_saved_presets, Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<ProgrammableMetronomePreset> orderedPresets = presetsProvider.getOrderedPresets();
        fillSpinnerWithPresets(presetSpinner, orderedPresets);

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                presetToDelete = orderedPresets.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        presetSpinner.setSelection(0);
        presetToDelete = orderedPresets.get(0);


        dialogBuilder.setView(DELETE_PRESET_VIEW);
        dialog = dialogBuilder.create();
        dialog.show();

        deletePresetConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                presetsProvider.deletePresetByName(ProgrammableMetronomePresetsProvider.CONTENT_URI,
                                                   presetToDelete.getPresetName());

                if(presetToDelete.getPresetName().equals(getCurrentPresetName())){
                    updateCurrentPreset("");
                }
                Toast.makeText(getContext(),R.string.toast_delete_preset,Toast.LENGTH_SHORT).show();

                closePresetManagementPopupWindow(dialog, view);
            }
        });
        deletePresetCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePresetManagementPopupWindow(dialog, view);
            }
        });
    }

    public void createNewLoadPresetDialog(){
        dialogBuilder = new AlertDialog.Builder(getContext());

        final View LOAD_PRESET_VIEW = getLayoutInflater().inflate(R.layout.load_preset_popup, null);
        presetSpinner = LOAD_PRESET_VIEW.findViewById(R.id.preset_toLoad_spinner);
        loadPresetConfirm = LOAD_PRESET_VIEW.findViewById(R.id.load_preset_button);
        loadPresetCancel = LOAD_PRESET_VIEW.findViewById(R.id.cancel_button);

        ProgrammableMetronomePresetsProvider presetsProvider =
                getProgrammableMetronomePresetsProvider();

        if (presetsProvider.getNumberOfPresets() == 0) {
            Toast.makeText(getContext(), R.string.toast_no_saved_presets, Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<ProgrammableMetronomePreset> orderedPresets = presetsProvider.getOrderedPresets();
        fillSpinnerWithPresets(presetSpinner, orderedPresets);

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                presetToLoad = orderedPresets.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // The default selected item(for the popup window presets spinner) will be the first preset
        // (in alphabetic order by name of preset)
        presetSpinner.setSelection(0);
        presetToLoad = orderedPresets.get(0);

        dialogBuilder.setView(LOAD_PRESET_VIEW);
        dialog = dialogBuilder.create();
        dialog.show();

        loadPresetConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadPreset(presetToLoad);

                Toast.makeText(getContext(),R.string.toast_loaded_preset,Toast.LENGTH_SHORT).show();
                closePresetManagementPopupWindow(dialog, view);
            }
        });
        loadPresetCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePresetManagementPopupWindow(dialog, view);
            }
        });
    }

    public void createNewSavePresetDialog(){
        dialogBuilder = new AlertDialog.Builder(getContext());

        final View SAVE_PRESET_VIEW = getLayoutInflater().inflate(R.layout.save_preset_popup, null);
        savePresetNameEditText = SAVE_PRESET_VIEW.findViewById(R.id.save_preset_editText);
        if (!getCurrentPresetName().isEmpty()) {
            savePresetNameEditText.setText(getCurrentPresetName());
        }

        savePresetConfirm = SAVE_PRESET_VIEW.findViewById(R.id.load_preset_button);
        savePresetCancel = SAVE_PRESET_VIEW.findViewById(R.id.cancel_button);

        dialogBuilder.setView(SAVE_PRESET_VIEW);
        dialog = dialogBuilder.create();
        dialog.show();

        savePresetConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String presetName = savePresetNameEditText.getText().toString();
                if (presetName.isEmpty()) {
                    Toast.makeText(getContext(), R.string.toast_empty_preset_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                ProgrammableMetronomePreset newPreset =
                        new ProgrammableMetronomePreset(presetName,
                                                        fromBpmManager.getNumber(),
                                                        toBpmManager.getNumber(),
                                                        secondsManager.getNumber(),
                                                        programmableIncrementBpm.getCurrentMode());

                ProgrammableMetronomePresetsProvider presetsProvider = getProgrammableMetronomePresetsProvider();

                ContentValues newPresetValues = new ContentValues();
                newPresetValues.put(ProgrammableMetronomePresetsProvider.PRESET_NAME,
                        newPreset.getPresetName());
                newPresetValues.put(ProgrammableMetronomePresetsProvider.FROM_BPM,
                        newPreset.getFromBpm());
                newPresetValues.put(ProgrammableMetronomePresetsProvider.TO_BPM,
                        newPreset.getToBpm());
                newPresetValues.put(ProgrammableMetronomePresetsProvider.SECONDS,
                        newPreset.getSeconds());
                newPresetValues.put(ProgrammableMetronomePresetsProvider.MODE,
                        newPreset.getMode().ordinal());

                InsertOrUpdateReturn ret;
                ret = presetsProvider.insertOrUpdate(ProgrammableMetronomePresetsProvider.CONTENT_URI,
                                                     newPresetValues);

                if (ret == InsertOrUpdateReturn.Inserted) {
                    updateCurrentPreset(newPreset.getPresetName());

                    Toast.makeText(getContext(), R.string.toast_new_preset_saved, Toast.LENGTH_SHORT).show();

                } else if (ret == InsertOrUpdateReturn.Updated) {
                    updateCurrentPreset(newPreset.getPresetName());

                    Toast.makeText(getContext(), R.string.toast_update_preset, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.toast_insert_preset_error, Toast.LENGTH_SHORT).show();
                }

                closePresetManagementPopupWindow(dialog, view);
            }
        });
        savePresetCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePresetManagementPopupWindow(dialog, view);
            }
        });
    }
    // -- End of Presets Management Methods --

    public void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            fromBpmManager.syncValueWithEdittextValue();
            toBpmManager.syncValueWithEdittextValue();
            secondsManager.syncValueWithEdittextValue();
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
        switch (position) {
            case 0:  // Loop
                programmableIncrementBpm.setCurrentMode(ProgrammableIncrementBpm.ModeName.Loop);
                break;
            case 1:  // Stop/Reset
                programmableIncrementBpm.setCurrentMode(ProgrammableIncrementBpm.ModeName.StopReset);
                break;
            case 2:  // Stay
                programmableIncrementBpm.setCurrentMode(ProgrammableIncrementBpm.ModeName.Stay);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    private void goButtonClick(){
        enableStopResetAndPauseResume();
        pauseResumeButton.setText(R.string.pauseResumeButtonPause);

        ProgrammableIncrementBpm.ModeName previousMode = initialMode;
        if (programmableIncrementBpm != null) { // if previous instance of programmableIncrement
            previousMode = programmableIncrementBpm.getCurrentMode();
            programmableIncrementBpm.stopAndReset();
        }
        programmableIncrementBpm = new ProgrammableIncrementBpm(soundPlayerMetronome,
                                                                fromBpmManager.getNumber(),
                                                                toBpmManager.getNumber(),
                                                                secondsManager.getNumber(),
                                                                previousMode,
                                                 this);
        programmableIncrementBpm.start();

        // Get valid from and to bpms
        fromBpmManager.setNumber(programmableIncrementBpm.getFromBpm(), true);
        toBpmManager.setNumber(programmableIncrementBpm.getToBpm(), true);

        // Save config
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.programmable_metronome_from_bpm_key), getFromBpmManager().getNumber());
        editor.putInt(getString(R.string.programmable_metronome_to_bpm_key), getToBpmManager().getNumber());
        editor.putInt(getString(R.string.programmable_metronome_seconds_key), getSecondsManager().getNumber());
        editor.putLong(getString(R.string.programmable_metronome_mode_key), getModesSpinner().getSelectedItemId());
        if (!getCurrentPresetName().isEmpty()){
            editor.putString(getString(R.string.programmable_metronome_currentPreset_key), getCurrentPresetName());
        }
        editor.apply();
    }

    private void stopButtonClick(){

        // Delegate updating of actual bpm label
        programmableIncrementBpm.setFromBpm(fromBpmManager.getNumber());

        programmableIncrementBpm.stopAndReset();

        pauseResumeButton.setText(R.string.programmable_metronome_pause_resume_button_label);
        disableStopResetAndPauseResume();
    }

    private void pauseResumeButtonClick(){

        if(programmableIncrementBpm.isInPause()){
            pauseResumeButton.setText(R.string.pauseResumeButtonPause);
        }
        else{
            pauseResumeButton.setText(R.string.pauseResumeButtonResume);
        }

        programmableIncrementBpm.setInPause(!programmableIncrementBpm.isInPause());
    }

    private void setListeners() {
        goButton.setOnClickListener((View v) -> goButtonClick());
        stopButton.setOnClickListener((View v) -> stopButtonClick());
        pauseResumeButton.setOnClickListener((View v) -> pauseResumeButtonClick());

        fromBpmManager.getEditNumber().setOnFocusChangeListener(focusListener);
        toBpmManager.getEditNumber().setOnFocusChangeListener(focusListener);
        secondsManager.getEditNumber().setOnFocusChangeListener(focusListener);
    }

    @Override
    public void update(Object o) {
        ObserverData obsData = (ObserverData) o;
        if (obsData.updatingClassInstance instanceof ProgrammableIncrementBpm) {
            actualBpmTextView.setText(Integer.toString(obsData.value));
        } else if (obsData.updatingClassInstance instanceof EditNumberManager) {
            if (programmableIncrementBpm != null && !programmableIncrementBpm.isPlaying()) {
                actualBpmTextView.setText(Integer.toString(obsData.value));
            }
        }

    }

    public EditNumberManager getFromBpmManager() {
        return fromBpmManager;
    }

    public EditNumberManager getToBpmManager() {
        return toBpmManager;
    }

    public EditNumberManager getSecondsManager() {
        return secondsManager;
    }

    public Spinner getModesSpinner() {
        return modesSpinner;
    }

    public String getCurrentPresetName() {
        return currentPresetName;
    }
}