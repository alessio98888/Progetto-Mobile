/*
 * @(#) Home.java     1.0 05/01/2022
 */

package com.example.guitartrainer.earTraining;

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

import com.example.guitartrainer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 Fragment that contains all the buttons that permit to access the app main features.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class MainPage extends Fragment {

    private ArrayList<GuessFunctionLevel> cardManagers;

    private ConstraintLayout parentLayout;

    private boolean addCustomCardAlreadyAdded = false;

    private static MainPage instance = null;

    private CardView levelAdderCard = null;

    private SharedPreferences.Editor editor;

    // Option defaults
    private final boolean DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE = false;
    private final float DEFAULT_PROGRESSION_VELOCITY = 1.0f;

    // Actual option values
    private boolean actualAutomaticAnswersWithVoiceValue = DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE;
    private float actualProgressionVelocity = DEFAULT_PROGRESSION_VELOCITY;

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
                        float progressionVelocity = data.getFloatExtra(
                                Options.PROGRESSION_VELOCITY_KEY,
                                DEFAULT_PROGRESSION_VELOCITY);

                        setActualAutomaticAnswersWithVoiceValue(automaticWithVoice);
                        setActualProgressionVelocity(progressionVelocity);
                        editor.putBoolean(
                                "automaticAnswersWithVoice",
                                automaticWithVoice);
                        editor.putFloat(
                                Options.PROGRESSION_VELOCITY_KEY,
                                progressionVelocity);
                        editor.apply();
                    }
                }
            });

    public void setActualAutomaticAnswersWithVoiceValue(boolean actualAutomaticAnswersWithVoiceValue) {
        this.actualAutomaticAnswersWithVoiceValue = actualAutomaticAnswersWithVoiceValue;
    }

    public void setActualProgressionVelocity(float actualProgressionVelocity) {
        this.actualProgressionVelocity = actualProgressionVelocity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_ear_training, container, false);
    }

    public static MainPage getInstance() {
        return instance;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cardManagers = new ArrayList<>();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        setActualAutomaticAnswersWithVoiceValue(sharedPref.getBoolean("automaticAnswersWithVoice",
                DEFAULT_AUTOMATIC_ANSWERS_WITH_VOICE));
        setActualProgressionVelocity(sharedPref.getFloat(Options.PROGRESSION_VELOCITY_KEY,
                DEFAULT_PROGRESSION_VELOCITY));


        parentLayout = (getView().findViewById(R.id.earTrainingContentConstraintLayout));
        initializeAndAddDefaultCards();


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
        Intent intent = new Intent(getContext(), GuessFunctionOptionsActivity.class);
        intent.putExtra("automaticAnswersWithVoice", actualAutomaticAnswersWithVoiceValue);
        intent.putExtra(Options.PROGRESSION_VELOCITY_KEY, actualProgressionVelocity);
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

    public void deleteCard(GuessFunctionLevel levelToDelete) {

        ConstraintSet set = new ConstraintSet();
        GuessFunctionLevel managerToDelete;
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
                        GuessFunctionLevel.CARD_COSTRAINTS_MARGIN);
                set.applyTo(parentLayout);
                cardManagers.remove(i);
                return;
            }
        }
    }

    public void getCustomLevelsFromProvider() {
        ContentResolver resolver = getActivity().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(
                CardStatsProvider.CONTENT_URI);
        CardStatsProvider provider =
                (CardStatsProvider) client.getLocalContentProvider();

        ArrayList<CardStats> cardStats = provider.getCardStats();

        for (int i=0; i<cardStats.size(); i++) {
            if(cardStats.get(i).getLevelType() == GuessFunctionLevel.LevelType.Custom) {
                addCardToBottom(new GuessFunctionLevel(getActivity(), cardStats.get(i)));
            }
        }
    }

    public void addCardCustomLevelAdder() {
        levelAdderCard = GuessFunctionLevel.addCustomCardAdder(getContext(),
                getCardManagers().get(getCardManagers().size()-1).getCardView(),
                parentLayout, getView());
        addCustomCardAlreadyAdded = true;
    }

    public void addCardToBottom(GuessFunctionLevel newManager) {
        CardView above;
        if (cardManagers.size() == 0) {
            above = null;
        } else {
            above = cardManagers.get(
                    cardManagers.size()-1).getCardView();
        }

        ContentResolver resolver = getActivity().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(
                CardStatsProvider.CONTENT_URI);
        CardStatsProvider provider =
                (CardStatsProvider) client.getLocalContentProvider();

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

    public void initializeAndAddDefaultCards() {
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                CardStatsProvider.CONTENT_URI);

        //Maybe not necessary
        CardStatsProvider provider =
                (CardStatsProvider) client.getLocalContentProvider();

        MusicalNote rootNoteCard = new MusicalNote(MusicalNote.MusicalNoteName.c, 0);
        MusicalScale scaleCard = new MusicalScale(rootNoteCard, MusicalScale.ScaleMode.Major);
        MusicalProgression progressionCard = new MusicalProgression(rootNoteCard.getNoteName(),
                MusicalProgression.MusicalProgressionId._1_4_1_5, true, scaleCard.getScaleMode());
        OctaveOption octaveOption = new OctaveOption(
                OctaveOption.EarTrainingOctaveOptionEnum.One_Octave);

        // DEFAULT 1
        ArrayList<Integer> functionsToPlay1 = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7));
        GuessFunctionLevel default1 = new GuessFunctionLevel(
                getActivity(),
                scaleCard,
                octaveOption,
                progressionCard,
                functionsToPlay1,
                GuessFunctionLevel.LevelType.Default,
                -1);

        provider.insertOrUpdateCard(default1.getCardStats());
        default1.setSuccessPerc(provider.getSuccessPerc(default1.getUniqueCardId()), false);
        addCardToBottom(default1);


        octaveOption = new OctaveOption(
                OctaveOption.EarTrainingOctaveOptionEnum.Many_Octaves);

        // DEFAULT 2
        ArrayList<Integer> functionsToPlay2 = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7));
        GuessFunctionLevel default2 = new GuessFunctionLevel(
                getActivity(),
                scaleCard,
                octaveOption,
                progressionCard,
                functionsToPlay2,
                GuessFunctionLevel.LevelType.Default,
                -1);

        provider.insertOrUpdateCard(default2.getCardStats());
        default2.setSuccessPerc(provider.getSuccessPerc(default2.getUniqueCardId()), false);
        addCardToBottom(default2);

    }

    public String getScoreTextUsingProvider(CardStatsProvider provider, String cardUniqueId) {
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

    public void cardOnClick(View view, GuessFunctionLevel cardManager) {
        ArrayList<Integer> rootNotes;
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
                cardManager.getCardMusicalScale().getScaleMode(),
                cardManager.getFunctionsToPlay()
        );

        bundle.putString("cardUniqueId", cardManager.getUniqueCardId());
        bundle.putInt("levelType", cardManager.getLevelType().ordinal());

        Navigation.findNavController(view).navigate(
                R.id.action_earTrainingMainPage_to_earTrainingExerciseExecutionPage, bundle);
    }

    public Bundle getEarTrainingBundle(ArrayList<Integer> rootNotes,
                                       OctaveOption.EarTrainingOctaveOptionEnum earTrainingOption,
                                       MusicalProgression.MusicalProgressionId progressionId,
                                       MusicalScale.ScaleMode scaleMode,
                                       ArrayList<Integer> functionsToPlay) {
        Bundle bundle = new Bundle();
        bundle.putInt("ear_training_option_index",
                earTrainingOption.ordinal());

        int[] rootNotesArray = rootNotes.stream().mapToInt(i->i).toArray();
        bundle.putIntArray("rootNotes", rootNotesArray);
        bundle.putInt("musicalProgression", progressionId.ordinal());
        bundle.putInt("scaleMode", scaleMode.ordinal());

        bundle.putIntArray("functionsToPlay", convertIntegers(functionsToPlay));
        bundle.putBoolean("automaticAnswersWithVoice", actualAutomaticAnswersWithVoiceValue);
        bundle.putFloat(Options.PROGRESSION_VELOCITY_KEY, actualProgressionVelocity);

        return bundle;
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    public ArrayList<GuessFunctionLevel> getCardManagers() {
        return cardManagers;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}