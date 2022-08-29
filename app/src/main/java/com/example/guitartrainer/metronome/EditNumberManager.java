/*
 * @(#) EditNumberManager.java     1.0 05/01/2022
 */

package com.example.guitartrainer.metronome;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;

/**
 *
 Helps to manage an edit number view with a plus and minus button.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class EditNumberManager implements AutoIncrementInterface{

    private boolean autoIncrement = false;
    private boolean autoDecrement = false;

    private int maxNumber;
    private int minNumber;
    private int number;

    private boolean outOfBound = false;
    final private Handler REPEAT_UPDATE_HANDLER = new Handler();

    public CustomEditText getEditNumber() {
        return editNumber;
    }

    private CustomEditText editNumber;
    private ImageButton minusButton;
    private ImageButton plusButton;

    private Observer observer;

    private final int DEFAULT_REPEAT_DELAY = 60;
    private long repeatDelay = DEFAULT_REPEAT_DELAY;

    private void initCoreNumberManager(View view, int numberEditFieldId, int minusButtonId,
                                       int plusButtonId, int initialNumber, int maxNumber,
                                       int minNumber){
        editNumber = view.findViewById(numberEditFieldId);
        minusButton = view.findViewById(minusButtonId);
        plusButton = view.findViewById(plusButtonId);
        setBounds(minNumber, maxNumber);
        setNumber(initialNumber, true);
        setListeners();
    }

    private void setBounds(int minNumber, int maxNumber) {
        if (minNumber >= maxNumber) {
            throw new IllegalArgumentException();
        }
        else {
            this.maxNumber = maxNumber;
            this.minNumber = minNumber;
        }
    }

    public EditNumberManager(View view, int numberEditFieldId, int minusButtonId,
                             int plusButtonId, int initialNumber, int maxNumber, int minNumber){
        initCoreNumberManager(view, numberEditFieldId, minusButtonId,
                plusButtonId, initialNumber, maxNumber, minNumber);
    }

    public EditNumberManager(View view, int numberEditFieldId, int minusButtonId,
                             int plusButtonId, int initialNumber, Observer observer, int maxNumber,
                             int minNumber){
        this.observer = observer;
        initCoreNumberManager(view, numberEditFieldId, minusButtonId, plusButtonId,
                initialNumber, maxNumber, minNumber);
    }

    private void setListeners() {

        plusButton.setOnClickListener((View v) -> plusButtonClick());
        plusButton.setOnLongClickListener(v -> plusButtonOnLongClick());
        plusButton.setOnTouchListener((v, event) -> plusButtonTouchListener(event));

        minusButton.setOnClickListener(v -> minusButtonClick());
        minusButton.setOnLongClickListener(v -> minusButtonOnLongClick());
        minusButton.setOnTouchListener((v, event) -> minusButtonTouchListener(event));

        editNumber.setOnEditorActionListener((v, actionId, event) -> editTextDoneClick(actionId));
    }

    private boolean editTextDoneClick(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            syncValueWithEdittextValue();
        }
        return false;
    }

    public void syncValueWithEdittextValue(){
        String editTextValue = editNumber.getText().toString();
        if (editTextValue.equals("")) {
            editTextValue = Integer.toString(getNumber());
        }
        setNumber(Integer.parseInt(editTextValue), true);
    }

    private boolean plusButtonTouchListener(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
            autoIncrement = false;
        }
        return false;
    }

    private boolean minusButtonTouchListener(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
            autoDecrement = false;
        }
        return false;
    }

    private void plusButtonClick() {
        incrementNumber();
    }

    private void minusButtonClick() {
        decrementNumber();
    }

    private boolean coreButtonOnLongClick() {
        REPEAT_UPDATE_HANDLER.post(new RepetitiveUpdater(this));

        // means the event is not consumed. Any other click events will continue to receive notifications.
        return false;
    }

    private boolean plusButtonOnLongClick() {
        autoIncrement = true;
        return coreButtonOnLongClick();
    }

    private boolean minusButtonOnLongClick() {
        autoDecrement = true;
        return coreButtonOnLongClick();
    }

    public void setNumber(int number, boolean updateTextField) {

        if (number > maxNumber ) {
            this.number = maxNumber;
            outOfBound = true;
        } else if (number < minNumber) {
            this.number = minNumber;
            outOfBound = true;
        } else {
            this.number = number;
            outOfBound = false;
        }

        if (updateTextField) {
            editNumber.setText(String.valueOf(this.number));
        }

        if (observer != null) {
            ObserverData obsData = new ObserverData();
            obsData.value = getNumber();
            obsData.updatingClassInstance = this;
            observer.update(obsData);
        }
    }

    public void incrementNumber(){
        setNumber(getNumber() + 1, true);
    }

    public void decrementNumber(){
        setNumber(getNumber() - 1, true);
    }

    public void increment(){
        incrementNumber();
    }

    public void decrement(){
        decrementNumber();
    }

    public int getNumber(){
        return number;
    }

    public long getRepeatDelay() {
        return repeatDelay;
    }

    public void setRepeatDelay(long repeatDelay) {
        this.repeatDelay = repeatDelay;
    }

    public Handler getRepeatUpdateHandler() {
        return REPEAT_UPDATE_HANDLER;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isAutoDecrement() {
        return autoDecrement;
    }

}
