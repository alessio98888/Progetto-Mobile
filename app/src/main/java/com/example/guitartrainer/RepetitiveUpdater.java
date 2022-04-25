/*
 * @(#) RepetitiveUpdater.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

/**
 *
 Implements the feature that permits to hold on an increment/decrement view
 to make the respective number increase or decrease automatically.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
class RepetitiveUpdater implements Runnable {

    private final AutoIncrementInterface AUTO_INCREMENT;

    public RepetitiveUpdater(AutoIncrementInterface a) {
        this.AUTO_INCREMENT = a;
    }

    @Override
    public void run() {

        if (AUTO_INCREMENT.isAutoIncrement()) {
            AUTO_INCREMENT.increment();
            AUTO_INCREMENT.getRepeatUpdateHandler().postDelayed(new RepetitiveUpdater(AUTO_INCREMENT), AUTO_INCREMENT.getRepeatDelay());
        } else if (AUTO_INCREMENT.isAutoDecrement()) {
            AUTO_INCREMENT.decrement();
            AUTO_INCREMENT.getRepeatUpdateHandler().postDelayed(new RepetitiveUpdater(AUTO_INCREMENT), AUTO_INCREMENT.getRepeatDelay());
        }
    }
}

