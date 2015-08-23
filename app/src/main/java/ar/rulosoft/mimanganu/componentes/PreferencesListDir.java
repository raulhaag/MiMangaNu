package ar.rulosoft.mimanganu.componentes;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ar.rulosoft.custompref.ArrayAdapterDirectory;
import ar.rulosoft.mimanganu.R;

public class PreferencesListDir extends DialogPreference {

    Context context = getContext();
    String actual;
    ListView dirs;
    TextView dirs_path;

    public PreferencesListDir(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_select_directory);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getContext());
        actual = pm.getString(getKey(), Environment.getExternalStorageDirectory().getPath() + "/");
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        dirs = (ListView) view.findViewById(R.id.dirList);
        dirs_path = (TextView) view.findViewById(R.id.dirBreadcrumb);

        ArrayList<String> dirList = dirList(actual);
        ArrayAdapter<String> adap =
                new ArrayAdapterDirectory(context, R.layout.listitem_dir, dirList);
        dirs_path.setText(actual);
        dirs.setAdapter(adap);
        dirs.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String item = (String) dirs.getItemAtPosition(arg2);
                if (item != "..") {
                    actual += item + "/";
                } else {
                    actual = actual.substring(0, actual.lastIndexOf("/"));
                    actual = actual.substring(0, actual.lastIndexOf("/") + 1);
                }
                ArrayList<String> dirList = dirList(actual);
                ArrayAdapter<String> adap =
                        new ArrayAdapterDirectory(context, R.layout.listitem_dir, dirList);
                dirs_path.setText(actual);
                dirs.setAdapter(adap);
            }
        });
        super.onBindDialogView(view);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {

        builder.setPositiveButton(getPositiveButtonText(), new OnClickListener() {
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
                        editor.putString(getKey(), actual);
                        editor.commit();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.unwritable_folder), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton(getNegativeButtonText(), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        super.onPrepareDialogBuilder(builder);
    }

    public ArrayList<String> dirList(String directorio) {
        ArrayList<String> list = new ArrayList<>();
        if (directorio.length() != 1) {
            list.add("..");
        }
        File dir = new File(directorio);
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (child.isDirectory()) {
                    list.add(child.getName());
                }
            }
        }
        return list;
    }

}
