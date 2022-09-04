package com.example.guitartrainer.fretboardVisualization;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.GuessFunctionLevel;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;

import java.util.ArrayList;
import java.util.Arrays;


public class PlayFunctionsMainPage extends Fragment {

    public ArrayList<PlayFunctionsLevel> getCardManagers() {
        return cardManagers;
    }

    private ArrayList<PlayFunctionsLevel> cardManagers;

    private ConstraintLayout parentLayout;

    private boolean addCustomCardAlreadyAdded = false;

    private CardView levelAdderCard = null;

    private boolean competitiveMode;
    private boolean noteNamesWithVoice;


    private static PlayFunctionsMainPage instance;
    private SharedPreferences.Editor editor;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_functions_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cardManagers = new ArrayList<>();
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        competitiveMode = requireArguments().getBoolean("playFunctionsCompetitiveMode");
        noteNamesWithVoice = requireArguments().getBoolean("noteNamesWithVoice");


        parentLayout = (requireView().findViewById(R.id.playFunctionsMainPageRootLayout));
        initializeAndAddDefaultCards();

        addCardCustomLevelAdder();

        getCustomLevelsFromProvider();


        // Just destroy(finish) activity when back pressed
        overrideBackButtonBehaviour(); // Preventing bug: back pressed after level completion
    }
    public void initializeAndAddDefaultCards() {
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                PlayFunctionsCardStatsProvider.CONTENT_URI);

        //Maybe not necessary
        PlayFunctionsCardStatsProvider provider =
                (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();


        MusicalNote.MusicalNoteName rootNoteCard = MusicalNote.MusicalNoteName.c;
        MusicalScale.ScaleMode scaleCard =  MusicalScale.ScaleMode.Major;
        ArrayList<Integer> functionsToPlay = new ArrayList<>(Arrays.asList(1,3,5));


        // DEFAULT 1
        PlayFunctionsLevel default1 = new PlayFunctionsLevel(getActivity(),
                scaleCard, rootNoteCard, functionsToPlay,
                PlayFunctionsLevel.LevelType.Default, -1);

        provider.insertOrUpdateCard(default1.getCardStats());
        default1.setSuccessSeconds(provider.getSuccessSeconds(default1.getUniqueCardId()), false);
        addCardToBottom(default1);

        MusicalNote.MusicalNoteName rootNoteCard2 = MusicalNote.MusicalNoteName.c;
        MusicalScale.ScaleMode scaleCard2 =  MusicalScale.ScaleMode.Major;
        ArrayList<Integer> functionsToPlay2 = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7));


        // DEFAULT 1
        PlayFunctionsLevel default2 = new PlayFunctionsLevel(getActivity(),
                scaleCard2, rootNoteCard2, functionsToPlay2,
                PlayFunctionsLevel.LevelType.Default, -1);

        provider.insertOrUpdateCard(default1.getCardStats());
        default1.setSuccessSeconds(provider.getSuccessSeconds(default1.getUniqueCardId()), false);
        addCardToBottom(default2);

    }

    public void getCustomLevelsFromProvider() {
        ContentResolver resolver = getActivity().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(
                PlayFunctionsCardStatsProvider.CONTENT_URI);
        PlayFunctionsCardStatsProvider provider =
                (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();

        ArrayList<CardStats> cardStats = provider.getCardStats();

        for (int i=0; i<cardStats.size(); i++) {
            if(cardStats.get(i).getLevelType() == PlayFunctionsLevel.LevelType.Custom) {
                addCardToBottom(new PlayFunctionsLevel(getActivity(), cardStats.get(i)));
            }
        }
    }
    public String getScoreTextUsingProvider(PlayFunctionsCardStatsProvider provider, String cardUniqueId) {
        int successSeconds = provider.getSuccessSeconds(cardUniqueId);
        String scoreText;
        if (successSeconds==-1) {
            scoreText = "";
        } else {
            scoreText = "" + successSeconds;
            scoreText += " s";
        }
        return scoreText;
    }
    public void addCardToBottom(PlayFunctionsLevel newManager) {
        CardView above;
        if (cardManagers.size() == 0) {
            above = null;
        } else {
            above = cardManagers.get(
                    cardManagers.size()-1).getCardView();
        }

        ContentResolver resolver = getActivity().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(
                PlayFunctionsCardStatsProvider.CONTENT_URI);
        PlayFunctionsCardStatsProvider provider =
                (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();

        newManager.setSuccessSeconds(provider.getSuccessSeconds(newManager.getUniqueCardId()),false);

        newManager.createAndAddPlayFunctionsLevelCard(getContext(), parentLayout, above);

        //newManager.getCardView().setOnClickListener(view -> cardOnClick(view, newManager));

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

    public void deleteCard(PlayFunctionsLevel levelToDelete) {

        ConstraintSet set = new ConstraintSet();
        PlayFunctionsLevel managerToDelete;
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

    public void addCardCustomLevelAdder() {
        levelAdderCard = PlayFunctionsLevel.addCustomCardAdder(getContext(),
                getCardManagers().get(getCardManagers().size()-1).getCardView(),
                parentLayout, getView());
        addCustomCardAlreadyAdded = true;
    }

    public static PlayFunctionsMainPage getInstance() {
        return instance;
    }
}