package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Raul on 07/11/2016.
 */

public class CompoundAdapter extends ArrayAdapter<CompoundButton> {
    ArrayList<CompoundButton> compoundButtons = new ArrayList<>();

    public CompoundAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public void add(CompoundButton object) {
        compoundButtons.add(object);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return compoundButtons.get(position);
    }

    @Override
    public int getCount() {
        return compoundButtons.size();
    }
}
