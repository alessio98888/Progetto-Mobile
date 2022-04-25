/*
 * @(#) FeatureButton.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.Navigation;

/**
 *
 Button used for accessing the app main features (like the metronome).
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class FeatureButton extends AppCompatButton {
    private final int IMAGE_RIGHT_BOTTOM_MARGINS = 60;
    public FeatureButton(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public FeatureButton(Context context,
                         @NonNull LinearLayout layout,
                         String feature_name,
                         int icon_resource_id,
                         int navigation_action_id
    ){
        super(context);
        FeatureButton_core(context, layout, feature_name, icon_resource_id);
        this.setOnClickListener(Navigation.createNavigateOnClickListener(navigation_action_id));
    }

    // One Activity for each flow
    public FeatureButton(Context context,
                         @NonNull LinearLayout layout,
                         String feature_name,
                         int icon_resource_id,
                         Class<?> activityToCreateClass
    ){
        super(context);
        FeatureButton_core(context, layout, feature_name, icon_resource_id);
        this.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                context.startActivity(new Intent(context, activityToCreateClass));
            }
        });
    }

    private void FeatureButton_core(Context context,
                               @NonNull LinearLayout layout,
                               String feature_name,
                               int icon_resource_id){
        int side_margin = getResources().getInteger(R.integer.feature_button_side_margin_dp);
        int top_bottom_margin = getResources().getInteger(R.integer.feature_button_top_bottom_margin_dp);
        int padding_bottom = getResources().getInteger(R.integer.feature_button_padding_bottom_dp);
        int padding_left = getResources().getInteger(R.integer.feature_button_padding_left_dp);
        int padding_top = getResources().getInteger(R.integer.feature_button_padding_top_dp);
        int padding_right = getResources().getInteger(R.integer.feature_button_padding_right_dp);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();

        params.setMargins(GeneralUtils.convertDpToPx(side_margin, dm),
                GeneralUtils.convertDpToPx(top_bottom_margin, dm),
                GeneralUtils.convertDpToPx(side_margin, dm),
                GeneralUtils.convertDpToPx(top_bottom_margin, dm));
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        this.setLayoutParams(params);

        this.setPadding(GeneralUtils.convertDpToPx(padding_left, dm),
                GeneralUtils.convertDpToPx(padding_top, dm),
                GeneralUtils.convertDpToPx(padding_right, dm),
                GeneralUtils.convertDpToPx(padding_bottom, dm));

        this.setText(feature_name);

        Drawable img = ResourcesCompat.getDrawable(context.getResources(),
                icon_resource_id, context.getTheme());
        img.setBounds(0, 0, IMAGE_RIGHT_BOTTOM_MARGINS, IMAGE_RIGHT_BOTTOM_MARGINS);
        this.setCompoundDrawables(img, null, null, null);
        this.setBackgroundColor(Color.parseColor("#DAA300"));
    }
}
