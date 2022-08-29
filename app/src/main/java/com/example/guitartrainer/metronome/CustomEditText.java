/*
 * @(#) CustomEditText.java     1.0 05/01/2022
 */
package com.example.guitartrainer.metronome;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 *
 Custom edit text that adds to the default edit text the fact that the keyboard cannot be
 hidden by pressing the back button. It can only be hidden by pressing the "Done" button.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {

    public CustomEditText(@NonNull Context context) {
        super(context);
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // User has pressed Back key. Do not hide when keyboard is visible.
            // Hides only when Done is pressed.
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(getRootView());
            boolean keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (keyboardVisible){
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
