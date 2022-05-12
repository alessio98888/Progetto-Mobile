/*
 * @(#) Home.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.ArrayList;


/**
 *
 Fragment that contains all the buttons that permit to access the app main features.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class EarTrainingMainPage extends Fragment {

    private ArrayList<EarTrainingGuessFunctionLevel> cardManagers;

    private ConstraintLayout parentLayout;

    private boolean addCustomCardAlreadyAdded = false;

    private static EarTrainingMainPage instance = null;

    private CardView levelAdderCard = null;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    // Option defaults
    private final boolean DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE = false;


    // Actual option values
    private boolean actualAutomaticAnswersWithVoiceValue = DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE;
    ActivityResultLauncher<Intent> optionsActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        boolean automaticWithVoice = data.getBooleanExtra(
                                "automaticAnswersWithVoice",
                                DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE
                        );
                        setActualAutomaticAnswersWithVoiceValue(automaticWithVoice);

                        editor.putBoolean("automaticAnswersWithVoice",
                                automaticWithVoice);
                        editor.apply();
                    }
                }
            });
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_ear_training, container, false);
    }

    public static EarTrainingMainPage getInstance() {
        return instance;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cardManagers = new ArrayList<>();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        setActualAutomaticAnswersWithVoiceValue(sharedPref.getBoolean("automaticAnswersWithVoice",
                DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE));



        parentLayout = (getView().findViewById(R.id.earTrainingContentConstraintLayout));
        initializeDefaultCardManagers();


        getCustomLevelsFromProvider();
        addCardCustomLevelAdder();

        // Just destroy(finish) activity when back pressed
        overrideBackButtonBehaviour(); // Preventing bug: back pressed after level completion

    }

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
        Intent intent = new Intent(getContext(), EarTrainingGuessFunctionOptionsActivity.class);
        intent.putExtra("automaticAnswersWithVoice", actualAutomaticAnswersWithVoiceValue);
        optionsActivityResultLauncher.launch(intent);
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

    public void deleteCard(EarTrainingGuessFunctionLevel levelToDelete) {

        ConstraintSet set = new ConstraintSet();
        EarTrainingGuessFunctionLevel managerToDelete;
        for (int i = 0; i< cardManagers.size(); i++) {
            managerToDelete = cardManagers.get(i);
            if (managerToDelete.getUniqueCardId().equals(levelToDelete.getUniqueCardId())) {
                CardView cardToDelete = managerToDelete.getCardView();
                ((ViewManager)cardToDelete.getParent()).removeView(cardToDelete);

                set.clone(parentLayout);
                CardView previousCard = cardManagers.get(i-1).getCardView();
                CardView nextCard;
                if (i != cardManagers.size()-1) {
                    nextCard = cardManagers.get(i+1).getCardView();
                } else {
                    nextCard = levelAdderCard;
                }
                set.connect(
                        nextCard.getId(), ConstraintSet.TOP,
                        previousCard.getId(), ConstraintSet.BOTTOM,
                        EarTrainingGuessFunctionLevel.CARD_COSTRAINTS_MARGIN);
                set.applyTo(parentLayout);
                cardManagers.remove(i);
                return;
            }
        }
    }

    public void getCustomLevelsFromProvider() {
        ContentResolver resolver = getActivity().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(
                EarTrainingCardStatsProvider.CONTENT_URI);
        EarTrainingCardStatsProvider provider =
                (EarTrainingCardStatsProvider) client.getLocalContentProvider();

        ArrayList<EarTrainingCardStats> cardStats = provider.getCardStats();

        for (int i=0; i<cardStats.size(); i++) {
            if(cardStats.get(i).getLevelType() == EarTrainingGuessFunctionLevel.LevelType.Custom) {
                addCardToBottom(new EarTrainingGuessFunctionLevel(getActivity(), cardStats.get(i)));
            }
        }
    }

    public void addCardCustomLevelAdder() {
        levelAdderCard = EarTrainingGuessFunctionLevel.addCustomCardAdder(getContext(),
                getCardManagers().get(getCardManagers().size()-1).getCardView(),
                parentLayout, getView());
        addCustomCardAlreadyAdded = true;
    }

    public void addCardToBottom(EarTrainingGuessFunctionLevel newManager) {
        CardView above;
        if (cardManagers.size() == 0) {
            above = null;
        } else {
            above = cardManagers.get(
                    cardManagers.size()-1).getCardView();
        }

        ContentResolver resolver = getActivity().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(
                EarTrainingCardStatsProvider.CONTENT_URI);
        EarTrainingCardStatsProvider provider =
                (EarTrainingCardStatsProvider) client.getLocalContentProvider();

        newManager.addEarTrainingCardWithObjectsAndDefaults(getContext(), parentLayout,
                getScoreTextUsingProvider(provider, newManager.getUniqueCardId()),
                above);

        newManager.getCardView().setOnClickListener(view -> cardOnClick(view, newManager));

        cardManagers.add(newManager);

        if (addCustomCardAlreadyAdded) {

            ConstraintLayout constraintLayout = (ConstraintLayout) levelAdderCard.getParent();
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(
                    levelAdderCard.getId(),
                    ConstraintSet.TOP,
                    cardManagers.get(cardManagers.size()-1).getCardView().getId(),
                    ConstraintSet.BOTTOM);
            constraintSet.applyTo(constraintLayout);
        }
    }

    public void initializeDefaultCardManagers() {
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                EarTrainingCardStatsProvider.CONTENT_URI);

        //Maybe not necessary
        EarTrainingCardStatsProvider provider =
                (EarTrainingCardStatsProvider) client.getLocalContentProvider();

        MusicalNote rootNoteCard = new MusicalNote(MusicalNote.MusicalNoteName.c, 0);
        MusicalScale scaleCard = new MusicalScale(rootNoteCard, MusicalScale.ScaleMode.Major);
        MusicalProgression progressionCard = new MusicalProgression(rootNoteCard.getNoteName(),
                MusicalProgression.MusicalProgressionId.i_iv_i_v, true, scaleCard.getScaleMode());
        EarTrainingOctaveOption octaveOption = new EarTrainingOctaveOption(
                EarTrainingOctaveOption.EarTrainingOctaveOptionEnum.One_Octave);

        // DEFAULT 1
        EarTrainingGuessFunctionLevel default1 = new EarTrainingGuessFunctionLevel(getActivity(),
                scaleCard, octaveOption, progressionCard,
                EarTrainingGuessFunctionLevel.LevelType.Default, -1);

        provider.insertOrUpdateCard(default1.getCardStats());
        default1.setSuccessPerc(provider.getSuccessPerc(default1.getUniqueCardId()), false);
        addCardToBottom(default1);


        octaveOption = new EarTrainingOctaveOption(
                EarTrainingOctaveOption.EarTrainingOctaveOptionEnum.Many_Octaves);

        // DEFAULT 2
        EarTrainingGuessFunctionLevel default2 = new EarTrainingGuessFunctionLevel(getActivity(),
                scaleCard, octaveOption, progressionCard,
                EarTrainingGuessFunctionLevel.LevelType.Default, -1);

        provider.insertOrUpdateCard(default2.getCardStats());
        default2.setSuccessPerc(provider.getSuccessPerc(default2.getUniqueCardId()), false);
        addCardToBottom(default2);

    }

    public String getScoreTextUsingProvider(EarTrainingCardStatsProvider provider, String cardUniqueId) {
        int succesPerc = provider.getSuccessPerc(cardUniqueId);
        String scoreText;
        if (succesPerc==-1) {
            scoreText = "";
        } else {
            scoreText = "" + succesPerc;
            scoreText += "%";
        }
        return scoreText;
    }

    public void cardOnClick(View view, EarTrainingGuessFunctionLevel cardManager) {
        ArrayList<Integer> rootNotes;
        // TO DO: ALL NOTES
        if (cardManager.getCardMusicalScale().getRootNote().getNoteName() == MusicalNote.MusicalNoteName.all) {
            rootNotes = (ArrayList<Integer>)MusicalNote.getMusicalNotesOrdinals();
        } else {
            rootNotes = new ArrayList<>();
            rootNotes.add(
                    cardManager.getCardMusicalScale().getRootNote().getNoteName().ordinal());
        }


        Bundle bundle = getEarTrainingBundle(
                rootNotes,
                cardManager.getOctaveOption().getOctaveOption(),
                cardManager.getCardMusicalProgression().getProgressionEnum(),
                cardManager.getCardMusicalScale().getScaleMode()
        );

        bundle.putString("cardUniqueId", cardManager.getUniqueCardId());
        bundle.putInt("levelType", cardManager.getLevelType().ordinal());

        Navigation.findNavController(view).navigate(
                R.id.action_earTrainingMainPage_to_earTrainingExerciseExecutionPage, bundle);
    }

    public Bundle getEarTrainingBundle(ArrayList<Integer> rootNotes,
                                       EarTrainingOctaveOption.EarTrainingOctaveOptionEnum earTrainingOption,
                                       MusicalProgression.MusicalProgressionId progressionId,
                                       MusicalScale.ScaleMode scaleMode) {
        Bundle bundle = new Bundle();
        bundle.putInt("ear_training_option_index",
                earTrainingOption.ordinal());

        int[] rootNotesArray = rootNotes.stream().mapToInt(i->i).toArray();
        bundle.putIntArray("rootNotes", rootNotesArray);
        bundle.putInt("musicalProgression", progressionId.ordinal());
        bundle.putInt("scaleMode", scaleMode.ordinal());

        bundle.putBoolean("automaticAnswersWithVoice", actualAutomaticAnswersWithVoiceValue);

        return bundle;
    }

    public ArrayList<EarTrainingGuessFunctionLevel> getCardManagers() {
        return cardManagers;
    }

    public boolean isActualAutomaticAnswersWithVoiceValue() {
        return actualAutomaticAnswersWithVoiceValue;
    }

    public void setActualAutomaticAnswersWithVoiceValue(boolean actualAutomaticAnswersWithVoiceValue) {
        this.actualAutomaticAnswersWithVoiceValue = actualAutomaticAnswersWithVoiceValue;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}