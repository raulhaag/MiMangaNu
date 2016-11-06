package ar.rulosoft.mimanganu.componentes;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

public class CompoundGroup {

    List<CompoundButton> radios = new ArrayList<CompoundButton>();
    OnClickListener onClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            for (CompoundButton rb : radios) {
                if (rb != v) {
                    rb.setChecked(false);
                } else {
                    rb.setChecked(true);
                }
            }
        }
    };

    public CompoundGroup(boolean single, CompoundButton... radios) {
        super();
        for (CompoundButton cb : radios) {
            this.radios.add(cb);
            cb.setOnClickListener(onClick);
        }
    }

    public void add(CompoundButton cb) {
        this.radios.add(cb);
        cb.setOnClickListener(onClick);
    }
}