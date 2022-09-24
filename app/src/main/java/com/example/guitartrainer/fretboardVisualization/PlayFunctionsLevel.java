package com.example.guitartrainer.fretboardVisualization;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.guitartrainer.ProviderReturn.InsertOrUpdateReturn;

import com.example.guitartrainer.ProviderReturn;
import com.example.guitartrainer.R;
import com.example.guitartrainer.fretboardVisualization.PlayFunctionsMainPage;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;
import com.example.guitartrainer.metronome.GeneralUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayFunctionsLevel {

    public View.OnClickListener getSettingsClickListener() {
        return settingsClickListener;
    }

    private View.OnClickListener settingsClickListener;

    public enum LevelType {
        Default,
        Custom
    }
    private static final int NOTE_NAME_INDEX = 1;
    private static final int SCALE_MODE_INDEX = 0;
    private static final int FUNCTIONS_LIST_START_INDEX = 2;

    private static final int CARD_ADDER_BUTTON_TOP_MARGIN = 32;
    private static final int CARD_ADDER_BUTTON_BOTTOM_MARGIN = 16;
    private static final int CARD_TOP_MARGIN = 32;
    private static final int CARD_RADIUS = 15;
    public static final int CARD_COSTRAINTS_MARGIN = 60;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_WIDTH = 100;
    private static final String DEFAULT_CARD_BACKGROUND_COLOR = "#DAA300";
    private static final int TEXT_MARGIN = 8;
    private static final int DEFAULT_MAIN_TITLE_TEXT_SIZE = 20;
    private LevelType levelType;

    private CardView cardview;

    public void setSuccessSeconds(int successSeconds, boolean updateProvider) {
        this.successSeconds = successSeconds;
        if (updateProvider) {
            PlayFunctionsCardStatsProvider provider = getPlayFunctionsCardStatsProvider();
            ContentValues values = new ContentValues();
            values.put(PlayFunctionsCardStatsProvider.SUCCESS_SECONDS_NAME, successSeconds);
            values.put(PlayFunctionsCardStatsProvider.CARD_ID_NAME, getUniqueCardId());
            provider.insertOrUpdate(PlayFunctionsCardStatsProvider.CONTENT_URI, values);
        }
    }

    private int successSeconds;

    private ArrayList<Integer> splittedCardId = null;

    private boolean updated;

    private Activity activity;
    private MusicalScale.ScaleMode musicalScale;
    private MusicalNote.MusicalNoteName musicalNote;

    private ArrayList<Integer> functionsToPlay;

    public PlayFunctionsLevel(Activity activity,
                              MusicalScale.ScaleMode musicalScale,
                              MusicalNote.MusicalNoteName musicalNote,
                              ArrayList<Integer> functionsToPlay,
                              LevelType levelType,
                              int successSeconds
    ) {
        constructorCore(activity, musicalScale, musicalNote, functionsToPlay, levelType,
                successSeconds);
    }

    public PlayFunctionsLevel(Activity activity, CardStats cardStats) {
        String cardId = cardStats.getCardUniqueId();
        constructorCore(
                activity,
                getMusicalScaleModeFromCardId(cardId),
                getMusicalNoteNameFromCardId(cardId),
                getFunctionsToPlayFromCardId(cardId),
                cardStats.getLevelType(),
                cardStats.getSuccessSeconds()
        );
    }

    public String getUniqueCardId() {
        StringBuilder id = new StringBuilder();
        id.append(getMusicalScale().ordinal());
        id.append("_");
        id.append(getMusicalNote().ordinal());
        id.append("_");
        for(Integer i : getFunctionsToPlay()){
            id.append(i).append("_");
        }
        return id.toString();
    }

    private ArrayList<Integer> getFunctionsToPlayFromCardId(String cardId) {
        calculateSplittedCardIdIfNecessary(cardId);

        ArrayList<Integer> functions = new ArrayList<>();

        for(int i = FUNCTIONS_LIST_START_INDEX; i < splittedCardId.size(); i++){
            functions.add(splittedCardId.get(i));
        }
        return functions;
    }

    private MusicalNote.MusicalNoteName getMusicalNoteNameFromCardId(String cardId) {
        calculateSplittedCardIdIfNecessary(cardId);
        return MusicalNote.MusicalNoteName.values()[splittedCardId.get(NOTE_NAME_INDEX)];
    }

    public MusicalScale.ScaleMode getMusicalScaleModeFromCardId(String cardId){
        calculateSplittedCardIdIfNecessary(cardId);

        return MusicalScale.ScaleMode.values()[splittedCardId.get(SCALE_MODE_INDEX)];
    }

    private void calculateSplittedCardIdIfNecessary(String cardId) {
        if (splittedCardId == null) {
            splittedCardId = new ArrayList<>();
            String[] splitted = cardId.split("_");
            for (int i=0; i<splitted.length; i++) {
                splittedCardId.add(Integer.parseInt(splitted[i]));
            }
        }
    }

    private void constructorCore(Activity activity,
                                 MusicalScale.ScaleMode musicalScale,
                                 MusicalNote.MusicalNoteName musicalNote,
                                 ArrayList<Integer> functionsToPlay,
                                 LevelType levelType,
                                 int successSeconds) {
        this.musicalScale = musicalScale;
        this.musicalNote = musicalNote;
        this.functionsToPlay = functionsToPlay;
        this.levelType = levelType;
        this.successSeconds = successSeconds;
        this.activity = activity;

        saveToProvider();
    }

    public void saveToProvider(){
        PlayFunctionsCardStatsProvider provider = getPlayFunctionsCardStatsProvider();

        ProviderReturn.InsertOrUpdateReturn returnValue;

        returnValue = provider.insertOrUpdateCard(new CardStats(
                this.getUniqueCardId(),
                successSeconds,
                this.levelType
        ));

        this.updated = returnValue == InsertOrUpdateReturn.Updated;
    }

    public void createAndAddPlayFunctionsLevelCard(Context context,
                                                   ConstraintLayout parentLayout,
                                                   CardView cardAbove
    ) {

        cardview = new CardView(context);
        ConstraintSet set = new ConstraintSet();
        cardview.setId(View.generateViewId());

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        CardView.LayoutParams layoutparams = new CardView.LayoutParams(
                GeneralUtils.convertDpToPx(DEFAULT_HEIGHT, dm),
                GeneralUtils.convertDpToPx(DEFAULT_WIDTH, dm)
        );

        layoutparams.setMargins(0, GeneralUtils.convertDpToPx(CARD_TOP_MARGIN, dm), 0, 0);

        cardview.setLayoutParams(layoutparams);
        cardview.setRadius(CARD_RADIUS);
        cardview.setCardBackgroundColor(Color.parseColor(DEFAULT_CARD_BACKGROUND_COLOR));

        PlayFunctionLevelsContent levelCardContent =
                PlayFunctionLevelsContent.newInstance(this);

        if(levelType == LevelType.Custom){
            levelCardContent.setSettingsOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSettingsPopup(view);
                }
            });
        }
        parentLayout.addView(cardview);

        set.clone(parentLayout);

        set.connect(cardview.getId(), ConstraintSet.END, parentLayout.getId(), ConstraintSet.END,
                CARD_COSTRAINTS_MARGIN);
        set.connect(cardview.getId(), ConstraintSet.START, parentLayout.getId(), ConstraintSet.START,
                CARD_COSTRAINTS_MARGIN);
        if (cardAbove==null) {
            set.connect(cardview.getId(), ConstraintSet.TOP, parentLayout.getId(), ConstraintSet.TOP,
                    CARD_COSTRAINTS_MARGIN);
        } else {
            set.connect(cardview.getId(), ConstraintSet.TOP, cardAbove.getId(), ConstraintSet.BOTTOM,
                    CARD_COSTRAINTS_MARGIN);
        }
        set.applyTo(parentLayout);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setId(View.generateViewId());
        activity.getFragmentManager().beginTransaction().add(frameLayout.getId(), levelCardContent, "someTag1").commit();

        cardview.addView(frameLayout);
    }

    private void showSettingsPopup(View v) {
        PopupMenu popup = new PopupMenu(activity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.ear_training_guess_function_card_menu, popup.getMenu());

        PlayFunctionsLevel thisLevel = this;
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.delete_guess_function_level) {
                    PlayFunctionsCardStatsProvider provider = thisLevel.getPlayFunctionsCardStatsProvider();
                    provider.deleteCardStatsByName(PlayFunctionsCardStatsProvider.CONTENT_URI,
                            thisLevel.getUniqueCardId());
                    PlayFunctionsMainPage.getInstance().deleteCard(thisLevel);

                    Toast.makeText(activity, "Level Deleted! ",
                            Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popup.show();
    }

    public static CardView addCustomCardAdder(Context context, CardView cardAbove,
                                              ConstraintLayout parentLayout, View view) {

        CardView cardview = new CardView(context);
        ConstraintSet set = new ConstraintSet();
        cardview.setId(View.generateViewId());

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        CardView.LayoutParams layoutparams = new CardView.LayoutParams(
                GeneralUtils.convertDpToPx(DEFAULT_HEIGHT, dm),
                GeneralUtils.convertDpToPx(DEFAULT_WIDTH, dm)
        );

        layoutparams.setMargins(
                0,
                GeneralUtils.convertDpToPx(CARD_ADDER_BUTTON_TOP_MARGIN,dm),
                0,
                GeneralUtils.convertDpToPx(CARD_ADDER_BUTTON_BOTTOM_MARGIN,dm)
        );

        cardview.setLayoutParams(layoutparams);
        cardview.setRadius(CARD_RADIUS);
        cardview.setCardBackgroundColor(Color.parseColor("#CCFF90"));

        ConstraintLayout internalConstraintLayout = new ConstraintLayout(context);
        internalConstraintLayout.setId(View.generateViewId());
        ConstraintLayout.LayoutParams internalConstraintLayoutParams =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                );
        internalConstraintLayout.setLayoutParams(internalConstraintLayoutParams);

        TextView mainTitleText = new TextView(context);
        mainTitleText.setId(View.generateViewId());
        ConstraintLayout.LayoutParams mainTitleTextLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        mainTitleTextLayoutParams.setMargins(GeneralUtils.convertDpToPx(TEXT_MARGIN,dm),0,0,0);
        mainTitleText.setLayoutParams(mainTitleTextLayoutParams);

        mainTitleText.setText("Add Custom Level");
        //mainTitleText.setTextColor(Color.parseColor(mainTitleTextColor));

        mainTitleText.setTextSize(DEFAULT_MAIN_TITLE_TEXT_SIZE);

        internalConstraintLayout.addView(mainTitleText);
        ConstraintSet mainTitleTextSet = new ConstraintSet();
        mainTitleTextSet.clone(internalConstraintLayout);
        mainTitleTextSet.connect(mainTitleText.getId(), ConstraintSet.START,
                internalConstraintLayout.getId(), ConstraintSet.START);
        mainTitleTextSet.connect(mainTitleText.getId(), ConstraintSet.TOP,
                internalConstraintLayout.getId(), ConstraintSet.TOP);
        mainTitleTextSet.connect(mainTitleText.getId(), ConstraintSet.BOTTOM,
                internalConstraintLayout.getId(), ConstraintSet.BOTTOM);
        mainTitleTextSet.connect(mainTitleText.getId(), ConstraintSet.END,
                internalConstraintLayout.getId(), ConstraintSet.END);
        mainTitleTextSet.applyTo(internalConstraintLayout);

        parentLayout.addView(cardview);

        set.clone(parentLayout);

        set.connect(cardview.getId(), ConstraintSet.END, parentLayout.getId(), ConstraintSet.END,
                CARD_COSTRAINTS_MARGIN);
        set.connect(cardview.getId(), ConstraintSet.START, parentLayout.getId(), ConstraintSet.START,
                CARD_COSTRAINTS_MARGIN);
        set.connect(cardview.getId(), ConstraintSet.TOP, cardAbove.getId(), ConstraintSet.BOTTOM,
                CARD_COSTRAINTS_MARGIN);
        set.applyTo(parentLayout);
        cardview.addView(internalConstraintLayout);
        cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Open pop up for inserting custom level info
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

                    final View NEW_CUSTOM_CARD_VIEW =
                            ((LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).
                                    inflate(R.layout.new_play_function_custom_level, null);

                    Spinner rootNameSpinner = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunctionRootNoteSpinner);
                    Spinner scaleModeSpinner = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunctionScaleModeSpinner);

                    ArrayList<CheckBox> functions = new ArrayList<>();
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction1));
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction2));
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction3));
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction4));
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction5));
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction6));
                    functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.playFunction7));

                    Button newCustomLevelButton = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.new_play_function_custom_level_button);
                    Button cancel = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.new_play_function_custom_level_cancel_button);

                    final MusicalNote.MusicalNoteName[] ROOT_NAME_SELECTED =
                            new MusicalNote.MusicalNoteName[1];
                    final MusicalScale.ScaleMode[] SCALE_MODE_SELECTED =
                            new MusicalScale.ScaleMode[1];

                    ArrayAdapter<MusicalNote.MusicalNoteName> rootNamesAdapter;
                    rootNamesAdapter = new ArrayAdapter<>((Activity) view.getContext(),
                            android.R.layout.simple_spinner_item,
                            (List<MusicalNote.MusicalNoteName>) Arrays.asList(MusicalNote.MusicalNoteName.values()));

                    rootNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    rootNameSpinner.setAdapter(rootNamesAdapter);

                    ArrayAdapter<MusicalScale.ScaleMode> scaleModesAdapter;
                    scaleModesAdapter = new ArrayAdapter<>(view.getContext(),
                            android.R.layout.simple_spinner_item,
                            MusicalScale.ScaleMode.values());

                    scaleModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    scaleModeSpinner.setAdapter(scaleModesAdapter);

                    rootNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                                   long l) {
                            ROOT_NAME_SELECTED[0] = MusicalNote.MusicalNoteName.values()[position];

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    scaleModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                                   long l) {
                            SCALE_MODE_SELECTED[0] = MusicalScale.ScaleMode.values()[position];

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    // The default selected item(for the popup window presets spinner) will be the first preset
                    rootNameSpinner.setSelection(0);
                    scaleModeSpinner.setSelection(0);

                    ROOT_NAME_SELECTED[0] = MusicalNote.MusicalNoteName.values()[0];
                    SCALE_MODE_SELECTED[0] = MusicalScale.ScaleMode.values()[0];


                    dialogBuilder.setView(NEW_CUSTOM_CARD_VIEW);
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();

                    newCustomLevelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // Create array of functions (integers) from checkboxes
                            ArrayList<Integer> functionsToPlay = new ArrayList<>();
                            int i = 1;
                            for(CheckBox c : functions){
                                if(c.isChecked()){
                                    functionsToPlay.add(i);
                                }
                                i+=1;
                            }
                            if(functionsToPlay.size() == 0){
                                for(int function = 1; function <= 7; function++ ){
                                    functionsToPlay.add(function);
                                }
                            }
                            PlayFunctionsLevel newCustomManager = new PlayFunctionsLevel(
                                    (Activity) context,
                                    SCALE_MODE_SELECTED[0],
                                    ROOT_NAME_SELECTED[0],
                                    functionsToPlay,
                                    PlayFunctionsLevel.LevelType.Custom,
                                    -1
                            );

                            ContentResolver resolver = ((Activity)view.getContext()).getContentResolver();

                            ContentProviderClient client = resolver.acquireContentProviderClient(
                                    PlayFunctionsCardStatsProvider.CONTENT_URI);

                            PlayFunctionsCardStatsProvider provider =
                                    (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();

                            provider.insertOrUpdateCard(newCustomManager.getCardStats());

                            if (!newCustomManager.isUpdated()) {
                                PlayFunctionsMainPage.getInstance().addCardToBottom(newCustomManager);
                                Toast.makeText(context, R.string.custom_level_added,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, R.string.custom_level_already_present,
                                        Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();

                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();

                        }
                    });
                }
            }
        );
        return cardview;
    }

    public CardStats getCardStats() {
        return new CardStats(getUniqueCardId(), getSuccessSeconds(), getLevelType());
    }

    public PlayFunctionsCardStatsProvider getPlayFunctionsCardStatsProvider() {
        ContentResolver resolver = activity.getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                PlayFunctionsCardStatsProvider.CONTENT_URI);

        return (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();
    }

    public MusicalScale.ScaleMode getMusicalScale() {
        return musicalScale;
    }

    public MusicalNote.MusicalNoteName getMusicalNote() {
        return musicalNote;
    }

    public ArrayList<Integer> getFunctionsToPlay() {
        return functionsToPlay;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public CardView getCardView() {
        return cardview;
    }

    public int getSuccessSeconds(){
        return successSeconds;
    }

    public boolean isUpdated() {
        return updated;
    }
}
