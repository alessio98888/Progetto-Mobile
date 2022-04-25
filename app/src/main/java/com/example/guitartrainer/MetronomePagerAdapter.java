/*
 * @(#) MetronomePagerAdapter.java     1.0 05/01/2022
 */

package com.example.guitartrainer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 *
 Creates metronome page fragments that are visible in the view pager.
 *
 * @version
1.00 05/01/2022
 * @author
Alessio Ardu  */
public class MetronomePagerAdapter extends FragmentStateAdapter {

    private MetronomeBasic metronomeBasic;
    private MetronomeProgrammable metronomeProgrammable;

    public MetronomePagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        if (position == 1){
            if(metronomeBasic ==null)
                metronomeBasic = new MetronomeBasic();
            return metronomeBasic;
        } else {
            if (metronomeProgrammable ==null)
                metronomeProgrammable = new MetronomeProgrammable();
            return metronomeProgrammable;
        }

    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public MetronomeBasic getMetronomeBasic() {
        return metronomeBasic;
    }

    public MetronomeProgrammable getMetronomeProgrammable() {
        return metronomeProgrammable;
    }


}
