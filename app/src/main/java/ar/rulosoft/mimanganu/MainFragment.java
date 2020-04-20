package ar.rulosoft.mimanganu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ar.rulosoft.mimanganu.adapters.MangasRecAdapter;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.Shortcuts;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.AutomaticUpdateTask;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.AppUpdateUtil;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReceiver;
import ar.rulosoft.mimanganu.utils.Paths;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

import static ar.rulosoft.mimanganu.utils.Util.bytesToHex;

/**
 * Created by Raul
 */

public class MainFragment extends Fragment implements MangasRecAdapter.OnMangaClick, View.OnLongClickListener, View.OnClickListener, MainActivity.OnBackListener {

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    public static final String SELECT_MODE = "selector_modo";
    public static final String LAST_CHECK = "last_check_update";
    public static final int MODE_SHOW_ALL = 0;
    public static final int MODE_HIDE_READ = 1;
    private static final String TAG = "MainFragment";
    public static int mNotifyID = 1246502;
    private SharedPreferences pm;
    private FloatingActionButton floatingActionButton_add;
    private RecyclerView recyclerView;
    private MangasRecAdapter mMAdapter;
    private SwipeRefreshLayout swipeReLayout;
    private UpdateListTask updateListTask = null;
    private int mNotifyID_AddAllMangaInDirectory = (int) System.currentTimeMillis();
    private int lastContextMenuIndex;
    public static String currentVault = "";

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        MainActivity.colors = ThemeColors.getColors(pm);
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        final CoordinatorLayout cl = viewGroup.findViewById(R.id.coordinator_layout);
        recyclerView = viewGroup.findViewById(R.id.recicleview_mis_mangas);
        swipeReLayout = viewGroup.findViewById(R.id.str);
        swipeReLayout.setColorSchemeColors(MainActivity.colors[0], MainActivity.colors[1]);
        swipeReLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateListTask = new UpdateListTask(getActivity(), cl, pm, currentVault);
                updateListTask.execute();
            }
        });
        int columnSize = Integer.parseInt(pm.getString("grid_columns", "-1"));
        if (columnSize == -1 || pm.getBoolean("grid_columns_automatic_detection", true))
            columnSize = Util.getInstance().getGridColumnSizeFromWidth(getActivity());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnSize));
        if (updateListTask != null && updateListTask.getStatus() == AsyncTask.Status.RUNNING) {
            swipeReLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeReLayout.setRefreshing(true);
                }
            });
        }
        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null) {
            floatingActionButton_add = getView().findViewById(R.id.floatingActionButton_add);
            floatingActionButton_add.setOnClickListener(this);
            floatingActionButton_add.setOnLongClickListener(this);
        }
    }

    @Override
    public void onPause() {
        if (swipeReLayout != null)
            swipeReLayout.clearAnimation();
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        activity.setColorToBars();
        if (MainActivity.darkTheme != pm.getBoolean("dark_theme", false)) {
            Util.getInstance().restartApp(getContext());
        }
        activity.enableHomeButton(false);
        activity.setTitle(getString(R.string.app_name));
        setListManga(true);
        floatingActionButton_add.setBackgroundTintList(ColorStateList.valueOf(MainActivity.colors[1]));
        if (MainActivity.coldStart) {
            // Manga Updates
            long updateInterval = Long.parseLong(pm.getString("update_interval", "0"));
            if (updateInterval < 0) {
                updateAtStartUp(updateInterval);
            }
            //AppUpdateUtil.checkAppUpdates(getContext()); just or tests
            // App Update
            if (pm.getBoolean("app_update", true) && BuildConfig.VERSION_NAME.contains("github")) {
                long last_check = pm.getLong("last_app_update", 0);
                long diff = System.currentTimeMillis() - last_check;
                //Log.i("MF", "diff: " + diff);
                AppUpdateUtil.checkAppUpdates(getContext());
                if (diff > 43200000) {
                    pm.edit().putLong("last_app_update", System.currentTimeMillis()).apply();
                    AppUpdateUtil.checkAppUpdates(getContext());
                }
            }
            MainActivity.coldStart = false;
        }
    }

    private void updateAtStartUp(long updateInterval) {
        if (updateInterval == -2) {
            updateInterval = 21600000; //180000
        } else if (updateInterval == -3) {
            updateInterval = 43200000;
        } else if (updateInterval == -4) {
            updateInterval = 86400000;
        }

        if (updateInterval < 0) { // update at start up (with no time)
            startUpdate();
        } else { // update at start up (with specific time)
            long last_check = pm.getLong(LAST_CHECK, 0);
            long diff = System.currentTimeMillis() - last_check;
            Log.i("MF", "diff: " + diff);
            if (diff > updateInterval) {
                pm.edit().putLong(LAST_CHECK, System.currentTimeMillis()).apply();
                startUpdate();
            }
        }
    }

    private void startUpdate() {
        try {
            if (NetworkUtilsAndReceiver.isConnected(getContext())) {
                updateListTask = new UpdateListTask(getActivity(), getView(), pm, currentVault);
                updateListTask.execute();
            }
        } catch (Exception e) {
            Util.getInstance().toast(getContext(), getString(R.string.no_internet_connection));
            Log.e("MF", "Exception", e);
        }
    }

    //@Override
    public void onClick(View v) {
        ((MainActivity) getActivity()).replaceFragment(ServersSelectFragment.newInstance(),
                "SERVER_SELECT", R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_left);
    }


    @Override
    public boolean onLongClick(View v) {
        openVault();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_mismangas, menu);
        final Menu mMenu = menu;

        /* Local Search */
        MenuItem search = menu.findItem(R.id.action_search_local);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);

        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenu.findItem(R.id.action_view_download).setVisible(false);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setListManga(false);
                mMenu.findItem(R.id.action_view_download).setVisible(true);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String value) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String value) {
                if (!value.isEmpty()) {
                    ArrayList<Manga> mangaList;
                    if (value.contains("'"))
                        value = value.replaceAll("'", "''");
                    try {
                        mangaList = Database.getMangasCondition(getActivity(), " ((nombre LIKE '%" + value + "%') AND (vault = '" + currentVault + "')) ", null, false);
                        mMAdapter = new MangasRecAdapter(mangaList, getContext(), MainFragment.this);
                        mMAdapter.setMangaClickListener(MainFragment.this);
                        recyclerView.setAdapter(mMAdapter);
                    } catch (Exception e) {
                        Log.e("MF", "Exception", e);
                        //  ACRA.getErrorReporter().handleException(e);
                    }
                }
                return false;
            }
        });

        MenuItem vmi = menu.findItem(R.id.open_vault);
        if (!currentVault.isEmpty()) {
            vmi.setTitle(R.string.close_vault);
        }

        /* Set hide/unhide checkbox */
        boolean checkedRead = pm.getInt(SELECT_MODE, MODE_SHOW_ALL) > 0;
        menu.findItem(R.id.action_hide_read).setChecked(checkedRead);

        /* Set sort mode */
        int[] sortList = {
                R.id.sort_last_read, R.id.sort_last_read_desc,
                R.id.sort_name, R.id.sort_name_desc,
                R.id.sort_author, R.id.sort_author_desc,
                R.id.sort_finished, R.id.sort_finished_asc,
                R.id.sort_as_added_to_db_asc, R.id.sort_as_added_to_db_desc,
                R.id.sort_last_updated,

        };
        menu.findItem(sortList[pm.getInt("manga_view_sort_by", 0)]).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_download: {
                ((MainActivity) getActivity()).replaceFragment(new DownloadsFragment(), "DownloadFragment");
                break;
            }
            case R.id.action_hide_read: {
                item.setChecked(!item.isChecked());
                pm.edit().putInt(SELECT_MODE, item.isChecked() ? MODE_HIDE_READ : MODE_SHOW_ALL).apply();
                setListManga(true);
                break;
            }
            case R.id.action_settings: {
                ((MainActivity) getActivity()).replaceFragment(new PreferencesFragment(), "PreferencesFragment");
                break;
            }
            case R.id.contact: {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/raulhaag/MiMangaNu/issues"));
                startActivity(i);
                break;
            }
            case R.id.restart: {
                Util.getInstance().restartApp(Objects.requireNonNull(getActivity()));
                break;
            }
            case R.id.sort_last_read: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 0).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_last_read_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 1).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_name: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 2).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_name_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 3).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_author: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 4).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_author_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 5).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_finished: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 6).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_finished_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 7).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_as_added_to_db_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 8).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_as_added_to_db_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 9).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_last_updated: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 10).apply();
                setListManga(true);
                break;
            }

            case R.id.open_vault: {
                openVault();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void openVault() {
        if (currentVault.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.open_vault);
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String p = input.getText().toString();
                    if (!p.isEmpty()) {
                        try {
                            /*  item.setTitle(R.string.close_vault);/*/
                            currentVault = bytesToHex(MessageDigest.getInstance("SHA-256").digest(p.getBytes()));
                            setListManga(true);
                            getActivity().invalidateOptionsMenu();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Util.getInstance().toast(getContext(), getString(R.string.empty_string));
                    }
                }
            });
            builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            currentVault = "";
            setListManga(true);
            getActivity().invalidateOptionsMenu();
        }
    }

    public void setListManga(boolean force) {
        if (recyclerView != null) {
            ArrayList<Manga> mangaList = new ArrayList<>();
            /*
             * sort_val: 0,1 = last_read (default), 2,3 = title, 4,5 = author
             *                  all odd numbers are asc, even numbers are desc
             *
             * feel free to add more sort type */
            int sort_val = PreferenceManager.getDefaultSharedPreferences(
                    getContext()).getInt("manga_view_sort_by", 0);

            String sort_by;
            boolean sort_ord = sort_val % 2 == 0;
            switch (sort_val) {
                case 2:
                case 3:
                    sort_by = Database.COL_NAME;
                    break;
                case 4:
                case 5:
                    sort_by = Database.COL_AUTHOR;
                    break;
                case 6:
                case 7:
                    sort_by = Database.COL_IS_FINISHED;
                    sort_ord = !sort_ord;
                    break;
                case 8:
                case 9:
                    sort_by = Database.COL_ID;
                    sort_ord = !sort_ord;
                    break;
                case 10:
                    sort_by = Database.COL_LAST_UPDATE_LONG;
                    sort_ord = false;
                    break;
                //case 0:
                //case 1:
                default:
                    sort_by = Database.COL_LAST_READ;
                    sort_ord = !sort_ord;
            }
            int value = PreferenceManager.getDefaultSharedPreferences(
                    getContext()).getInt(SELECT_MODE, MODE_SHOW_ALL);
            String showUnreadOnly = "";
            if (value == MODE_HIDE_READ) {
                showUnreadOnly = "AND id IN (SELECT manga_id FROM capitulos WHERE estado != 1 GROUP BY manga_id)";
            }

            mangaList = Database.getMangasCondition(getContext(), " vault = '" + currentVault + "'" + showUnreadOnly, sort_by, sort_ord);

            if (mMAdapter == null || sort_val < 2 || mangaList.size() > mMAdapter.getItemCount() || force) {
                mMAdapter = new MangasRecAdapter(mangaList, getContext(), MainFragment.this);
                mMAdapter.setMangaClickListener(MainFragment.this);
                recyclerView.setAdapter(mMAdapter);
            }
        } else {
            Log.i(TAG, "rec_view was null");
        }
    }

    @Override
    public void onMangaClick(Manga manga) {
        Bundle bundle = new Bundle();
        bundle.putInt(MainFragment.MANGA_ID, manga.getId());
        MangaFragment mangaFragment = new MangaFragment();
        mangaFragment.setArguments(bundle);
        //In rare cases State loss occurs
        ((MainActivity) getActivity()).replaceFragmentAllowStateLoss(mangaFragment, "MangaFragment");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.gridview_mismangas, menu);
        MenuItem m = menu.findItem(R.id.noupdate);
        Manga manga = mMAdapter.getItem((int) v.getTag());
        if (manga.isFinished()) {
            m.setTitle(getActivity().getResources().getString(R.string.buscarupdates));
        } else {
            m.setTitle(getActivity().getResources().getString(R.string.nobuscarupdate));
        }
        if (!manga.getVault().isEmpty()) {
            MenuItem vmi = menu.findItem(R.id.add_to_vault);
            vmi.setTitle(R.string.rem_vault);
        }
        lastContextMenuIndex = (int) v.getTag();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Manga manga = ((MangasRecAdapter) recyclerView.getAdapter()).getItem(lastContextMenuIndex);
        if (item.getItemId() == R.id.delete) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.manga_delete_confirm, manga.getTitle()))
                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DownloadPoolService.forceStop(manga.getId());
                            ServerBase serverBase = ServerBase.getServer(manga.getServerId(), getContext());
                            String path = Paths.generateBasePath(serverBase, manga, getActivity());
                            Util.getInstance().deleteRecursive(new File(path));
                            Database.deleteManga(getActivity(), manga.getId());
                            mMAdapter.remove(manga);
                            Util.getInstance().showFastSnackBar(getResources().getString(R.string.deleted, manga.getTitle()), getView(), getContext());
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else if (item.getItemId() == R.id.noupdate) {
            if (manga.isFinished()) {
                manga.setFinished(false);
                Database.setUpgradable(getActivity(), manga.getId(), false);
            } else {
                manga.setFinished(true);
                Database.setUpgradable(getActivity(), manga.getId(), true);
            }
        } else if (item.getItemId() == R.id.download_all_chapters) {
            ArrayList<Chapter> chapters = Database.getChapters(getActivity(), manga.getId(), Database.COL_CAP_DOWNLOADED + " != 1", true);
            for (Chapter chapter : chapters) {
                try {
                    DownloadPoolService.addChapterDownloadPool(getActivity(), chapter, false);
                } catch (Exception e) {
                    Log.e(TAG, "Download add pool error", e);
                }
            }
            if (chapters.size() > 1)
                Util.getInstance().showFastSnackBar(getString(R.string.downloading) + " " + chapters.size() + " " + getString(R.string.chapters), getView(), getContext());
            else
                Util.getInstance().showFastSnackBar(getString(R.string.downloading) + " " + chapters.size() + " " + getString(R.string.chapter), getView(), getContext());
        } else if (item.getItemId() == R.id.add_to_vault) {
            if (manga.getVault().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getContext().getString(R.string.set_vault));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String p = input.getText().toString();
                        if (!p.isEmpty()) {
                            try {
                                p = bytesToHex(MessageDigest.getInstance("SHA-256").digest(p.getBytes()));
                                manga.setVault(p);
                                Database.updateManga(getContext(), manga, false);
                                setListManga(true);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Util.getInstance().toast(getContext(), getString(R.string.empty_string));
                        }
                    }
                });
                builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                manga.setVault("");
                Database.updateManga(getContext(), manga, false);
                setListManga(true);
            }
        } else if (item.getItemId() == R.id.create_shortcut) {
            Shortcuts.addShortCutsX(manga, getContext());
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        if (currentVault.isEmpty()) {
            return false;
        } else {
            currentVault = "";
            setListManga(true);
            getActivity().invalidateOptionsMenu();
            return true;
        }
    }


    class UpdateListTask extends AutomaticUpdateTask {
        private Context context;

        UpdateListTask(Context context, View view, SharedPreferences pm, String vault) {
            super(context, view, pm, vault);
            this.context = context;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (context != null) {
                if (result > 0) {
                    Util.getInstance().cancelNotification(mNotifyID);
                    if (isAdded())
                        setListManga(true);
                    Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.mgs_update_found, "" + result), getView(), context);
                } else {
                    Util.getInstance().cancelNotification(mNotifyID);
                    if (!error.isEmpty()) {
                        Util.getInstance().toast(getContext(), error);
                    } else {
                        Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.no_new_updates_found), getView(), context);
                    }
                }
                swipeReLayout.setRefreshing(false);
            } else {
                Util.getInstance().cancelNotification(mNotifyID);
            }

            if (pm.getBoolean("auto_import", false)) {
                String autoImportPath = pm.getString("auto_import_path", "-1");
                Log.d("MF", "auto: " + autoImportPath);
                if (!autoImportPath.equals("-1"))
                    new AddAllMangaInDirectoryTask().execute(autoImportPath);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            swipeReLayout.setRefreshing(false);
            MainActivity.isCancelled = false;
        }
    }

    public class AddAllMangaInDirectoryTask extends AsyncTask<String, Integer, Void> {
        String error = "";
        int max = 0;
        ServerBase serverBase = ServerBase.getServer(ServerBase.FROMFOLDER, getContext());
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
                    Log.i("MainFragment", "FromFolder directory: " + directory);
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
                        Log.i("MainFragment", "already on db: " + directory);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isAdded()) {
                Util.getInstance().cancelNotification(mNotifyID_AddAllMangaInDirectory);
                Toast.makeText(getActivity(), getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
                if (!error.isEmpty()) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
                setListManga(true);
            }
            super.onPostExecute(result);
        }
    }

}

