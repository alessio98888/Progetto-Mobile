package com.example.guitartrainer.earTraining;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.guitartrainer.R;

import java.util.Arrays;
import java.util.List;

public class ProgressionVelocitySpinner
        extends androidx.appcompat.widget.AppCompatSpinner
        implements AdapterView.OnItemSelectedListener
{
    private static final int DEFAULT_SELECTED = 1;
    private int selected = DEFAULT_SELECTED;

    private List<Float> velocities = Arrays.asList(0.75f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f);
    private List<String> velocitiesText = Arrays.asList("0.75x", "1x", "1.5x", "2x", "2.5x", "3x");

    public void init(Context c){
        initAdapter(c);
        this.setOnItemSelectedListener(this);
        this.setSelection(DEFAULT_SELECTED);
    }

    private void initAdapter(Context c) {
        ArrayAdapter<String> aa = new ArrayAdapter<>(c, R.layout.progression_velocity_spinner_text_style, velocitiesText);
        //aa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        this.setAdapter(aa);
    }

    public void setSelected(float velocity){
        selected = velocities.indexOf(velocity);
        this.setSelection(selected);
    }

    public ProgressionVelocitySpinner(Context context) {
        super(context);
        init(context);
    }

    public ProgressionVelocitySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressionVelocitySpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selected = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public Float getSelectedItem(){
        return velocities.get(selected);
    }
}
