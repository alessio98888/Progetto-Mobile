package com.example.guitartrainer.fretboardVisualization;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.guitartrainer.R;
import com.example.guitartrainer.earTraining.MusicalNote;
import com.example.guitartrainer.earTraining.MusicalScale;
import com.example.guitartrainer.earTraining.MusicalScaleNote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;


public class PlayFunctionExecutionPage extends ExecutionPage {

    private PlayFunctionsLevel.LevelType levelType;
    private MusicalScale.ScaleMode scaleMode;
    private String cardUniqueId;
    private ArrayList<MusicalNote.MusicalNoteName> rootNotesNames;
    private ArrayList<Integer> functionsToPlay;


    private MusicalNote.MusicalNoteName currentRootNote;
    private TextView rootNoteText;

    private int currentFunctionToPlay;
    private TextView functionToPlayText;


    private TextView scaleModeText;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_function_execution_page, container, false);
    }

    @Override
    public void initViews(){
        super.initViews();

        currentRoundText = requireView().findViewById(R.id.fretboardPlayFunctionCurrentRoundText);

        rootNoteText = requireView().findViewById(R.id.playFunctionsExecutionRootNoteText);
        functionToPlayText = requireView().findViewById(R.id.playFunctionsExecutionFunctionToPlayText);
        scaleModeText = requireView().findViewById(R.id.playFunctionsExecutionScaleMode);
        scaleModeText.setText(scaleMode.toString());
        fakeGuitarFragmentContainer = requireActivity().findViewById(R.id.fretboard_visualization_fake_guitar_container_container);
    }

    @Override
    public void generateNoteToPlay(){
        Random rand = new Random();
        currentRootNote = rootNotesNames.get(rand.nextInt(rootNotesNames.size()));

        MusicalScaleNote randomNote = MusicalScale.getRandomScaleNote(currentRootNote, scaleMode, functionsToPlay);
        currentFunctionToPlay = randomNote.getMusicalFunction();
        noteToPlay = randomNote.getNoteName();
    }

    @Override
    public String getWhatToSpeak(){
        return MusicalNote.getSpeakableNoteName(currentRootNote) + currentFunctionToPlay;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateUI(){
        functionToPlayText.setText(Integer.toString(currentFunctionToPlay));
        rootNoteText.setText(currentRootNote.toString());
    }

    @Override
    public void initWithArguments(){

        scaleMode = MusicalScale.ScaleMode.values()[
                getArguments().getInt("scaleMode")];

        cardUniqueId = getArguments().getString("cardUniqueId");

        levelType = PlayFunctionsLevel.LevelType.values()[
                getArguments().getInt("levelType")];

        int[] rootNotesArray = getArguments().getIntArray("rootNotes");
        rootNotesNames = MusicalNote.toMusicalNotesNames(
                (ArrayList<Integer>) Arrays.stream(rootNotesArray).boxed().collect(Collectors.toList()));

        functionsToPlay = (ArrayList<Integer>) Arrays.stream(getArguments().getIntArray("functionsToPlay")).boxed().collect(Collectors.toList());

        voiceSynthMode = getArguments().getBoolean(
                Options.VOICE_SYNTH_MODE_KEY,
                false);

        competitiveMode = getArguments().getBoolean(Options.COMPETITIVE_MODE_KEY);

        fakeGuitarMode = getArguments().getBoolean(Options.FAKE_GUITAR_MODE_KEY);
    }

    @Override
    public void initFakeGuitar() {
        adaptLayoutToLandscape();
    }

    private void adaptLayoutToLandscape(){
        TextView rootNoteText = requireActivity().findViewById(R.id.playFunctionsExecutionRootNoteText);
        TextView scaleText = requireActivity().findViewById(R.id.playFunctionsExecutionScaleMode);
        TextView functionText = requireActivity().findViewById(R.id.playFunctionsExecutionFunctionToPlayText);


        ConstraintLayout constraintLayout = requireActivity().findViewById(R.id.playFunctionsExecutionPageRootLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.clear(rootNoteText.getId());
        constraintSet.clear(scaleText.getId());
        constraintSet.clear(functionText.getId());

        allHeightsToMatchConstraintAllWidthToWrapContent(rootNoteText, scaleText, functionText, constraintSet);

        horizontalChain(rootNoteText, scaleText, constraintSet);

        allBottomsToTopOfFakeGuitar(rootNoteText, scaleText, functionText, constraintSet);

        allTopsToParentTop(rootNoteText, scaleText, functionText, constraintSet);

        toRight(functionText, constraintSet);

        leftToRight(rootNoteText, currentRoundText, constraintSet);

        constraintSet.applyTo(constraintLayout);
    }

    private void leftToRight(TextView one, TextView other, ConstraintSet constraintSet) {
        constraintSet.connect(
                one.getId(),ConstraintSet.LEFT,
                other.getId(),ConstraintSet.RIGHT,
                0);
    }

    private void toRight(TextView view, ConstraintSet constraintSet) {
        constraintSet.connect(
                view.getId(),ConstraintSet.RIGHT,
                R.id.playFunctionsExecutionPageRootLayout,ConstraintSet.RIGHT,
                0);
    }

    private void allTopsToParentTop(TextView view1, TextView view2, TextView view3, ConstraintSet constraintSet) {
        constraintSet.connect(
                view1.getId(),ConstraintSet.TOP,
                R.id.playFunctionsExecutionPageRootLayout,ConstraintSet.TOP,
                0);

        constraintSet.connect(
                view2.getId(),ConstraintSet.TOP,
                R.id.playFunctionsExecutionPageRootLayout,ConstraintSet.TOP,
                0);

        constraintSet.connect(
                view3.getId(),ConstraintSet.TOP,
                R.id.playFunctionsExecutionPageRootLayout,ConstraintSet.TOP,
                0);
    }

    private void allBottomsToTopOfFakeGuitar(TextView view1, TextView view2, TextView view3, ConstraintSet constraintSet) {
        constraintSet.connect(
                view1.getId(),ConstraintSet.BOTTOM,
                fakeGuitarFragmentContainer.getId(),ConstraintSet.TOP,
                0);

        constraintSet.connect(
                view2.getId(),ConstraintSet.BOTTOM,
                fakeGuitarFragmentContainer.getId(),ConstraintSet.TOP,
                0);

        constraintSet.connect(
                view3.getId(),ConstraintSet.BOTTOM,
                fakeGuitarFragmentContainer.getId(),ConstraintSet.TOP,
                0);
    }

    private void horizontalChain(TextView view1, TextView view2, ConstraintSet constraintSet) {
        constraintSet.createHorizontalChain(
                ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                new int[] {view1.getId(), view2.getId()},
                null,
                ConstraintSet.CHAIN_PACKED

        );
    }

    private void allHeightsToMatchConstraintAllWidthToWrapContent(TextView view1, TextView view2, TextView view3, ConstraintSet constraintSet) {
        constraintSet.constrainHeight(view1.getId(), ConstraintSet.MATCH_CONSTRAINT);
        constraintSet.constrainWidth(view1.getId(), ConstraintSet.WRAP_CONTENT);

        constraintSet.constrainHeight(view2.getId(), ConstraintSet.MATCH_CONSTRAINT);
        constraintSet.constrainWidth(view2.getId(), ConstraintSet.WRAP_CONTENT);

        constraintSet.constrainHeight(view3.getId(), ConstraintSet.MATCH_CONSTRAINT);
        constraintSet.constrainWidth(view3.getId(), ConstraintSet.WRAP_CONTENT);
    }

    @Override
    public void firstThingsToDoAfterGameEnds(){
        saveLevelStats(resultTime);
    }

    public void saveLevelStats(int successSeconds){
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                PlayFunctionsCardStatsProvider.CONTENT_URI);

        PlayFunctionsCardStatsProvider provider =
                (PlayFunctionsCardStatsProvider) client.getLocalContentProvider();

        provider.insertOrUpdateCard(new CardStats(
                cardUniqueId,
                successSeconds,
                levelType
        ));
    }
}