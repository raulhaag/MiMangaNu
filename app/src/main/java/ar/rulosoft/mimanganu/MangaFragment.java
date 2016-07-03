package ar.rulosoft.mimanganu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fedorvlasov.lazylist.ImageLoader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import ar.rulosoft.mimanganu.adapters.ChapterAdapter;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.ControlInfoNoScroll;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class MangaFragment extends Fragment implements MainActivity.OnKeyUpListener {
    public static final String DIRECTION = "direcciondelectura";
    public static final String CHAPTERS_ORDER = "chapters_order";
    public static final String CHAPTER_ID = "cap_id";
    private static final String TAG = "MangaFragment";
    public SwipeRefreshLayout swipeReLayout;
    public Manga mManga;
    private SearchForNewChapters searchForNewChapters = new SearchForNewChapters();
    private Direction mDirection;
    private ChapterAdapter mChapterAdapter;
    private SharedPreferences pm;
    private ImageLoader mImageLoader;
    private ListView mListView;
    private MenuItem mMenuItemReaderSense, mMenuItemReaderType;
    private int mMangaId, readerType;
    private int chapters_order; // 0 = db | 1 = chapter number | 2 = chapter number asc | 3 = title | 4 = title asc
    private Menu menu;
    private ControlInfoNoScroll mInfo;
    private ServerBase mServerBase;
    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mMangaId = getArguments().getInt(MainFragment.MANGA_ID, -1);
        return inflater.inflate(R.layout.activity_manga, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        super.onCreate(savedInstanceState);
        if (mMangaId == -1) {
            getActivity().onBackPressed();
        }
        mManga = Database.getManga(getActivity(), mMangaId);
        mServerBase = ServerBase.getServer(mManga.getServerId());
        readerType = pm.getBoolean("reader_type", true) ? 1 : 2;
        if (mManga.getReaderType() != 0) {
            readerType = mManga.getReaderType();
        }
        if (getView() != null) {
            mListView = (ListView) getView().findViewById(R.id.lista);
            swipeReLayout = (SwipeRefreshLayout) getView().findViewById(R.id.str);
        }
        mImageLoader = new ImageLoader(getActivity());
        int[] colors = ThemeColors.getColors(pm);
        swipeReLayout.setColorSchemeColors(colors[0], colors[1]);
        if (savedInstanceState != null) {
            if (searchForNewChapters.getStatus() == AsyncTask.Status.RUNNING) {
                swipeReLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeReLayout.setRefreshing(true);
                    }
                });
            }
        }
        swipeReLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if ((searchForNewChapters.getStatus() != AsyncTask.Status.RUNNING)) {
                    searchForNewChapters = new SearchForNewChapters();
                    searchForNewChapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        mListView.setDivider(new ColorDrawable(colors[0]));
        mListView.setDividerHeight(1);
        mInfo = new ControlInfoNoScroll(getActivity());
        mListView.addHeaderView(mInfo);
        mInfo.setColor(MainActivity.darkTheme, colors[0]);
        ChapterAdapter.setColor(MainActivity.darkTheme, colors[1], colors[0]);
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
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.listitem_capitulo_menu_cab, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(final android.view.ActionMode mode, MenuItem item) {
                final SparseBooleanArray selection = mChapterAdapter.getSelection();
                final ServerBase serverBase = ServerBase.getServer(mManga.getServerId());
                boolean finish = true;
                switch (item.getItemId()) {
                    case R.id.select_all:
                        mChapterAdapter.selectAll();
                        return true;
                    case R.id.unselect:
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
                        for (Chapter chapter : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(getActivity(), chapter, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Download add pool error", e);
                            }
                        }
                        break;
                    case R.id.mark_as_read_and_delete_images:
                        new MarkSelectedAsRead(selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter chapter = mChapterAdapter.getItem(selection.keyAt(i));
                            chapter.freeSpace(getActivity(), mManga, serverBase);
                        }
                        break;
                    case R.id.delete_images:
                        for (int i = 0; i < selection.size(); i++) {
                            Chapter chapter = mChapterAdapter.getItem(selection.keyAt(i));
                            chapter.freeSpace(getActivity(), mManga, serverBase);
                        }
                        break;
                    case R.id.remove_chapter:
                        finish = false;
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
                        dlgAlert.setMessage(getString(R.string.delete_comfirm));
                        dlgAlert.setTitle(R.string.app_name);
                        dlgAlert.setCancelable(true);
                        dlgAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SparseBooleanArray selection = mChapterAdapter.getSelection();
                                int[] selected = new int[selection.size()];
                                for (int j = 0; j < selection.size(); j++) {
                                    selected[j] = selection.keyAt(j);
                                }
                                Arrays.sort(selected);
                                for (int i = selection.size() - 1; i >= 0; i--) {
                                    Chapter chapter = mChapterAdapter.getItem(selection.keyAt(i));
                                    chapter.delete(getActivity(), mManga, serverBase);
                                    mChapterAdapter.remove(chapter);
                                    mode.finish();
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
                    case R.id.reset_chapter:
                        for (int i = 0; i < selection.size(); i++) {
                            Chapter chapter = mChapterAdapter.getItem(selection.keyAt(i));
                            chapter.reset(getActivity(), mManga, serverBase);
                        }
                        break;
                    case R.id.mark_selected_as_read:
                        new MarkSelectedAsRead(selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case R.id.mark_selected_as_unread:
                        new MarkSelectedAsUnread(selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                }
                mChapterAdapter.notifyDataSetChanged();
                if (finish)
                    mode.finish();
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(
                    android.view.ActionMode mode, int position, long id, boolean checked) {
                mChapterAdapter.setSelectedOrUnselected(position);
            }
        });

        getActivity().setTitle(mManga.getTitle());
        Database.updateMangaRead(getActivity(), mManga.getId());
        loadInfo(mManga);
        chapters_order = pm.getInt(CHAPTERS_ORDER, 1);
    }

    public void loadInfo(Manga manga) {
        if (mInfo != null && manga != null && isAdded()) {
            String infoExtra = "";
            if (manga.isFinished()) {
                infoExtra = infoExtra +
                        getResources().getString(R.string.finalizado);
            } else {
                infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
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

    public void loadChapters(ArrayList<Chapter> chapters) {
        int fvi = 0;
        if (mChapterAdapter != null) {
            fvi = mListView.getFirstVisiblePosition();
            mChapterAdapter.replaceData(chapters);
        } else {
            if(activity != null)
                mChapterAdapter = new ChapterAdapter(activity, chapters, !(mServerBase instanceof FromFolder));
        }
        if (mListView != null) {
            mListView.setAdapter(mChapterAdapter);
            mListView.setSelection(mManga.getLastIndex());
        }
        if (fvi != 0) mListView.setSelection(fvi);
    }

    @Override
    public void onPause() {
        int first = mListView.getFirstVisiblePosition();
        Database.updateMangaLastIndex(getActivity(), mManga.getId(), first);
        super.onPause();
        if(swipeReLayout != null)
            swipeReLayout.clearAnimation();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menu.performIdentifierAction(R.id.submenu, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_download_remaining: {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
                dlgAlert.setMessage(getString(R.string.download_remain_confirmation));
                dlgAlert.setTitle(R.string.descargarestantes);
                dlgAlert.setCancelable(true);
                dlgAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Chapter> chapters =
                                Database.getChapters(getActivity(), mMangaId, Database.COL_CAP_DOWNLOADED + " != 1", true);
                        for (Chapter chapter : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(getActivity(), chapter, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Download add pool error", e);
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
            case R.id.mark_all_as_read: {
                Database.markAllChapters(getActivity(), this.mMangaId, true);
                new MarkAllAsRead().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case R.id.mark_all_as_unread: {
                Database.markAllChapters(getActivity(), this.mMangaId, false);
                new MarkAllAsUnread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case R.id.action_sentido: {
                int readDirection;
                if (mManga.getReadingDirection() != -1) {
                    readDirection = mManga.getReadingDirection();
                } else {
                    readDirection = Integer.parseInt(pm.getString(DIRECTION, "" + Direction.L2R.ordinal()));
                }
                if (readDirection == Direction.R2L.ordinal()) {
                    mMenuItemReaderSense.setIcon(R.drawable.ic_action_inverso);
                    this.mDirection = Direction.L2R;
                } else if (readDirection == Direction.L2R.ordinal()) {
                    mMenuItemReaderSense.setIcon(R.drawable.ic_action_verical);
                    this.mDirection = Direction.VERTICAL;
                } else {
                    mMenuItemReaderSense.setIcon(R.drawable.ic_action_clasico);
                    this.mDirection = Direction.R2L;
                }
                mManga.setReadingDirection(this.mDirection.ordinal());
                Database.updateReadOrder(getActivity(), this.mDirection.ordinal(), mManga.getId());
                break;
            }
            case R.id.action_reader:
                if (mManga.getReaderType() == 2) {
                    mManga.setReaderType(1);
                    readerType = 1;
                    mMenuItemReaderType.setIcon(R.drawable.ic_action_paged);
                    mMenuItemReaderType.setTitle(R.string.paged_reader);
                } else {
                    mManga.setReaderType(2);
                    readerType = 2;
                    mMenuItemReaderType.setIcon(R.drawable.ic_action_continuous);
                    mMenuItemReaderType.setTitle(R.string.continuous_reader);
                }
                Database.updateManga(getActivity(), mManga, false);
                break;
            case R.id.action_view_download: {
                ((MainActivity) getActivity()).replaceFragment(new DownloadsFragment(), "DownloadFragment");
                break;
            }
            case R.id.action_descargar_no_leidos: {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
                dlgAlert.setMessage(getString(R.string.download_unread_confirmation));
                dlgAlert.setTitle(R.string.descarga_no_leidos);
                dlgAlert.setCancelable(true);
                dlgAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Chapter> chapters =
                                Database.getChapters(getActivity(), MangaFragment.this.mMangaId, Database.COL_CAP_STATE + " < 1", true);
                        for (Chapter c : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(getActivity(), c, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Download add pool error", e);
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
            case R.id.sort_number:
                pm.edit().putInt(CHAPTERS_ORDER, 1).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.NUMBERS_DESC);
                break;

            case R.id.sort_number_asc:
                pm.edit().putInt(CHAPTERS_ORDER, 2).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.NUMBERS_ASC);
                break;

            case R.id.sort_title:
                pm.edit().putInt(CHAPTERS_ORDER, 3).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.TITLE_DESC);
                break;

            case R.id.sort_title_asc:
                pm.edit().putInt(CHAPTERS_ORDER, 4).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.TITLE_ASC);
                break;

            case R.id.sort_as_added_to_db_asc_chapters:
                pm.edit().putInt(CHAPTERS_ORDER, 5).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.DATABASE_ADDED_ASC);
                break;

            case R.id.sort_as_added_to_db_desc_chapters:
                pm.edit().putInt(CHAPTERS_ORDER, 0).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.DATABASE_ADDED_DESC);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).enableHomeButton(true);
        ((MainActivity) getActivity()).setTitle(mManga.getTitle());
        Chapter.Comparators.setManga_title(mManga.getTitle());
        new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        searchForNewChapters.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manga, menu);
        mMenuItemReaderSense = menu.findItem(R.id.action_sentido);
        mMenuItemReaderType = menu.findItem(R.id.action_reader);
        int readDirection;
        if (mManga.getReadingDirection() != -1) {
            readDirection = mManga.getReadingDirection();
        } else {
            readDirection = Integer.parseInt(pm.getString(DIRECTION, "" + Direction.R2L.ordinal()));
        }

        if (readDirection == Direction.R2L.ordinal()) {
            this.mDirection = Direction.R2L;
            mMenuItemReaderSense.setIcon(R.drawable.ic_action_clasico);
        } else if (readDirection == Direction.L2R.ordinal()) {
            this.mDirection = Direction.L2R;
            mMenuItemReaderSense.setIcon(R.drawable.ic_action_inverso);
        } else {
            this.mDirection = Direction.VERTICAL;
            mMenuItemReaderSense.setIcon(R.drawable.ic_action_verical);
        }

        if (readerType == 2) {
            mMenuItemReaderType.setIcon(R.drawable.ic_action_continuous);
            mMenuItemReaderType.setTitle(R.string.continuous_reader);
        } else {
            mMenuItemReaderType.setIcon(R.drawable.ic_action_paged);
            mMenuItemReaderType.setTitle(R.string.paged_reader);
        }

        this.menu = menu;
    }

    public enum Direction {
        L2R, R2L, VERTICAL
    }

    private class GetPagesTask extends AsyncTask<Chapter, Void, Chapter> {
        ProgressDialog asyncdialog = new ProgressDialog(getActivity());
        String error = "";

        @Override
        protected void onPreExecute() {
            try {
                asyncdialog.setMessage(getResources().getString(R.string.iniciando));
                asyncdialog.show();
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
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
                Log.e(TAG, "ChapterInit error", e);
            } finally {
                publishProgress();
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (asyncdialog != null && isAdded())
                asyncdialog.dismiss();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Chapter result) {
            if(isAdded()) {
                if (error != null && error.length() > 1) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        asyncdialog.dismiss();
                        Database.updateChapter(getActivity(), result);
                        DownloadPoolService.addChapterDownloadPool(getActivity(), result, true);
                        int first = mListView.getFirstVisiblePosition();
                        Database.updateMangaLastIndex(getActivity(), mManga.getId(), first);
                        Intent intent;
                        if (readerType == 2) {
                            intent = new Intent(getActivity(), ActivityReader.class);
                        } else {
                            intent = new Intent(getActivity(), ActivityPagedReader.class);
                        }
                        intent.putExtra(MangaFragment.CHAPTER_ID, result.getId());
                        MangaFragment.this.startActivity(intent);
                    }catch (Exception e){
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
            super.onPostExecute(result);
        }
    }

    public class SearchForNewChapters extends AsyncTask<Void, Void, Integer> {
        boolean running = false;
        SearchForNewChapters actual = null;
        int mangaId = 0;
        String msg;
        String orgMsg;
        String errorMsg;

        @Override
        protected void onPreExecute() {
            running = true;
            actual = this;
            msg = getResources().getString(R.string.searching_for_updates);
            orgMsg = getActivity().getTitle().toString();
            getActivity().setTitle(msg + " " + orgMsg);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            int result = 0;
            ServerBase s = ServerBase.getServer(mManga.getServerId());
            mangaId = mManga.getId();
            try {
                int diff = s.searchForNewChapters(mManga.getId(), getActivity());
                result += diff;
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    errorMsg = getResources().getString(R.string.error) + ":" + e.getMessage();
                } else {
                    errorMsg = getResources().getString(R.string.error);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Manga manga = Database.getManga(getActivity(), mangaId);
            loadInfo(manga);
            new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            swipeReLayout.setRefreshing(false);
            if(isAdded()) {
                getActivity().setTitle(orgMsg);
            }
            if (result > 0) {
                if(isAdded())
                    Toast.makeText(getActivity(), getString(R.string.mgs_update_found, result), Toast.LENGTH_SHORT).show();
            } else if (errorMsg != null && errorMsg.length() > 2) {
                if(isAdded())
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
            }
            running = false;
            actual = null;
        }
    }

    public class SortAndLoadChapters extends AsyncTask<Void, Void, Void> {
        ArrayList<Chapter> chapters;

        @Override
        protected Void doInBackground(Void... params) {
                chapters = Database.getChapters(getActivity(), mMangaId);
                try {
                    switch (chapters_order) {
                        case 1:
                            Collections.sort(chapters, Chapter.Comparators.NUMBERS_DESC);
                            break;
                        case 2:
                            Collections.sort(chapters, Chapter.Comparators.NUMBERS_ASC);
                            break;
                        case 3:
                            Collections.sort(chapters, Chapter.Comparators.TITLE_DESC);
                            break;
                        case 4:
                            Collections.sort(chapters, Chapter.Comparators.TITLE_ASC);
                            break;
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Log.d(TAG, sw.toString());
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadChapters(chapters);
        }
    }

    private class MarkAllAsUnread extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mChapterAdapter.getCount(); i++) {
                if (mChapterAdapter.getItem(i).getPages() != 0) {
                    Chapter chapter = mChapterAdapter.getItem(i);
                    chapter.markRead(getActivity(), false);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class MarkAllAsRead extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mChapterAdapter.getCount(); i++) {
                if (mChapterAdapter.getItem(i).getPages() != 0) {
                    Chapter chapter = mChapterAdapter.getItem(i);
                    chapter.markRead(getActivity(), true);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class MarkSelectedAsRead extends AsyncTask<Void, Integer, Void> {
        int selectionSize = 0;

        public MarkSelectedAsRead(int selectionSize) {
            super();
            this.selectionSize = selectionSize;
        }

        @Override
        protected Void doInBackground(Void... params) {
                for (int i = 0; i < selectionSize; i++) {
                    if(mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i)).getReadStatus() != 1) {
                        Chapter chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                        chapter.markRead(getActivity(), true);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mChapterAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    private class MarkSelectedAsUnread extends AsyncTask<Void, Integer, Void> {
        int selectionSize = 0;

        public MarkSelectedAsUnread(int selectionSize) {
            super();
            this.selectionSize = selectionSize;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < selectionSize; i++) {
                if(mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i)).getReadStatus() != 0) {
                    Chapter chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                    chapter.markRead(getActivity(), false);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mChapterAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

}
