package com.example.guitartrainer.earTraining;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.guitartrainer.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Game for guessing the function of a note.
 *
 * All MediaPlayer listeners are registered on the worker thread.
 */
public class GuessFunctionExecutionPage extends Fragment {
    private boolean automaticAnswersWithVoice;
    private TextToSpeech textToSpeech;
    private UtteranceProgressListener utteranceProgressListener;
    private GuessFunctionLevel.LevelType levelType;

    private final int MIN_OCTAVE_SUPPORTED = 1;
    private final int MAX_OCTAVE_SUPPORTED = 7;

    private ArrayList<MusicalNote.MusicalNoteName> rootNotesNames;
    private MusicalProgression.MusicalProgressionId progressionId;
    private MusicalScale.ScaleMode scaleMode;

    private int round = 0;
    private final int MAX_ROUND = 3;

    private int correctAnswers;
    private int incorrectAnswers;

    private TextView roundText;
    private TextView successPercText;
    private TextView correctAnswersText;
    private TextView incorrectAnswersText;

    private MusicalScaleNote currentNoteToGuess;

    private MusicalProgression currentProgressionToPlay;

    private int successPerc;

    private int notePlayersPrepared = 0;
    private int progressionPlayersPrepared = 0;

    private int numberOfNotePlayersToInit;
    private Vector<Vector<MediaPlayer>> notePlayers;
    private Map<MusicalNote.MusicalNoteName, MediaPlayer> progressionPlayers;

    private OctaveOption.EarTrainingOctaveOptionEnum earTrainingOctaveOptionEnum;

    private boolean wrongAnswer = false;

    private boolean canAnswer;

    private Button repeatNoteButton;
    private Button repeatButton;

    private String cardUniqueId;

    private boolean levelEnded = false;

    private ArrayList<Button> answerButtons;

    private TextView giantFunctionNumberText;
    private ProgressBar progressBar;
    private HandlerThread handlerThreadWorker;
    private Handler handlerMain;
    private final int DEFAULT_OCTAVE = 4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.fragment_ear_training_exercise_execution_page,
                container, false);

        roundText = returnView.findViewById(R.id.currentRoundText);
        return returnView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        canAnswer = false;
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();

        this.getView().setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    levelEnded = true;
                }
                return false;
            }
        });
        setTextViews();
        setAnswerButtons();
        setRepeatButtons();
        setTextViewsVisibilityState(false);
        setAllButtonsVisibilityState(false);
        assert getArguments() != null;
        automaticAnswersWithVoice = getArguments().getBoolean(
                "automaticAnswersWithVoice",
                false);

        if (automaticAnswersWithVoice) {
            initTextToSpeech();
        }

        earTrainingOctaveOptionEnum = OctaveOption.EarTrainingOctaveOptionEnum.values()[
                getArguments().getInt("ear_training_option_index")];

        scaleMode = MusicalScale.ScaleMode.values()[
                getArguments().getInt("scaleMode")];

        cardUniqueId = getArguments().getString("cardUniqueId");

        levelType = GuessFunctionLevel.LevelType.values()[
                getArguments().getInt("levelType")];

        if (!automaticAnswersWithVoice) {
            setScoreStats(0, 0, 0);
            setRepeatButtonListeners();
            setAnswerButtonListeners();
            giantFunctionNumberText.setVisibility(View.INVISIBLE);
        }

        progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        handlerMain = new Handler(Looper.getMainLooper());
        handlerThreadWorker = new HandlerThread("MyHandlerThread");
        handlerThreadWorker.start();
        Looper looper = handlerThreadWorker.getLooper();
        Handler handlerForBackgroundThread = new Handler(looper);
        handlerForBackgroundThread.post(new Runnable() {
            @Override
            public void run() {
                //Background work here
                progressionPlayers = new HashMap<>();

                progressionId = MusicalProgression.MusicalProgressionId.values()[
                        getArguments().getInt("musicalProgression")];

                int[] rootNotesArray = getArguments().getIntArray("rootNotes");
                rootNotesNames = MusicalNote.toMusicalNotesNames(
                        (ArrayList<Integer>) Arrays.stream(rootNotesArray).boxed().collect(Collectors.toList()));

                for(int i = 0; i< rootNotesNames.size(); i++) {
                    int progressionResId =
                            MusicalProgression.getResId(rootNotesNames.get(i), progressionId, true,
                                    scaleMode);

                    AssetFileDescriptor afd;
                    MediaPlayer progressionPlayer = new MediaPlayer();

                    try {
                        afd = getContext().getResources().openRawResourceFd(progressionResId);

                        progressionPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                        afd.close();
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    progressionPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer player) {
                            progressionPlayersPrepared+=1;
                            int progress = (int) (((float)
                                    (notePlayersPrepared+progressionPlayersPrepared) /
                                    (rootNotesNames.size()+numberOfNotePlayersToInit))*100);

                            //Log.d("Thread main", String.valueOf(Thread.currentThread().equals(
                            // Looper.getMainLooper().getThread() )));
                            handlerMain.post(() -> progressBar.setProgress(progress));

                            if ((notePlayersPrepared == numberOfNotePlayersToInit) &&
                                    (progressionPlayersPrepared == rootNotesNames.size())) {
                                handlerMain.post(GuessFunctionExecutionPage.this::playFirstRound);
                            }
                        }
                    });

                    progressionPlayers.put(rootNotesNames.get(i), progressionPlayer);

                }
                numberOfNotePlayersToInit = 0;
                notePlayers = new Vector<>();
                List<String> noteNames = MusicalNote.getNoteNames();

                boolean allNotes = rootNotesNames.size() == noteNames.size();
                ArrayList<MusicalNote.MusicalNoteName> scalesAllPlayableNotes = null;

                if(!allNotes){
                    scalesAllPlayableNotes = new ArrayList<>();
                    for(int i=0; i< rootNotesNames.size(); i++){
                        scalesAllPlayableNotes.addAll(MusicalScale.getScaleNotes(rootNotesNames.get(i),
                                scaleMode));
                    }
                }

                for(int i = MIN_OCTAVE_SUPPORTED; i<= MAX_OCTAVE_SUPPORTED; i++){
                    Vector<MediaPlayer> r=new Vector<>();
                    Integer noteId = null;

                    for(int j=0;j<noteNames.size();j++){
                        MusicalNote.MusicalNoteName noteName =
                                MusicalNote.MusicalNoteName.valueOf(noteNames.get(j));

                        // Add player only if the note can be played with current level options
                        if (allNotes || scalesAllPlayableNotes.contains(noteName)){
                            try {
                                noteId = R.raw.class.getField(
                                        noteNames.get(j) + Integer.toString(i)
                                ).getInt(null);

                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            AssetFileDescriptor afd;
                            MediaPlayer notePlayer = new MediaPlayer();

                            try {
                                afd = getContext().getResources().openRawResourceFd(noteId);

                                notePlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                                        afd.getLength());

                                afd.close();
                            } catch (IOException | NullPointerException e) {
                                e.printStackTrace();
                            }
                            notePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer player) {
                                    notePlayersPrepared+=1;
                                    int progress = (int) (((float)
                                            (notePlayersPrepared+progressionPlayersPrepared) /
                                            (rootNotesNames.size()+numberOfNotePlayersToInit))*100);

                                    //Log.d("Thread main", String.valueOf(Thread.currentThread().equals(
                                    // Looper.getMainLooper().getThread() )));
                                    handlerMain.post(() -> progressBar.setProgress((int) progress));

                                    if ((notePlayersPrepared == numberOfNotePlayersToInit) &&
                                            (progressionPlayersPrepared == rootNotesNames.size())) {

                                        handlerMain.post(GuessFunctionExecutionPage.this::playFirstRound);
                                    }
                                }

                            });

                            r.add(notePlayer);
                            numberOfNotePlayersToInit++;
                        } else {
                            r.add(null);
                        }
                    }
                    notePlayers.add(r);
                }

                for(Vector<MediaPlayer> r : notePlayers){
                    for(MediaPlayer notePlayer : r){
                        if (notePlayer != null) {
                            try {
                                notePlayer.prepare();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                for(MediaPlayer player : progressionPlayers.values()){
                    try {
                        player.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });
    }

    public void setAllButtonsVisibilityState(boolean visible){
        int visState = visible ? View.VISIBLE : View.INVISIBLE;
        for (int i=0; i<answerButtons.size(); i++){
            answerButtons.get(i).setVisibility(visState);
        }
        repeatButton.setVisibility(visState);
        repeatNoteButton.setVisibility(visState);
    }

    public void setRepeatButtonListeners(){
       repeatNoteButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               playNote(currentNoteToGuess.getOctave(), currentNoteToGuess.getNoteName());
           }
       });

       repeatButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               canAnswer=false;
               playRound(currentProgressionToPlay, currentNoteToGuess);
           }
       });
    }

    public void playFirstRound(){
        if (isNotePlayersReady() && isProgressionsPlayersReady()) {

            if(!isAdded()){ // necessary because the user can press the back button while the handler
                            // thread for the media loading is running, and can happen that the handler post to
                            // the main thread happens after the fragment has been detached (isAdded = false)
                return;
            }
            progressBar.setVisibility(View.INVISIBLE);

            if(!automaticAnswersWithVoice){
                setTextViewsVisibilityState(true);
                setAllButtonsVisibilityState(true);
            }
            playNextRound();
        }
    }

    public void playNextRound(){
        setNextRound();

        canAnswer = false;
        disableButtons();
        playRound(currentProgressionToPlay, currentNoteToGuess);
    }

    public void setEnableStatusForAllButtons(boolean newState){
        for (Button b: answerButtons) {
            b.setEnabled(newState);
        }
        repeatButton.setEnabled(newState);
        repeatNoteButton.setEnabled(newState);
    }

    public void disableButtons(){
        setEnableStatusForAllButtons(false);
    }

    public void enableButtons(){
        setEnableStatusForAllButtons(true);
    }

    public void playRound(MusicalProgression progression, MusicalNote note){

        progressionPlayers.get(progression.getRootNote()).start();
        progressionPlayers.get(progression.getRootNote()).setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Executes on worker thread
                playNote(note.getOctave(), note.getNoteName());
            }
        });
    }

    public void setNextRound(){
        currentProgressionToPlay = getNextProgressionToPlay();
        currentNoteToGuess = getNextNoteToGuess();

        if(!automaticAnswersWithVoice) {
            setRound(getRound()+1);
        } else {
            String text = String.format(getResources().getString(R.string.ear_training_giant_function_value_text),
                    currentNoteToGuess.getMusicalFunction());
            giantFunctionNumberText.setText(text);
        }
    }

    public void sendAnswer(int answer){
        if(!canAnswer){
           return;
        }

        boolean lastRound = false;
        if (getRound() == MAX_ROUND) {
            lastRound = true;
        }

        if (answer == currentNoteToGuess.getMusicalFunction()) {
            correctAnswers+=1;
            wrongAnswer = false;

            setScoreStats(calculateSuccessPerc(), correctAnswers, incorrectAnswers);

            if (!lastRound) {
                playNextRound();
            }

        } else {

            wrongAnswer = true;
            incorrectAnswers+=1;

            setScoreStats(calculateSuccessPerc(), correctAnswers, incorrectAnswers);
        }

        if (lastRound && !wrongAnswer) {

            saveLevelStats();
            releaseNotePlayers();
            releaseProgressionPlayers();
            showEndLevelPopupDialog();

        }
    }

    public void saveLevelStats(){
        ContentResolver resolver = getActivity().getContentResolver();

        ContentProviderClient client = resolver.acquireContentProviderClient(
                CardStatsProvider.CONTENT_URI);

        CardStatsProvider provider =
                (CardStatsProvider) client.getLocalContentProvider();

        provider.insertOrUpdateCard(new CardStats(
                cardUniqueId,
                successPerc,
                levelType
        ));
    }

    public void showEndLevelPopupDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        final View POPUP_VIEW =
                ((LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE )).
                        inflate(R.layout.end_of_guess_function_level_popup, null);

        Button continueButton = POPUP_VIEW.findViewById(R.id.continue_button);

        TextView successPercText = POPUP_VIEW.findViewById(R.id.success_perc_text_popup);
        setSuccessPerc(successPerc, successPercText);

        dialogBuilder.setView(POPUP_VIEW);
        AlertDialog dialog = dialogBuilder.create();
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                navigateToEarTrainingMainPage();
            }
        });

        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                navigateToEarTrainingMainPage();
            }
        });
    }

    public void navigateToEarTrainingMainPage(){
        levelEnded = true;
        Navigation.findNavController(getView()).navigate(
                R.id.action_earTrainingExerciseExecutionPage_to_earTrainingMainPage);
    }

    public void setAnswerButtons(){
        answerButtons = new ArrayList<>();

        Button button = getView().findViewById(R.id.button1);
        answerButtons.add(button);

        button = getView().findViewById(R.id.button2);
        answerButtons.add(button);

        button = getView().findViewById(R.id.button3);
        answerButtons.add(button);

        button = getView().findViewById(R.id.button4);
        answerButtons.add(button);

        button = getView().findViewById(R.id.button5);
        answerButtons.add(button);

        button = getView().findViewById(R.id.button6);
        answerButtons.add(button);

        button = getView().findViewById(R.id.button7);
        answerButtons.add(button);

    }

    public void setAnswerButtonListeners(){
        for(int i=0; i<answerButtons.size(); i++) {
            int finalI = i;
            answerButtons.get(i).setOnClickListener(view -> sendAnswer(finalI + 1));
        }
    }

    public boolean playNote(int octave, MusicalNote.MusicalNoteName note) {
        boolean canPlayNote = isNotePlayersReady();

        if (canPlayNote) {

            getNotePlayer(octave,note.ordinal()).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Executes on worker thread
                    if (automaticAnswersWithVoice) {
                        textToSpeech.speak(Integer.toString(currentNoteToGuess.getMusicalFunction()),
                                TextToSpeech.QUEUE_FLUSH, null,
                                TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                    } else {
                        enableButtons();
                        canAnswer=true;
                    }
                }
            });
            getNotePlayer(octave,note.ordinal()).start();
        }
        return canPlayNote;
    }

    public void releaseProgressionPlayers(){
        for(int i = 0; i< rootNotesNames.size(); i++){
            MediaPlayer player = progressionPlayers.get(rootNotesNames.get(i));
            if(player != null) {
                player.release();
            }
        }
    }

    public void releaseNotePlayers(){
        for(int i = MIN_OCTAVE_SUPPORTED; i<= MAX_OCTAVE_SUPPORTED; i++) {
            for (int j = 0; j < MusicalNote.getNoteNames().size(); j++) {
                MediaPlayer player = getNotePlayer(i,j);
                if(player != null) {
                    player.release();
                }
            }
        }
    }

    public MediaPlayer getNotePlayer(int octave, int noteIndex){
        return notePlayers.get(octave- MIN_OCTAVE_SUPPORTED).get(noteIndex);
    }

    public boolean isNotePlayersReady(){
       return numberOfNotePlayersToInit == notePlayersPrepared;
    }

    public boolean isProgressionsPlayersReady(){
        return rootNotesNames.size() == progressionPlayersPrepared;
    }

    public MusicalNote.MusicalNoteName getRandomMusicalNote() {
        Random rand = new Random();
        return rootNotesNames.get(rand.nextInt(rootNotesNames.size()));
    }

    public MusicalScaleNote getNextNoteToGuess(){

        MusicalScaleNote scaleNote = MusicalScale.getRandomScaleNote(
                currentProgressionToPlay.getRootNote(), scaleMode);

        int octave = DEFAULT_OCTAVE;
        if (earTrainingOctaveOptionEnum == OctaveOption.EarTrainingOctaveOptionEnum.Many_Octaves) {
            octave = (int) Math.floor(Math.random()*
                    (MAX_OCTAVE_SUPPORTED - MIN_OCTAVE_SUPPORTED +1)+ MIN_OCTAVE_SUPPORTED);
        }

        scaleNote.setOctave(octave);
        return scaleNote;
    }

    public MusicalProgression getNextProgressionToPlay(){
        MusicalProgression progressionToPlay = new MusicalProgression(
                getRandomMusicalNote(), progressionId, true, scaleMode);
        return progressionToPlay;
    }

    public int calculateSuccessPerc(){
        return (int) Math.floor((float)correctAnswers/(float)(correctAnswers+incorrectAnswers) * 100);
    }

    public void setSuccessPerc(int perc, TextView successPercText){
        this.successPerc = perc;
        String text = String.format(getResources().getString(R.string.ear_training_success_perc_text),
                perc);
        successPercText.setText(text);
    }

    public void setScoreStats(int perc, int correctAnswers, int incorrectAnswers){
        setSuccessPerc(perc, successPercText);
        setCorrectIncorrectAnswers(correctAnswers, incorrectAnswers);
    }

    public void setCorrectIncorrectAnswers(int correctAnswers, int incorrectAnswers){
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;

        String text = String.format(getResources().getString(R.string.earTrainingCorrectAnswers),
                correctAnswers);
        correctAnswersText.setText(text);

        text = String.format(getResources().getString(R.string.earTrainingIncorrectAnswers),
                incorrectAnswers);
        incorrectAnswersText.setText(text);
    }

    public void setRound(int round){
        this.round = round;

        String text = "";

         text = String.format(getResources().getString(R.string.ear_training_round_text),
                Integer.toString(round), Integer.toString(MAX_ROUND));
        roundText.setText(text);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handlerThreadWorker.quit();
        releaseNotePlayers();
        releaseProgressionPlayers();
        if(automaticAnswersWithVoice) {
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(!levelEnded){ // Stopped not because of level ended
            ((Activity) getView().getContext()).finish();
        }
    }

    public int getRound(){
        return round;
    }


    public void setRepeatButtons(){
        repeatNoteButton = getView().findViewById(R.id.earTrainingExecutionPageRepeatNote);
        repeatButton = getView().findViewById(R.id.earTrainingExecutionPageRepeat);
    }

    public void setTextViews(){
        successPercText = getView().findViewById(R.id.successPercText);
        correctAnswersText = getView().findViewById(R.id.correctAnswersText);
        incorrectAnswersText = getView().findViewById(R.id.incorrectAnswersText);
        giantFunctionNumberText = getView().findViewById(R.id.functionNumberGiantTextView);
    }

    public void initTextToSpeech(){
        utteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                Log.i("TextToSpeech","On Done");

                getActivity().runOnUiThread(() -> playNextRound());
            }

            @Override
            public void onError(String s) {

            }
        };

        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        textToSpeech.setLanguage(Locale.ENGLISH);
                        Log.e("TTS", "Language not supported");
                    }

                    textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                } else {
                    Log.e("TTS", "Failed");
                }
            }
        });
    }

    public void setTextViewsVisibilityState(boolean visible){
        int visState = visible ? View.VISIBLE : View.INVISIBLE;
        successPercText.setVisibility(visState);
        correctAnswersText.setVisibility(visState);
        incorrectAnswersText.setVisibility(visState);
        roundText.setVisibility(visState);
    }
}