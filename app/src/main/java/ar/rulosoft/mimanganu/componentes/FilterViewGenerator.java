package ar.rulosoft.mimanganu.componentes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;

import com.buildware.widget.indeterm.IndeterminateCheckBox;
import com.buildware.widget.indeterm.IndeterminateRadioButton;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.adapters.CompoundAdapter;

import static android.view.Gravity.CENTER;
import static android.widget.LinearLayout.VERTICAL;


/**
 * Created by Raul on 25/10/2016.
 */

public class FilterViewGenerator {
    private CompoundButton[][] compoundButtons;
    private FilterListener mFilterListener;
    private AlertDialog dialog;

    public FilterViewGenerator(Context context, String title, final ServerFilter[] filters, int[][] selection, int color) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp10 = Math.round(10 / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        int dp20 = Math.round(20 / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

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
        layout.setGravity(CENTER);
        compoundButtons = new CompoundButton[filters.length][];
        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        GridView.LayoutParams paramsComp = new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT);

        int i = 0;
        for (ServerFilter filter : filters) {
            TextView nTextView = new TextView(context);
            nTextView.setBackgroundColor(color);
            nTextView.setText(filter.getTitle());
            nTextView.setTextColor(Color.WHITE);
            nTextView.setLayoutParams(paramsText);
            nTextView.setPadding(dp20, dp10, dp20, dp10);
            layout.addView(nTextView);
            UnScrolledGridView gridView = new UnScrolledGridView(context);
            CompoundAdapter compoundAdapter = new CompoundAdapter(context);
            if (filter.getFilterType() == ServerFilter.FilterType.SINGLE) {
                IndeterminateRadioButton[] rb = new IndeterminateRadioButton[filter.getOptions().length];
                compoundButtons[i] = new CompoundButton[filter.getOptions().length];
                CompoundGroup rGroup = new CompoundGroup(true);
                for (int j = 0; j < filter.getOptions().length; j++) {
                    rb[j] = new IndeterminateRadioButton(context);
                    if (Build.VERSION.SDK_INT < 23) {
                        rb[j].setTextAppearance(context, android.R.style.TextAppearance_Small);
                    } else {
                        rb[j].setTextAppearance(android.R.style.TextAppearance_Small);
                    }
                    rb[j].setText(filter.getOptions()[j]);
                    rb[j].setLayoutParams(paramsComp);
                    compoundAdapter.add(rb[j]);
                    rGroup.add(rb[j]);
                    compoundButtons[i][j] = rb[j];
                }
            } else if (filter.getFilterType() == ServerFilter.FilterType.MULTI_STATES) {
                CheckBox3[] cb = new CheckBox3[filter.getOptions().length];
                compoundButtons[i] = new CompoundButton[filter.getOptions().length];
                for (int j = 0; j < filter.getOptions().length; j++) {
                    cb[j] = new CheckBox3(context);
                    if (Build.VERSION.SDK_INT < 23) {
                        cb[j].setTextAppearance(context, android.R.style.TextAppearance_Small);
                    } else {
                        cb[j].setTextAppearance(android.R.style.TextAppearance_Small);
                    }
                    cb[j].setText(filter.getOptions()[j]);
                    cb[j].setLayoutParams(paramsComp);
                    compoundAdapter.add(cb[j]);
                    compoundButtons[i][j] = cb[j];
                }
            } else {
                IndeterminateCheckBox[] cb = new IndeterminateCheckBox[filter.getOptions().length];
                compoundButtons[i] = new CompoundButton[filter.getOptions().length];
                for (int j = 0; j < filter.getOptions().length; j++) {
                    cb[j] = new IndeterminateCheckBox(context);
                    if (Build.VERSION.SDK_INT < 23) {
                        cb[j].setTextAppearance(context, android.R.style.TextAppearance_Small);
                    } else {
                        cb[j].setTextAppearance(android.R.style.TextAppearance_Small);
                    }
                    cb[j].setText(filter.getOptions()[j]);
                    cb[j].setLayoutParams(paramsComp);
                    compoundAdapter.add(cb[j]);
                    compoundButtons[i][j] = cb[j];
                }
            }
            gridView.setAdapter(compoundAdapter);
            gridView.setNumColumns(GridView.AUTO_FIT);
            layout.addView(gridView);
            i++;
        }

        for (int l = 0; l < selection.length; l++) {
            if (filters[l].getFilterType() == ServerFilter.FilterType.MULTI_STATES) {
                for (int j = 0; j < selection[l].length; j++) {
                    ((CheckBox3) compoundButtons[l][j]).setState(selection[l][j]);
                }
            } else {
                for (int j = 0; j < selection[l].length; j++) {
                    compoundButtons[l][selection[l][j]].setChecked(true);
                }
            }
        }

        TextView nTextView = new TextView(context);
        if (Build.VERSION.SDK_INT < 23) {
            nTextView.setTextAppearance(context, android.R.style.TextAppearance_Large);
        } else {
            nTextView.setTextAppearance(android.R.style.TextAppearance_Large);
        }
        nTextView.setTextColor(Color.WHITE);
        nTextView.setBackgroundColor(color);
        nTextView.setText(title);
        nTextView.setPadding(dp10, dp20, dp10, dp20);
        nTextView.setLayoutParams(paramsText);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCustomTitle(nTextView);
        dialogBuilder.setView(rootLayout);
        dialogBuilder.setPositiveButton(R.string.apply, null);
        dialogBuilder.setNeutralButton(R.string.reset, null);
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
                                ArrayList<Integer> checked = new ArrayList<>();
                                if (filters[j].getFilterType() == ServerFilter.FilterType.MULTI_STATES) {
                                    for (int k = 0; k < cbs.length; k++) {
                                        checked.add(((CheckBox3) cbs[k]).getIntState());
                                    }
                                    result[j] = new int[checked.size()];
                                    for (int k = 0; k < checked.size(); k++) {
                                        result[j][k] = checked.get(k);
                                    }
                                } else {
                                    for (int k = 0; k < cbs.length; k++) {
                                        if (cbs[k].isChecked()) {
                                            checked.add(k);
                                        }
                                    }
                                    result[j] = new int[checked.size()];
                                    for (int k = 0; k < checked.size(); k++) {
                                        result[j][k] = checked.get(k);
                                    }
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
                            if (cbs[0] instanceof AppCompatRadioButton)
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
