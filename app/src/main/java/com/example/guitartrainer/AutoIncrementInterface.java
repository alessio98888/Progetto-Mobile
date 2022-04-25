/*
 * @(#) AutoIncrementInterface.java     1.0 05/01/2022
 */

package com.example.guitartrainer;


import android.os.Handler;

public interface AutoIncrementInterface {
    boolean isAutoIncrement();
    boolean isAutoDecrement();

    void increment();
    void decrement();

    long getRepeatDelay();

    Handler getRepeatUpdateHandler();
}
