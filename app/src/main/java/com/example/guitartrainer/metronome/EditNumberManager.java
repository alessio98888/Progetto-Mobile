/*
 * @(#) EditNumberManager.java     1.0 05/01/2022
 */

package com.example.guitartrainer.metronome;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

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

    private ViewPager2 viewPager;
    private void initCoreNumberManager(View view, int numberEditFieldId, int minusButtonId,
                                       int plusButtonId, int initialNumber, int maxNumber,
                                       int minNumber, ViewPager2 viewPager){
        editNumber = view.findViewById(numberEditFieldId);
        minusButton = view.findViewById(minusButtonId);
        plusButton = view.findViewById(plusButtonId);
        this.viewPager = viewPager;
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
                             int plusButtonId, int initialNumber, int maxNumber, int minNumber, ViewPager2 viewPager){
        initCoreNumberManager(view, numberEditFieldId, minusButtonId,
                plusButtonId, initialNumber, maxNumber, minNumber, viewPager);
    }

    public EditNumberManager(View view, int numberEditFieldId, int minusButtonId,
                             int plusButtonId, int initialNumber, Observer observer, int maxNumber,
                             int minNumber, ViewPager2 viewPager){
        this.observer = observer;
        initCoreNumberManager(view, numberEditFieldId, minusButtonId, plusButtonId,
                initialNumber, maxNumber, minNumber, viewPager);
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
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(autoIncrement){
                autoIncrement = false;
            }
            onClickOut();

        } else if(event.getAction() == MotionEvent.ACTION_DOWN){
            onClickIn();
        }
        return false;
    }

    private boolean minusButtonTouchListener(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(autoDecrement){
                autoDecrement = false;
            }
            onClickOut();

        } else if(event.getAction() == MotionEvent.ACTION_DOWN){
            onClickIn();
        }
        return false;
    }

    private void onClickOut(){
        enableViewPagerSwipe();
    }

    private void enableViewPagerSwipe(){
        if(viewPager != null){
            viewPager.setUserInputEnabled(true);
        }
    }

    private void onClickIn(){
        disableViewPagerSwipe();
    }

    private void disableViewPagerSwipe(){
        if(viewPager != null){
            viewPager.setUserInputEnabled(false);
        }
    }

    private void plusButtonClick() {
        incrementNumber();
    }

    private void minusButtonClick() {
        decrementNumber();
    }

    public void incrementNumber(){
        setNumber(getNumber() + 1, true);
    }

    public void decrementNumber(){
        setNumber(getNumber() - 1, true);
    }

    private boolean plusButtonOnLongClick() {
        autoIncrement = true;
        return coreButtonOnLongClick();
    }

    private boolean minusButtonOnLongClick() {
        autoDecrement = true;
        return coreButtonOnLongClick();
    }

    private boolean coreButtonOnLongClick() {
        REPEAT_UPDATE_HANDLER.post(new RepetitiveUpdater(this));

        // means the event is not consumed. Any other click events will continue to receive notifications.
        return false;
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
