/*
 * @(#) MainActivity.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 *
 Main activity.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }
}