package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.util.AttributeSet;

import com.buildware.widget.indeterm.IndeterminateCheckBox;

public class CheckBox3 extends IndeterminateCheckBox {

    public CheckBox3(Context context) {
        super(context);
    }

    public CheckBox3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBox3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void toggle() {
        if (isIndeterminate()) {
            setChecked(false);
        } else if (!isChecked()) {
            setChecked(true);
        } else if (isChecked()) {
            setIndeterminate(true);
        }
    }

    public void setState(int state) {
        if (state == -1) {
            setIndeterminate(true);
        } else {
            setState(state > 0);
        }
    }

    public int getIntState() {
        if (getState() == null) {
            return -1;
        } else if (getState()) {
            return 1;
        } else {
            return 0;
        }
    }
}
