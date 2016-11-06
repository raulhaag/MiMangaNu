package ar.rulosoft.mimanganu.componentes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.Gravity.CENTER;
import static android.widget.LinearLayout.VERTICAL;


/**
 * Created by Raul on 25/10/2016.
 */

public class FilterViewGenerator {

    public CompoundButton[][] compoundButtons;
    FilterListener mFilterListener;
    AlertDialog dialog;

    public FilterViewGenerator(Context context, String title, final ServerFilter[] filters, int[][] selection, int color) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp10 = Math.round(10 / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        int dp20 = Math.round(20 / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        int sp15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, displayMetrics);
        int sp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, displayMetrics);

        LinearLayout rootLayout = new LinearLayout(context);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(rootParams);
        ScrollView scrollView = new ScrollView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(scrollParams);
        rootLayout.addView(scrollView);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);
        layout.setGravity(CENTER);
        layout.setLayoutParams(rootParams);
        scrollView.addView(layout);
        compoundButtons = new CompoundButton[filters.length][];
        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsGroups = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int i = 0;
        for (ServerFilter filter : filters) {
            TextView nTextView = new TextView(context);
            nTextView.setTextColor(Color.WHITE);
            nTextView.setBackgroundColor(color);
            nTextView.setText(filter.getTitle());
            nTextView.setLayoutParams(paramsText);
            nTextView.setPadding(dp20, dp10, dp10, dp20);
            layout.addView(nTextView);
            if (filter.getFilterType() == ServerFilter.FilterType.SINGLE) {
                RadioButton[] rb = new RadioButton[filter.getOptions().length];
                GridLayout gridLayout = new GridLayout(context);
                gridLayout.setLayoutParams(paramsGroups);
                compoundButtons[i] = new CompoundButton[filter.getOptions().length];
                CompoundGroup rGroup = new CompoundGroup(true);
                for (int j = 0; j < filter.getOptions().length; j++) {
                    rb[j] = new RadioButton(context);
                    rb[j].setTextSize(sp8);
                    rb[j].setText(filter.getOptions()[j]);
                    gridLayout.addView(rb[j]);
                    rGroup.add(rb[j]);
                    compoundButtons[i][j] = rb[j];
                }
                gridLayout.setColumnCount(2);
                layout.addView(gridLayout);
            } else {
                GridLayout gridLayout = new GridLayout(context);
                gridLayout.setColumnCount(2);
                gridLayout.setLayoutParams(paramsGroups);
                CheckBox[] cb = new CheckBox[filter.getOptions().length];
                compoundButtons[i] = new CompoundButton[filter.getOptions().length];
                for (int j = 0; j < filter.getOptions().length; j++) {
                    cb[j] = new CheckBox(context);
                    cb[j].setTextSize(sp8);
                    cb[j].setText(filter.getOptions()[j]);
                    gridLayout.addView(cb[j]);
                    compoundButtons[i][j] = cb[j];
                }
                layout.addView(gridLayout);
            }
            i++;
        }

        for (int l = 0; l < selection.length; l++) {
            for (int j = 0; j < selection[l].length; j++) {
                compoundButtons[l][selection[l][j]].setChecked(true);
            }
        }

        TextView nTextView = new TextView(context);
        nTextView.setTextColor(Color.WHITE);
        nTextView.setBackgroundColor(color);
        nTextView.setText(title);
        nTextView.setPadding(dp10, dp20, dp10, dp20);
        nTextView.setLayoutParams(paramsText);
        nTextView.setTextSize(sp15);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCustomTitle(nTextView);
        dialogBuilder.setView(rootLayout);
        dialogBuilder.setPositiveButton("Apply", null);
        dialogBuilder.setNeutralButton("Reset", null);
        dialogBuilder.setNegativeButton(context.getString(android.R.string.cancel), null);

        dialog = dialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button accept = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                accept.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mFilterListener != null) {
                            int[][] result = new int[filters.length][];
                            int j = 0;
                            for (CompoundButton[] cbs : compoundButtons) {
                                ArrayList<Integer> checkeds = new ArrayList<Integer>();
                                for (int k = 0; k < cbs.length; k++) {
                                    if (cbs[k].isChecked()) {
                                        checkeds.add(k);
                                    }
                                }
                                result[j] = new int[checkeds.size()];
                                for (int k = 0; k < checkeds.size(); k++) {
                                    result[j][k] = checkeds.get(k);
                                }
                                j++;
                            }
                            mFilterListener.applyFilter(result);
                        }
                        dialog.dismiss();
                    }
                });
                Button cancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                Button reset = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                reset.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        for (CompoundButton[] cbs : compoundButtons) {
                            for (CompoundButton cb : cbs) {
                                cb.setChecked(false);
                            }
                            if (cbs[0] instanceof RadioButton)
                                cbs[0].setChecked(true);
                        }
                    }
                });
            }
        });
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public void setFilterListener(FilterListener nFilterListener) {
        this.mFilterListener = nFilterListener;
    }

    public interface FilterListener {
        void applyFilter(int[][] selectedIndexes);
    }
}
