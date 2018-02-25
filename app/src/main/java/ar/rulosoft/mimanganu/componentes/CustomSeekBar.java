package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

/**
 * Created by Raúl on 25/02/2018.
 */

public class CustomSeekBar extends AppCompatSeekBar {
    int min = 0;
    int max;
    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setMin(int nMin){
        min = nMin;
        super.setMax(max - min);
    }

    @Override
    public void setMax(int nMax){
        max = nMax;
        super.setMax(max - min);
    }

    @Override
    public int getProgress(){
        int oP = super.getProgress();
        return oP + min;
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress - min);
    }

    @Override
    public void setProgress(int progress, boolean animate) {
        super.setProgress(progress - min, animate);
    }
}
