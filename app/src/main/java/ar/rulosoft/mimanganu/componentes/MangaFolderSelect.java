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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.custompref.ArrayAdapterDirectory;
import ar.rulosoft.mimanganu.MainActivity;
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

                if(MainActivity.pm != null){
                    if (MainActivity.pm.getBoolean("multi_import", false)) {
                        new AddAllMangaInDirectoryTask().execute(actual);
                    } else {
                        String title = Util.getLastStringInPath(actual);
                        List<Manga> mangas = Database.getFromFolderMangas(getContext());
                        Log.d("MFS", "ac: " + actual);
                        boolean onDb = false;
                        for (Manga m : mangas) {
                            if (m.getPath().equals(actual))
                                onDb = true;
                        }
                        if (!onDb) {
                            Manga manga = new Manga(FromFolder.FROMFOLDER, title, actual, true);
                            manga.setImages("");
                            (new AddMangaTask()).execute(manga);
                        } else {
                            Toast.makeText(getContext(), getContext().getString(R.string.dir_already_on_db), Toast.LENGTH_LONG).show();
                        }
                    }
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
        String error = "";
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
                Log.e("MangaFolderSelect", "Exception", e);
                error = Log.getStackTraceString(e);
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
                if (!error.isEmpty()) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
                if (mainFragment != null)
                    mainFragment.setListManga(true);
                getActivity().onBackPressed();
            }
            super.onPostExecute(result);
        }
    }


    public class AddAllMangaInDirectoryTask extends AsyncTask<String, Integer, Void> { //Manga
        String error = "";
        int total = 0;
        ServerBase serverBase = ServerBase.getServer(ServerBase.FROMFOLDER);
        Manga manga;

        @Override
        protected void onPreExecute() {
            /*adding.setMessage(getResources().getString(R.string.adding_to_db));
            adding.show();*/
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) { // Manga
            String directory = params[0];//"/storage/emulated/0/MiMangaNu/asd/"; // "/storage/emulated/0/MiMangaNu/asd/"
            File f = new File(directory);

            if (f.listFiles().length > 0) {
                total = f.listFiles().length;
                int n = 0;
                for (File child : f.listFiles()) {
                    n++;
                    publishProgress(n);
                    Log.d("MF", "c: " + child.getAbsolutePath());
                    directory = child.getAbsolutePath();

                    List<Manga> fromFolderMangas = Database.getFromFolderMangas(getContext());
                    Log.d("MF", "dir: " + directory);
                    boolean onDb = false;
                    for (Manga m : fromFolderMangas) {
                        if (m.getPath().equals(directory))
                            onDb = true;
                    }
                    if (!onDb) {
                        String title = Util.getLastStringInPathDontRemoveLastChar(directory);
                        manga = new Manga(FromFolder.FROMFOLDER, title, directory, true);
                        manga.setImages("");

                        try {
                            serverBase.loadChapters(manga, false);
                        } catch (Exception e) {
                            Log.e("MangaFolderSelect", "Exception", e);
                            error = Log.getStackTraceString(e);
                        }
                        //total = manga.getChapters().size();
                        int mid = Database.addManga(getActivity(), manga);
                        long initTime = System.currentTimeMillis();
                        for (int i = 0; i < manga.getChapters().size(); i++) {
                            if (System.currentTimeMillis() - initTime > 500) {
                                publishProgress(i);
                                initTime = System.currentTimeMillis();
                            }
                            Database.addChapter(getActivity(), manga.getChapter(i), mid);
                        }
                    } else {
                        Log.i("MF", "already on db: " + directory);
                    }

                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            /*if (isAdded()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adding != null) {
                            adding.setMessage(getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + total);
                        }
                    }
                });
            }*/
        }

        @Override
        protected void onPostExecute(Void result) {
            /*try {
                adding.dismiss();
            } catch (Exception e) {
                Log.e("MangaFolderSelect", "Exception", e);
            }*/
            if (isAdded()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
                if (!error.isEmpty()) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
                if (mainFragment != null)
                    mainFragment.setListManga(true);
                //getActivity().onBackPressed();
            }
            super.onPostExecute(result);
        }
    }


}
