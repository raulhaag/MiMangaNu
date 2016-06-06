package ar.rulosoft.mimanganu.componentes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.custompref.ArrayAdapterDirectory;
import ar.rulosoft.mimanganu.MainFragment;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 14/05/2016
 */
public class MangaFolderSelect extends DialogFragment {
    private Context context = getActivity();
    private String actual;
    private ListView dirs;
    private TextView dirs_path;
    private MainFragment mainFragment;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = Util.getLastStringInPath(actual);
                List<Manga> mangas = Database.getMangas(getContext(), null, true);
                boolean onDb = false;
                for (Manga m : mangas) {
                    if (m.getPath().contains(actual))
                        onDb = true;
                }
                if (!onDb) {
                    Manga manga = new Manga(FromFolder.FROMFOLDER, title, actual, true);
                    manga.setImages("");
                    (new AddMangaTask()).execute(manga);
                }else{
                    Toast.makeText(getContext(),getContext().getString(R.string.dir_already_on_db),Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(getActivity().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        LayoutInflater i = getActivity().getLayoutInflater();

        View view = i.inflate(R.layout.dialog_select_directory, null);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        actual = pm.getString("directorio", Environment.getExternalStorageDirectory().getPath() + "/MiMangaNu/");
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
                if (!item.equals("..")) {
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
        builder.setView(view);
        return builder.create();
    }

    public void setMainFragment(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    public class AddMangaTask extends AsyncTask<Manga, Integer, Void> {
        ProgressDialog adding = new ProgressDialog(getActivity());
        String error = ".";
        int total = 0;
        ServerBase serverBase = ServerBase.getServer(ServerBase.FROMFOLDER);

        @Override
        protected void onPreExecute() {
            adding.setMessage(getResources().getString(R.string.adding_to_db));
            adding.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Manga... params) {
            try {
                serverBase.loadChapters(params[0], false);
            } catch (Exception e) {
                error = e.getMessage();
            }
            total = params[0].getChapters().size();
            int mid = Database.addManga(getActivity(), params[0]);
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < params[0].getChapters().size(); i++) {
                if (System.currentTimeMillis() - initTime > 500) {
                    publishProgress(i);
                    initTime = System.currentTimeMillis();
                }
                Database.addChapter(getActivity(), params[0].getChapter(i), mid);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adding != null) {
                            adding.setMessage(getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + total);
                        }
                    }
                });
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            adding.dismiss();
            if (isAdded()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
                if (error != null && error.length() > 2) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
                if (mainFragment != null)
                    mainFragment.setListManga(true);
                getActivity().onBackPressed();
            }
            super.onPostExecute(result);
        }
    }
}
