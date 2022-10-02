package com.example.guitartrainer.earTraining;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.guitartrainer.ProviderReturn;
import com.example.guitartrainer.fretboardVisualization.PlayFunctionsLevel;
import com.example.guitartrainer.metronome.GeneralUtils;
import com.example.guitartrainer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuessFunctionLevel {

    public enum LevelType {
        Default,
        Custom
    }

    private static final int NOTE_NAME_INDEX = 0;
    private static final int SCALE_MODE_INDEX = 1;
    private static final int PROGRESSION_INDEX = 2;
    private static final int OCTAVE_OPTION_INDEX = 3;
    private static final int FUNCTIONS_LIST_START_INDEX = 4;

    private static final int TEXT_MARGIN = 8;
    private static final float OCTAVE_TEXT_V_BIAS = 0.88f;
    private static final float OCTAVE_TEXT_H_BIAS = 0.80f;
    private static final float BEST_SCORE_V_BIAS = 1.0f;
    private static final int CARD_ADDER_BUTTON_TOP_MARGIN = 32;
    private static final int CARD_ADDER_BUTTON_BOTTOM_MARGIN = 16;

    private static final int CARD_TOP_MARGIN = 32;
    private static final int CARD_RADIUS = 15;
    public static final int CARD_COSTRAINTS_MARGIN = 60;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_WIDTH = 100;
    private static final String DEFAULT_MAIN_TITLE_TEXT_COLOR = "#CCFF90";
    private static final String DEFAULT_NUMBER_OCTAVES_TEXT_COLOR = "#CCFF90";
    private static final String DEFAULT_PROGRESSION_TEXT_COLOR = "#6200EA";
    private static final String DEFAULT_BEST_SCORE_TEXT_COLOR = "#301D1D";
    private static final String DEFAULT_FUNCTIONS_TEXT_COLOR = "#301D1D";
    private static final String DEFAULT_CARD_BACKGROUND_COLOR = "#DAA300";
    private static final int DEFAULT_MAIN_TITLE_TEXT_SIZE = 20;
    private static final int DEFAULT_NUMBER_OCTAVES_TEXT_SIZE = 20;
    private static final int DEFAULT_PROGRESSION_TEXT_SIZE = 20;
    private static final int DEFAULT_BEST_SCORE_TEXT_SIZE = 20;

    private TextView bestScoreTextView;
    private TextView functionsTextView;
    private TextView mainTitleText;
    private TextView progressionText;
    private TextView numberOctavesText;

    private MusicalScale cardMusicalScale;
    private MusicalProgression cardMusicalProgression;
    private OctaveOption octaveOption;
    private ArrayList<Integer> functionsToPlay;

    private LevelType levelType;

    private CardView cardview;

    private int successPerc;

    private ArrayList<Integer> splittedCardId = null;

    private boolean updated;

    private Activity activity;

    public GuessFunctionLevel() {

    }

    private void constructorCore(Activity activity,
                                 MusicalScale cardMusicalScale,
                                 OctaveOption octaveOption,
                                 MusicalProgression cardMusicalProgression,
                                 ArrayList<Integer> functionsToPlay,
                                 LevelType levelType,
                                 int successPerc) {
        this.cardMusicalScale = cardMusicalScale;
        this.cardMusicalProgression = cardMusicalProgression;
        this.octaveOption = octaveOption;
        this.levelType = levelType;
        this.functionsToPlay = functionsToPlay;
        this.successPerc = successPerc;
        this.activity = activity;

        CardStatsProvider provider = getEarTrainingCardStatsProvider();

        ProviderReturn.InsertOrUpdateReturn returnValue;

        returnValue = provider.insertOrUpdateCard(new CardStats(
                this.getUniqueCardId(),
                successPerc,
                this.levelType
        ));

        this.updated = returnValue == ProviderReturn.InsertOrUpdateReturn.Updated;
    }

    public GuessFunctionLevel(Activity activity, MusicalScale cardMusicalScale,
                              OctaveOption octaveOption,
                              MusicalProgression cardMusicalProgression,
                              ArrayList<Integer> functionsToPlay,
                              LevelType levelType,
                              int successPerc
    ) {
        constructorCore(activity, cardMusicalScale, octaveOption, cardMusicalProgression, functionsToPlay, levelType,
                successPerc);
    }

    public GuessFunctionLevel(Activity activity, CardStats cardStats) {
        String cardId = cardStats.getCardUniqueId();
        MusicalScale musicalScale = getMusicalScaleFromCardId(cardId);
        this.cardMusicalScale = musicalScale;
        constructorCore(
                activity,
                musicalScale,
                getEarTrainingOctaveOptionFromCardId(cardId),
                getMusicalProgressionFromCardId(cardId),
                getFunctionsToPlayFromCardId(cardId),
                cardStats.getLevelType(),
                cardStats.getSuccessPerc()
        );
    }

    private String getReadableFunctionsToPlay(){
        StringBuilder readable = new StringBuilder();

        for(Integer i : functionsToPlay){
            readable.append(i).append("  ");
        }
        readable.deleteCharAt(readable.length()-1);

        return readable.toString();
    }

    private ArrayList<Integer> getFunctionsToPlayFromCardId(String cardId) {
        calculateSplittedCardIdIfNecessary(cardId);

        ArrayList<Integer> functions = new ArrayList<>();

        for(int i = FUNCTIONS_LIST_START_INDEX; i < splittedCardId.size(); i++){
            functions.add(splittedCardId.get(i));
        }
        return functions;
    }

    public CardStats getCardStats() {
        return new CardStats(getUniqueCardId(), getSuccessPerc(), getLevelType());
    }

    public void calculateSplittedCardIdIfNecessary(String cardId) {
        if (splittedCardId == null) {
            splittedCardId = new ArrayList<>();
            String[] splitted = cardId.split("_");
            for (int i=0; i<splitted.length; i++) {
                splittedCardId.add(Integer.parseInt(splitted[i]));
            }
        }
    }


    public MusicalNote getMusicalNoteFromCardId(String cardId) {

        calculateSplittedCardIdIfNecessary(cardId);
        MusicalNote.MusicalNoteName noteName = MusicalNote.MusicalNoteName.values()[
                splittedCardId.get(NOTE_NAME_INDEX)];

        return new MusicalNote(noteName, 0);
    }

    public MusicalScale getMusicalScaleFromCardId(String cardId) {

        calculateSplittedCardIdIfNecessary(cardId);

        MusicalScale.ScaleMode scaleMode = MusicalScale.ScaleMode.values()[
                splittedCardId.get(SCALE_MODE_INDEX)];

       return new MusicalScale(getMusicalNoteFromCardId(cardId), scaleMode);
    }

    public OctaveOption getEarTrainingOctaveOptionFromCardId(String cardId) {
        calculateSplittedCardIdIfNecessary(cardId);

        return new OctaveOption(
                OctaveOption.EarTrainingOctaveOptionEnum.values()[
                        splittedCardId.get(OCTAVE_OPTION_INDEX)]);
    }

    public MusicalProgression getMusicalProgressionFromCardId(String cardId) {
        calculateSplittedCardIdIfNecessary(cardId);
        return new MusicalProgression(
                getMusicalNoteFromCardId(cardId).getNoteName(),
                MusicalProgression.MusicalProgressionId.values()[
                        splittedCardId.get(PROGRESSION_INDEX)],
                true, cardMusicalScale.getScaleMode());
    }

    public String getUniqueCardId() {
       StringBuilder id = new StringBuilder();
       id.append(getCardMusicalScale().getRootNote().getNoteName().ordinal());
       id.append("_");
       id.append(getCardMusicalScale().getScaleMode().ordinal());
       id.append("_");
       id.append(getCardMusicalProgression().getProgressionEnum().ordinal());
       id.append("_");
       id.append(getOctaveOption().getOctaveOption().ordinal());
       id.append("_");
       for(Integer i : getFunctionsToPlay()){
           id.append(i).append("_");
       }
       return id.toString();
    }

    public ArrayList<Integer> getFunctionsToPlay() {
        return functionsToPlay;
    }


    public void addEarTrainingCardWithObjectsAndDefaults(Context context,
                                   ConstraintLayout parentLayout,
                                   String bestScoreTextString,
                                   CardView cardAbove
    ) {
        addEarTrainingCard(context, parentLayout, cardMusicalScale.toString(),
                octaveOption.getReadableOctaveOption(),
                cardMusicalProgression.toString(), bestScoreTextString, getReadableFunctionsToPlay(),
                DEFAULT_MAIN_TITLE_TEXT_COLOR, DEFAULT_NUMBER_OCTAVES_TEXT_COLOR,
                DEFAULT_PROGRESSION_TEXT_COLOR, DEFAULT_BEST_SCORE_TEXT_COLOR, DEFAULT_FUNCTIONS_TEXT_COLOR,
                DEFAULT_CARD_BACKGROUND_COLOR, DEFAULT_HEIGHT, DEFAULT_WIDTH, cardAbove);
    }

    public void addEarTrainingCard(Context context,
                                       ConstraintLayout parentLayout,
                                       String mainTitleTextString,
                                       String numberOctavesTextString,
                                       String progressionTextString,
                                       String bestScoreTextString,
                                       String functionsTextString,
                                       String mainTitleTextColor,
                                       String numberOctavesTextColor,
                                       String progressionTextColor,
                                       String bestScoreTextColor,
                                       String functionsTextColor,
                                       String cardBackgroundColor,
                                       int height, int width,
                                       CardView cardAbove
    ) {


        cardview = new CardView(context);
        ConstraintSet set = new ConstraintSet();
        cardview.setId(View.generateViewId());

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        CardView.LayoutParams layoutparams = new CardView.LayoutParams(
                GeneralUtils.convertDpToPx(height, dm),
                GeneralUtils.convertDpToPx(width, dm)
        );

        layoutparams.setMargins(0, GeneralUtils.convertDpToPx(CARD_TOP_MARGIN,dm),0,0);

        cardview.setLayoutParams(layoutparams);
        cardview.setRadius(CARD_RADIUS);
        cardview.setCardBackgroundColor(Color.parseColor(cardBackgroundColor));

        // Add card content
        ConstraintLayout internalConstraintLayout = new ConstraintLayout(context);
        internalConstraintLayout.setId(View.generateViewId());
        ConstraintLayout.LayoutParams internalConstraintLayoutParams =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                );
        internalConstraintLayout.setLayoutParams(internalConstraintLayoutParams);

        // "C Major Scale" text view
        addMainTitleToLayout(internalConstraintLayout, dm, context, mainTitleTextString,
                mainTitleTextColor);
        // "One Octave" text view
        addOctaveOptionToLayout(internalConstraintLayout, dm, context, numberOctavesTextString,
                numberOctavesTextColor);
        // Progression TextView
        addProgressionTextToLayout(internalConstraintLayout, dm, context, progressionTextString,
                progressionTextColor);
        //BEST score text view
        addBestScoreToLayout(internalConstraintLayout, dm, context, bestScoreTextString,
                bestScoreTextColor);

        addFunctionsToPlayToLayout(internalConstraintLayout, dm, context, functionsTextString,
                functionsTextColor);

        if(this.getLevelType() == LevelType.Custom) {
            // OPTION MENU
            addOptionMenuToLayout(internalConstraintLayout, dm, context);
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

        cardview.addView(internalConstraintLayout);
    }

    private void addOptionMenuToLayout(ConstraintLayout internalConstraintLayout, DisplayMetrics dm,
                                       Context context) {
        ImageButton optionButton = new ImageButton(context);
        optionButton.setId(View.generateViewId());
        ConstraintLayout.LayoutParams optionButtonLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        optionButton.setLayoutParams(optionButtonLayoutParams);

        optionButton.setImageResource(R.drawable.ic_baseline_menu_24);
        optionButton.setBackground(null);

        ConstraintSet optionButtonSet = new ConstraintSet();
        internalConstraintLayout.addView(optionButton);
        optionButtonSet.clone(internalConstraintLayout);
        optionButtonSet.connect(optionButton.getId(), ConstraintSet.BOTTOM,
                mainTitleText.getId(), ConstraintSet.BOTTOM);
        optionButtonSet.connect(optionButton.getId(), ConstraintSet.TOP,
                mainTitleText.getId(), ConstraintSet.TOP);
        optionButtonSet.connect(optionButton.getId(), ConstraintSet.END,
                internalConstraintLayout.getId(), ConstraintSet.END, 0);
        optionButtonSet.applyTo(internalConstraintLayout);

        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(activity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.ear_training_guess_function_card_menu, popup.getMenu());

        GuessFunctionLevel thisLevel = this;
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.delete_guess_function_level) {
                    CardStatsProvider provider = thisLevel.getEarTrainingCardStatsProvider();
                    provider.deleteCardStatsByName(CardStatsProvider.CONTENT_URI,
                            thisLevel.getUniqueCardId());
                    MainPage.getInstance().deleteCard(thisLevel);

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
                        inflate(R.layout.new_ear_training_custom_level, null);

                Spinner rootNameSpinner = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.rootNoteSpinner);
                Spinner scaleModeSpinner = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.scaleModeSpinner);
                Spinner progressionSpinner = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.progressionSpinner);
                Spinner octavesSpinner = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.octavesSpinner);


                ArrayList<CheckBox> functions = new ArrayList<>();
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction1));
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction2));
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction3));
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction4));
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction5));
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction6));
                functions.add(NEW_CUSTOM_CARD_VIEW.findViewById(R.id.earTrainingFunction7));
                Button newCustomLevelButton = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.new_custom_level_button);
                Button cancel = NEW_CUSTOM_CARD_VIEW.findViewById(R.id.new_custom_level_cancel_button);

                final MusicalNote.MusicalNoteName[] ROOT_NAME_SELECTED =
                        new MusicalNote.MusicalNoteName[1];
                final MusicalScale.ScaleMode[] SCALE_MODE_SELECTED =
                        new MusicalScale.ScaleMode[1];
                final MusicalProgression.MusicalProgressionId[] PROGRESSION_SELECTED =
                        new MusicalProgression.MusicalProgressionId[1];
                final OctaveOption.EarTrainingOctaveOptionEnum[] OCTAVE_OPTION_SELECTED =
                        new OctaveOption.EarTrainingOctaveOptionEnum[1];

                ArrayAdapter<MusicalNote.MusicalNoteName> rootNamesAdapter;
                rootNamesAdapter = new ArrayAdapter<>((Activity) view.getContext(),
                        android.R.layout.simple_spinner_item,
                        (List<MusicalNote.MusicalNoteName>)Arrays.asList(MusicalNote.MusicalNoteName.values()));

                rootNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                rootNameSpinner.setAdapter(rootNamesAdapter);

                ArrayAdapter<MusicalScale.ScaleMode> scaleModesAdapter;
                scaleModesAdapter = new ArrayAdapter<>(view.getContext(),
                        android.R.layout.simple_spinner_item,
                        MusicalScale.ScaleMode.values());

                scaleModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                scaleModeSpinner.setAdapter(scaleModesAdapter);

                ArrayAdapter<MusicalProgression.MusicalProgressionId> progressionsAdapter;
                progressionsAdapter = new ArrayAdapter<>(view.getContext(),
                        android.R.layout.simple_spinner_item,
                         MusicalProgression.MusicalProgressionId.values());

                progressionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                progressionSpinner.setAdapter( progressionsAdapter);

                ArrayAdapter<OctaveOption.EarTrainingOctaveOptionEnum> octaveOptionsAdapter;
                octaveOptionsAdapter = new ArrayAdapter<>(view.getContext(),
                        android.R.layout.simple_spinner_item,
                        OctaveOption.EarTrainingOctaveOptionEnum.values());

                octaveOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                octavesSpinner.setAdapter( octaveOptionsAdapter);

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

                progressionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                               long l) {
                        PROGRESSION_SELECTED[0] = MusicalProgression.MusicalProgressionId.values()[position];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                octavesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                               long l) {
                        OCTAVE_OPTION_SELECTED[0] =
                                OctaveOption.EarTrainingOctaveOptionEnum.values()[position];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                // The default selected item(for the popup window presets spinner) will be the first preset
                rootNameSpinner.setSelection(0);
                scaleModeSpinner.setSelection(0);
                progressionSpinner.setSelection(0);
                octavesSpinner.setSelection(0);
                ROOT_NAME_SELECTED[0] = MusicalNote.MusicalNoteName.values()[0];
                SCALE_MODE_SELECTED[0] = MusicalScale.ScaleMode.values()[0];
                PROGRESSION_SELECTED[0] = MusicalProgression.MusicalProgressionId.values()[0];
                OCTAVE_OPTION_SELECTED[0] =
                        OctaveOption.EarTrainingOctaveOptionEnum.values()[0];

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

                        GuessFunctionLevel newCustomManager = new GuessFunctionLevel(
                                (Activity) context,

                                new MusicalScale(new MusicalNote(ROOT_NAME_SELECTED[0],0),
                                       SCALE_MODE_SELECTED[0]),

                                new OctaveOption(OCTAVE_OPTION_SELECTED[0]),

                                new MusicalProgression(ROOT_NAME_SELECTED[0], PROGRESSION_SELECTED[0],
                                        true, SCALE_MODE_SELECTED[0]),
                                functionsToPlay,
                                LevelType.Custom,
                                -1
                        );

                        ContentResolver resolver = ((Activity)view.getContext()).getContentResolver();

                        ContentProviderClient client = resolver.acquireContentProviderClient(
                                CardStatsProvider.CONTENT_URI);

                        CardStatsProvider provider =
                                (CardStatsProvider) client.getLocalContentProvider();

                        provider.insertOrUpdateCard(newCustomManager.getCardStats());

                        if (!newCustomManager.isUpdated()) {
                            MainPage.getInstance().addCardToBottom(newCustomManager);
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

    public MusicalScale getCardMusicalScale() {
        return cardMusicalScale;
    }

    public MusicalProgression getCardMusicalProgression() {
        return cardMusicalProgression;
    }

    public OctaveOption getOctaveOption() {
        return octaveOption;
    }

    public CardView getCardView() {
        return cardview;
    }

    public TextView getBestScoreTextView() {
        return bestScoreTextView;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public boolean isUpdated() {
        return updated;
    }

    public int getSuccessPerc() {
        return successPerc;
    }

    public void setSuccessPerc(int successPerc, boolean updateProvider) {
        this.successPerc = successPerc;
        if (updateProvider) {
          CardStatsProvider provider = getEarTrainingCardStatsProvider();
          ContentValues values = new ContentValues();
          values.put(CardStatsProvider.SUCCESS_PERC_NAME, successPerc);
          values.put(CardStatsProvider.CARD_ID_NAME, getUniqueCardId());
          provider.insertOrUpdate(CardStatsProvider.CONTENT_URI, values);
        }
    }

    public CardStatsProvider getEarTrainingCardStatsProvider() {
        ContentResolver resolver = activity.getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                CardStatsProvider.CONTENT_URI);

       return (CardStatsProvider) client.getLocalContentProvider();
    }

    private void addMainTitleToLayout(ConstraintLayout internalConstraintLayout, DisplayMetrics dm,
                                      Context context, String mainTitleTextString,
                                      String mainTitleTextColor) {
        mainTitleText = new TextView(context);
        mainTitleText.setId(View.generateViewId());
        ConstraintLayout.LayoutParams mainTitleTextLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        mainTitleTextLayoutParams.setMargins(GeneralUtils.convertDpToPx(TEXT_MARGIN,dm),0,0,0);
        mainTitleText.setLayoutParams(mainTitleTextLayoutParams);

        mainTitleText.setText(mainTitleTextString);
        mainTitleText.setTextColor(Color.parseColor(mainTitleTextColor));

        mainTitleText.setTextSize(DEFAULT_MAIN_TITLE_TEXT_SIZE);

        mainTitleText.setTypeface(mainTitleText.getTypeface(), Typeface.BOLD);
        mainTitleText.setTypeface(mainTitleText.getTypeface(), Typeface.ITALIC);
        ConstraintSet mainTitleTextSet = new ConstraintSet();
        internalConstraintLayout.addView(mainTitleText);
        mainTitleTextSet.clone(internalConstraintLayout);
        mainTitleTextSet.connect(mainTitleText.getId(), ConstraintSet.START,
                internalConstraintLayout.getId(), ConstraintSet.START);
        mainTitleTextSet.connect(mainTitleText.getId(), ConstraintSet.TOP,
                internalConstraintLayout.getId(), ConstraintSet.TOP);
        mainTitleTextSet.applyTo(internalConstraintLayout);
    }

    private void addOctaveOptionToLayout(ConstraintLayout internalConstraintLayout, DisplayMetrics dm,
                                         Context context, String numberOctavesTextString,
                                         String numberOctavesTextColor) {
        numberOctavesText = new TextView(context);
        numberOctavesText.setId(View.generateViewId());
        numberOctavesText.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        ));
        numberOctavesText.setText(numberOctavesTextString);
        numberOctavesText.setTextColor(Color.parseColor(numberOctavesTextColor));

        numberOctavesText.setTextSize(DEFAULT_NUMBER_OCTAVES_TEXT_SIZE);

        numberOctavesText.setTypeface(numberOctavesText.getTypeface(), Typeface.BOLD);
        numberOctavesText.setTypeface(numberOctavesText.getTypeface(), Typeface.ITALIC);
        ConstraintSet numberOctavesTextSet = new ConstraintSet();
        internalConstraintLayout.addView(numberOctavesText);
        numberOctavesTextSet.clone(internalConstraintLayout);
        numberOctavesTextSet.connect(numberOctavesText.getId(), ConstraintSet.BOTTOM,
                internalConstraintLayout.getId(), ConstraintSet.BOTTOM);
        numberOctavesTextSet.connect(numberOctavesText.getId(), ConstraintSet.END,
                internalConstraintLayout.getId(), ConstraintSet.END);
        numberOctavesTextSet.connect(numberOctavesText.getId(), ConstraintSet.START,
                mainTitleText.getId(), ConstraintSet.END);
        numberOctavesTextSet.connect(numberOctavesText.getId(), ConstraintSet.TOP,
                mainTitleText.getId(), ConstraintSet.BOTTOM);


        numberOctavesTextSet.setVerticalBias(numberOctavesText.getId(), OCTAVE_TEXT_V_BIAS);
        numberOctavesTextSet.setHorizontalBias(numberOctavesText.getId(), OCTAVE_TEXT_H_BIAS);
        numberOctavesTextSet.applyTo(internalConstraintLayout);
    }

    private void addProgressionTextToLayout(ConstraintLayout internalConstraintLayout, DisplayMetrics dm,
                                            Context context, String progressionTextString,
                                            String progressionTextColor) {
        progressionText = new TextView(context);
        progressionText.setId(View.generateViewId());
        progressionText.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        ));

        progressionText.setText(progressionTextString);
        progressionText.setTextColor(Color.parseColor(progressionTextColor));

        progressionText.setTextSize(DEFAULT_PROGRESSION_TEXT_SIZE);

        ConstraintSet progressionTextSet = new ConstraintSet();
        internalConstraintLayout.addView(progressionText);
        progressionTextSet.clone(internalConstraintLayout);
        progressionTextSet.connect(progressionText.getId(), ConstraintSet.BOTTOM,
                mainTitleText.getId(), ConstraintSet.BOTTOM);
        progressionTextSet.connect(progressionText.getId(), ConstraintSet.TOP,
                mainTitleText.getId(), ConstraintSet.TOP);
        progressionTextSet.connect(progressionText.getId(), ConstraintSet.START,
                mainTitleText.getId(), ConstraintSet.END, CARD_ADDER_BUTTON_BOTTOM_MARGIN);

        progressionTextSet.applyTo(internalConstraintLayout);
    }

    private void addBestScoreToLayout(ConstraintLayout internalConstraintLayout, DisplayMetrics dm,
                                      Context context, String bestScoreTextString,
                                      String bestScoreTextColor) {
        bestScoreTextView = new TextView(context);
        bestScoreTextView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams bestScoreLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        bestScoreTextView.setLayoutParams(bestScoreLayoutParams);

        bestScoreTextView.setText(bestScoreTextString);
        bestScoreTextView.setTextColor(Color.parseColor(bestScoreTextColor));

        bestScoreTextView.setTextSize(DEFAULT_BEST_SCORE_TEXT_SIZE);

        ConstraintSet bestScoreTextSet = new ConstraintSet();
        internalConstraintLayout.addView(bestScoreTextView);
        bestScoreTextSet.clone(internalConstraintLayout);
        bestScoreTextSet.connect(bestScoreTextView.getId(), ConstraintSet.BOTTOM,
                numberOctavesText.getId(), ConstraintSet.BOTTOM);
        bestScoreTextSet.connect(bestScoreTextView.getId(), ConstraintSet.START,
                mainTitleText.getId(), ConstraintSet.START);
        bestScoreTextSet.connect(bestScoreTextView.getId(), ConstraintSet.TOP,
                mainTitleText.getId(), ConstraintSet.BOTTOM);
        bestScoreTextSet.setVerticalBias(bestScoreTextView.getId(), BEST_SCORE_V_BIAS);

        bestScoreTextSet.applyTo(internalConstraintLayout);
    }

    private void addFunctionsToPlayToLayout(ConstraintLayout internalConstraintLayout, DisplayMetrics dm,
                                            Context context, String functionsTextString,
                                            String functionsTextColor) {
        functionsTextView = new TextView(context);
        functionsTextView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams functionsLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        functionsTextView.setLayoutParams(functionsLayoutParams);

        functionsTextView.setText(functionsTextString);
        functionsTextView.setTextColor(Color.parseColor(functionsTextColor));

        functionsTextView.setTextSize(DEFAULT_BEST_SCORE_TEXT_SIZE);

        ConstraintSet functionsTextSet = new ConstraintSet();
        internalConstraintLayout.addView(functionsTextView);
        functionsTextSet.clone(internalConstraintLayout);
        functionsTextSet.connect(functionsTextView.getId(), ConstraintSet.BOTTOM,
                numberOctavesText.getId(), ConstraintSet.TOP);
        functionsTextSet.connect(functionsTextView.getId(), ConstraintSet.START,
                mainTitleText.getId(), ConstraintSet.START);
        functionsTextSet.connect(functionsTextView.getId(), ConstraintSet.TOP,
                mainTitleText.getId(), ConstraintSet.BOTTOM);
        functionsTextSet.connect(functionsTextView.getId(), ConstraintSet.END,
                numberOctavesText.getId(), ConstraintSet.END);
        functionsTextSet.applyTo(internalConstraintLayout);
    }

}
