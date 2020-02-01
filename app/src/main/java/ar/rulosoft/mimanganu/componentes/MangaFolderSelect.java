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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.custompref.ArrayAdapterDirectory;
import ar.rulosoft.mimanganu.MainActivity;
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
    private int mNotifyID_AddAllMangaInDirectory = (int) System.currentTimeMillis();
    private AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(getActivity().getString(android.R.string.ok), null);
        builder.setNegativeButton(getActivity().getString(android.R.string.cancel), null);

        LayoutInflater i = getActivity().getLayoutInflater();

        View view = i.inflate(R.layout.dialog_select_directory, null);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        actual = pm.getString("directorio", Environment.getExternalStorageDirectory().getPath() + "/MiMangaNu/");
        dirs = view.findViewById(R.id.dirList);
        dirs_path = view.findViewById(R.id.dirBreadcrumb);
        ArrayList<String> dirList = Util.getInstance().dirList(actual);
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
                ArrayList<String> dirList = Util.getInstance().dirList(actual);
                ArrayAdapter<String> adap =
                        new ArrayAdapterDirectory(context, R.layout.listitem_dir, dirList);
                dirs_path.setText(actual);
                dirs.setAdapter(adap);
            }
        });
        builder.setView(view);
        dialog = builder.create();

        // override the onClick action for the 'ok' button to keep the dialog open (the default action
        // is to dismiss() the dialog on positive or negative button press automatically)
        // if it would be closed directly, the activity would be detached during the time the tasks
        // are running (i.e. getActivity() called at that time would return null)
        // so let the tasks themselves dismiss() the dialog in their onPostExecute() callbacks
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MainActivity.pm != null) {
                            if (MainActivity.pm.getBoolean("multi_import", false)) {
                                new AddAllMangaInDirectoryTask().execute(actual);
                            } else {
                                new AddMangaTask().execute(actual);
                            }
                        } else {
                            new AddMangaTask().execute(actual);
                        }
                    }
                });
            }
        });

        return dialog;
    }

    public class AddMangaTask extends AsyncTask<String, Integer, Void> {
        ProgressDialog adding = new ProgressDialog(getActivity());
        String error = "";
        int max = 0;
        Manga manga;
        boolean onDb;

        @Override
        protected void onPreExecute() {
            adding.setMessage(getResources().getString(R.string.adding_to_db));
            adding.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            ServerBase serverBase = ServerBase.getServer(ServerBase.FROMFOLDER, context);
            String title = Util.getInstance().getLastStringInPath(params[0]);
            List<Manga> mangas = Database.getFromFolderMangas(getContext());
            onDb = false;
            for (Manga m : mangas) {
                if (m.getPath().equals(params[0]))
                    onDb = true;
            }
            if (!onDb) {
                manga = new Manga(FromFolder.FROMFOLDER, title, params[0], true);
                manga.setImages("");
                try {
                    serverBase.loadChapters(manga, false);
                } catch (Exception e) {
                    Log.e("MangaFolderSelect", "Exception", e);
                    error = Log.getStackTraceString(e);
                }
                max = manga.getChapters().size();
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
                Log.i("MangaFolderSelect", "already on db: " + params[0]);
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adding != null) {
                        adding.setMessage(getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + max);
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(Void result) {
            adding.dismiss();

            if (!onDb)
                Toast.makeText(getActivity(), getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), getContext().getString(R.string.dir_already_on_db), Toast.LENGTH_LONG).show();
            if (!error.isEmpty()) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }

            dialog.dismiss();
            getActivity().onBackPressed();

            super.onPostExecute(result);
        }
    }

    public class AddAllMangaInDirectoryTask extends AsyncTask<String, Integer, Void> {
        String error = "";
        int max = 0;
        ServerBase serverBase = ServerBase.getServer(ServerBase.FROMFOLDER, context);
        Manga manga;

        @Override
        protected void onPreExecute() {
            mNotifyID_AddAllMangaInDirectory = (int) System.currentTimeMillis();
            Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_AddAllMangaInDirectory, getResources().getString(R.string.adding_folders_as_mangas), "");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            Util.getInstance().changeNotificationWithProgressbar(max, values[0], mNotifyID_AddAllMangaInDirectory, getResources().getString(R.string.adding_folders_as_mangas), "" + values[0] + " / " + max, true);
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(String... params) {
            String directory = params[0];
            File f = new File(directory);
            if (f.listFiles().length > 0) {
                max = f.listFiles().length;
                int n = 0;
                for (File child : f.listFiles()) {
                    n++;
                    publishProgress(n);
                    directory = child.getAbsolutePath();
                    List<Manga> fromFolderMangas = Database.getFromFolderMangas(getContext());
                    Log.i("MangaFolderSelect", "FromFolder directory: " + directory);
                    boolean onDb = false;
                    for (Manga m : fromFolderMangas) {
                        if (m.getPath().equals(directory))
                            onDb = true;
                    }
                    if (!onDb) {
                        String title = Util.getInstance().getLastStringInPathDontRemoveLastChar(directory);
                        manga = new Manga(FromFolder.FROMFOLDER, title, directory, true);
                        manga.setImages("");

                        try {
                            serverBase.loadChapters(manga, false);
                        } catch (Exception e) {
                            Log.e("MangaFolderSelect", "Exception", e);
                            error = Log.getStackTraceString(e);
                        }
                        int mid = Database.addManga(getActivity(), manga);
                        for (int i = 0; i < manga.getChapters().size(); i++) {
                            Database.addChapter(getActivity(), manga.getChapter(i), mid);
                        }
                    } else {
                        Log.i("MangaFolderSelect", "already on db: " + directory);
                    }

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Util.getInstance().cancelNotification(mNotifyID_AddAllMangaInDirectory);
            Toast.makeText(getActivity(), getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
            if (!error.isEmpty()) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
            super.onPostExecute(result);
        }
    }

}
