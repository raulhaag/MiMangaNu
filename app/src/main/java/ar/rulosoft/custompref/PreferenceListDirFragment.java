package ar.rulosoft.custompref;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 15/04/2016.
 */
public class PreferenceListDirFragment extends PreferenceDialogFragmentCompat {
    private Context context = getContext();
    private String actual;
    private ListView dirs;
    private TextView dirs_path;
    PreferencesListDir parent;

    public static PreferenceListDirFragment newInstance(Preference preference) {
        PreferenceListDirFragment fragment = new PreferenceListDirFragment();
        fragment.setParent((PreferencesListDir) preference);
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getContext());
        actual = pm.getString(parent.getKey(), Environment.getExternalStorageDirectory().getPath() + "/");
        dirs = (ListView) view.findViewById(R.id.dirList);
        dirs_path = (TextView) view.findViewById(R.id.dirBreadcrumb);
        ArrayList<String> dirList = Util.dirList(actual);
        context = getActivity();
        ArrayAdapter<String> adap = new ArrayAdapterDirectory(context, R.layout.listitem_dir, dirList);
        dirs_path.setText(actual);
        dirs.setAdapter(adap);
        dirs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String item = (String) dirs.getItemAtPosition(arg2);
                if (item != "..") {
                    actual += item + "/";
                } else {
                    actual = actual.substring(0, actual.lastIndexOf("/"));
                    actual = actual.substring(0, actual.lastIndexOf("/") + 1);
                }
                ArrayList<String> dirList = Util.dirList(actual);
                ArrayAdapter<String> adap =
                        new ArrayAdapterDirectory(context, R.layout.listitem_dir, dirList);
                dirs_path.setText(actual);
                dirs.setAdapter(adap);
            }
        });
        super.onBindDialogView(view);
    }

    @Override
    protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
        builder.setPositiveButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File nd;
                if (actual.contains("MiManga")) {
                    nd = new File(actual, ".nomedia");
                } else {
                    nd = new File(actual + "MiMangaNu/", ".nomedia");
                }
                nd.mkdirs();
                try {
                    nd.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (nd.exists()) {
                        Toast.makeText(context, context.getResources().getString(R.string.folder_changed), Toast.LENGTH_SHORT).show();
                        //TODO moveFiles to new folder
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(parent.getKey(), actual);
                        editor.apply();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.unwritable_folder), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        super.onPrepareDialogBuilder(builder);
    }

    public PreferencesListDir getParent() {
        return parent;
    }

    public void setParent(PreferencesListDir parent) {
        this.parent = parent;
    }

    @Override
    public void onDialogClosed(boolean b) {

    }
}
