package com.example.guitartrainer.metronome;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class GeneralUtils {
    public static int convertDpToPx(int dp, DisplayMetrics displayMetrics) {
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
        return Math.round(pixels);
    }


}
