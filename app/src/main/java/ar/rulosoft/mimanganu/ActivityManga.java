package ar.rulosoft.mimanganu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fedorvlasov.lazylist.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;

import ar.rulosoft.mimanganu.adapters.ChapterAdapter;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.ControlInfoNoScroll;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.FragmentUpdateSearchTask;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityManga extends AppCompatActivity {
    public static final String DIRECTION = "direcciondelectura";
    public static final String CHAPTER_ID = "cap_id";
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public Manga mManga;
    private Direction mDirection;

    private ChapterAdapter mChapterAdapter;
    private SharedPreferences pm;
    private ImageLoader mImageLoader;
    private ListView mListView;
    private MenuItem mMenuItem;
    private int mMangaId;
    private boolean darkTheme;
    private Menu menu;

    private FragmentUpdateSearchTask mUpdateSearchTask;
    private ControlInfoNoScroll mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga);
        mMangaId = getIntent().getExtras().getInt(ActivityMisMangas.MANGA_ID, -1);
        if (mMangaId == -1) {
            onBackPressed();
            finish();
        }
        mListView = (ListView) findViewById(R.id.lista);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.str);
        mImageLoader = new ImageLoader(this);
        int[] colors = ThemeColors.getColors(pm, getApplicationContext());
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
        mSwipeRefreshLayout.setColorSchemeColors(colors[0], colors[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        if (savedInstanceState == null) {
            mUpdateSearchTask = new FragmentUpdateSearchTask();
            getSupportFragmentManager().beginTransaction()
                    .add(mUpdateSearchTask, "BUSCAR_NUEVOS").commit();
        } else {
            mUpdateSearchTask = (FragmentUpdateSearchTask) getSupportFragmentManager()
                    .findFragmentByTag("BUSCAR_NUEVOS");
            if (mUpdateSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUpdateSearchTask.iniciaTarea(mManga, ActivityManga.this);
            }
        });
        mListView.setDivider(new ColorDrawable(colors[0]));
        mListView.setDividerHeight(1);
        mInfo = new ControlInfoNoScroll(ActivityManga.this);
        mListView.addHeaderView(mInfo);
        mInfo.setColor(darkTheme, colors[0]);
        ChapterAdapter.setColor(darkTheme, colors[1], colors[0]);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chapter c = (Chapter) mListView.getAdapter().getItem(position);
                new GetPagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, c);
            }
        });
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
                mChapterAdapter.clearSelection();
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.listitem_capitulo_menu_cab, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {

                SparseBooleanArray selection = mChapterAdapter.getSelection();
                ServerBase s = ServerBase.getServer(mManga.getServerId());

                switch (item.getItemId()) {
                    case R.id.seleccionar_todo:
                        mChapterAdapter.selectAll();
                        return true;
                    case R.id.seleccionar_nada:
                        mChapterAdapter.clearSelection();
                        return true;
                    case R.id.select_from:
                        mChapterAdapter.selectFrom(selection.keyAt(0));
                        return true;
                    case R.id.select_to:
                        mChapterAdapter.selectTo(selection.keyAt(0));
                        return true;
                    case R.id.download_selection:
                        Chapter[] chapters = mChapterAdapter.getSelectedChapters();
                        for (Chapter c : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(ActivityManga.this, c, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case R.id.mar_and_di:
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter c = mChapterAdapter.getItem(selection.keyAt(i));
                            c.markRead(ActivityManga.this, true);
                            c.freeSpace(ActivityManga.this, mManga, s);
                        }
                        break;
                    case R.id.borrar_imagenes:
                        for (int i = 0; i < selection.size(); i++) {
                            Chapter c = mChapterAdapter.getItem(selection.keyAt(i));
                            c.freeSpace(ActivityManga.this, mManga, s);
                        }
                        break;
                    case R.id.borrar:
                        int[] selected = new int[selection.size()];
                        for (int j = 0; j < selection.size(); j++) {
                            selected[j] = selection.keyAt(j);
                        }
                        Arrays.sort(selected);
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter c = mChapterAdapter.getItem(selection.keyAt(i));
                            c.delete(ActivityManga.this, mManga, s);
                            mChapterAdapter.remove(c);
                        }
                        break;
                    case R.id.reset:
                        for (int i = 0; i < selection.size(); i++) {
                            Chapter c = mChapterAdapter.getItem(selection.keyAt(i));
                            c.reset(ActivityManga.this, mManga, s);
                        }
                        break;
                    case R.id.mark_as_read:
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter c = mChapterAdapter.getItem(selection.keyAt(i));
                            c.markRead(ActivityManga.this, true);
                        }
                        break;
                    case R.id.mark_unread:
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter c = mChapterAdapter.getItem(selection.keyAt(i));
                            c.markRead(ActivityManga.this, false);
                        }
                        break;
                }
                mChapterAdapter.notifyDataSetChanged();
                mode.finish();
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(
                    android.view.ActionMode mode, int position, long id, boolean checked) {
                mChapterAdapter.setSelectedOrUnselected(position);
            }
        });

        mManga = Database.getFullManga(getApplicationContext(), mMangaId);
        setTitle(mManga.getTitle());
        cargarCapitulos(mManga.getChapters());
        Database.updateMangaRead(this, mManga.getId());
        Database.updateNewMangas(ActivityManga.this, mManga, -100);
        cargarDatos(mManga);
    }

    public void cargarDatos(Manga manga) {
        if (mInfo != null && manga != null) {
            String infoExtra = "";
            if (manga.isFinished()) {
                infoExtra = infoExtra +
                        getResources().getString(R.string.finalizado);
            } else {
                infoExtra = infoExtra +
                        getResources().getString(R.string.en_progreso);
            }
            mInfo.setStatus(infoExtra);
            mInfo.setSynopsis(manga.getSynopsis());
            mInfo.setServer(ServerBase.getServer(manga.getServerId()).getServerName());
            if (manga.getAuthor().length() > 1) {
                mInfo.setAuthor(manga.getAuthor());
            } else {
                mInfo.setAuthor(getResources().getString(R.string.nodisponible));
            }
            if (manga.getGenre().length() > 4) {
                mInfo.setGenre(manga.getGenre());
            } else {
                mInfo.setGenre(getResources().getString(R.string.nodisponible));
            }
            mImageLoader.displayImg(manga.getImages(), mInfo);
        }
    }

    public void cargarCapitulos(ArrayList<Chapter> chapters) {
        int fvi = 0;
        if (mChapterAdapter != null) fvi = mListView.getFirstVisiblePosition();
        mChapterAdapter = new ChapterAdapter(this, chapters);
        if (mListView != null) {
            mListView.setAdapter(mChapterAdapter);
            mListView.setSelection(mManga.getLastIndex());
        }
        if (fvi != 0) mListView.setSelection(fvi);
    }

    @Override
    protected void onPause() {
        int first = mListView.getFirstVisiblePosition();
        Database.updateMangaLastIndex(this, mManga.getId(), first);
        super.onPause();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menu.performIdentifierAction(R.id.submenu, 0);
            return true;
        } else
            return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_descargar_restantes: {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage(getString(R.string.download_remain_confirmation));
                dlgAlert.setTitle(R.string.descargarestantes);
                dlgAlert.setCancelable(true);
                dlgAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Chapter> chapters =
                                Database.getChapters(ActivityManga.this, mMangaId, Database.COL_CAP_DOWNLOADED + " != 1", true);
                        for (Chapter c : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(ActivityManga.this, c, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                dlgAlert.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlgAlert.create().show();
                break;
            }
            case R.id.action_marcar_todo_leido: {
                Database.markAllChapters(ActivityManga.this, this.mMangaId, true);
                mManga = Database.getFullManga(getApplicationContext(), this.mMangaId);
                cargarCapitulos(mManga.getChapters());
                break;
            }
            case R.id.action_marcar_todo_no_leido: {
                Database.markAllChapters(ActivityManga.this, this.mMangaId, false);
                mManga = Database.getFullManga(getApplicationContext(), this.mMangaId);
                cargarCapitulos(mManga.getChapters());
                break;
            }
            case R.id.action_sentido: {
                // TODO check database
                int readDirection;
                if (mManga.getReadingDirection() != -1) {
                    readDirection = mManga.getReadingDirection();
                } else {
                    readDirection = Integer.parseInt(
                            pm.getString(DIRECTION, "" + Direction.L2R.ordinal()));
                }
                if (readDirection == Direction.R2L.ordinal()) {
                    mMenuItem.setIcon(R.drawable.ic_action_inverso);
                    this.mDirection = Direction.L2R;
                } else if (readDirection == Direction.L2R.ordinal()) {
                    mMenuItem.setIcon(R.drawable.ic_action_verical);
                    this.mDirection = Direction.VERTICAL;
                } else {
                    mMenuItem.setIcon(R.drawable.ic_action_clasico);
                    this.mDirection = Direction.R2L;
                }
                mManga.setReadingDirection(this.mDirection.ordinal());
                Database.updadeReadOrder(ActivityManga.this, this.mDirection.ordinal(), mManga.getId());
                break;
            }
            case R.id.descargas: {
                Intent intent = new Intent(this, ActivityDownloads.class);
                startActivity(intent);
                break;
            }
            case R.id.action_descargar_no_leidos: {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage(getString(R.string.download_unread_confirmation));
                dlgAlert.setTitle(R.string.descarga_no_leidos);
                dlgAlert.setCancelable(true);
                dlgAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Chapter> chapters =
                                Database.getChapters(ActivityManga.this, ActivityManga.this.mMangaId,
                                        Database.COL_CAP_STATE + " < 1", true);
                        for (Chapter c : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(ActivityManga.this, c, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                dlgAlert.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlgAlert.create().show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Chapter> chapters = Database.getChapters(getApplicationContext(),mMangaId);
        mChapterAdapter.replaceData(chapters);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_chapter, menu);
        mMenuItem = menu.findItem(R.id.action_sentido);
        int readDirection;
        if (mManga.getReadingDirection() != -1) {
            readDirection = mManga.getReadingDirection();
        } else {
            readDirection = Integer.parseInt(pm.getString(DIRECTION, "" + Direction.R2L.ordinal()));
        }

        if (readDirection == Direction.R2L.ordinal()) {
            this.mDirection = Direction.R2L;
            mMenuItem.setIcon(R.drawable.ic_action_clasico);
        } else if (readDirection == Direction.L2R.ordinal()) {
            this.mDirection = Direction.L2R;
            mMenuItem.setIcon(R.drawable.ic_action_inverso);
        } else {
            this.mDirection = Direction.VERTICAL;
            mMenuItem.setIcon(R.drawable.ic_action_verical);
        }
        this.menu = menu;
        return true;
    }

    public enum Direction {
        L2R, R2L, VERTICAL
    }


    private class GetPagesTask extends AsyncTask<Chapter, Void, Chapter> {
        ProgressDialog asyncdialog = new ProgressDialog(ActivityManga.this);
        String error = "";

        @Override
        protected void onPreExecute() {
            try {
                asyncdialog.setMessage(getResources().getString(R.string.iniciando));
                asyncdialog.show();
            } catch (Exception e) {
                //prevent dialog error
            }
        }

        @Override
        protected Chapter doInBackground(Chapter... arg0) {
            Chapter c = arg0[0];
            ServerBase s = ServerBase.getServer(mManga.getServerId());
            try {
                if (c.getPages() < 1) s.chapterInit(c);
            } catch (Exception e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                publishProgress();
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (asyncdialog != null)
                asyncdialog.dismiss();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Chapter result) {
            if (error != null && error.length() > 1) {
                Toast.makeText(ActivityManga.this, error, Toast.LENGTH_LONG).show();
            } else {
                asyncdialog.dismiss();
                Database.updateChapter(ActivityManga.this, result);
                DownloadPoolService.addChapterDownloadPool(ActivityManga.this, result, true);
                int first = mListView.getFirstVisiblePosition();
                Database.updateMangaLastIndex(ActivityManga.this, mManga.getId(), first);
                Intent intent;
                if (pm.getBoolean("test_reader", false)) {
                    intent = new Intent(ActivityManga.this, ActivityReader.class);
                } else {
                    intent = new Intent(ActivityManga.this, ActivityLector.class);
                }
                intent.putExtra(ActivityManga.CHAPTER_ID, result.getId());
                ActivityManga.this.startActivity(intent);
            }
            super.onPostExecute(result);
        }
    }
}
