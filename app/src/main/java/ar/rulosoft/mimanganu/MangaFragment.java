package ar.rulosoft.mimanganu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
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

import com.fedorvlasov.lazylist.ImageLoader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ar.rulosoft.mimanganu.adapters.ChapterAdapter;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.ControlInfoNoScroll;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.readers.Reader.Direction;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

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
    private int chapters_order; // 0 = db_desc | 1 = chapter number | 2 = chapter number asc | 3 = title | 4 = title asc | 5 = db_asc
    private Menu menu;
    private ControlInfoNoScroll mInfo;
    private ServerBase mServerBase;
    private MainActivity mActivity;
    private int mNotifyID_DeleteImages = (int) System.currentTimeMillis();
    private int mNotifyID_MarkSelectedAsRead = (int) System.currentTimeMillis();
    private int mNotifyID_MarkSelectedAsUnread = (int) System.currentTimeMillis();
    private int mNotifyID_RemoveChapters = (int) System.currentTimeMillis();
    private int mNotifyID_ResetChapters = (int) System.currentTimeMillis();

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
        super.onActivityCreated(savedInstanceState);
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
            MainActivity.cLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinator_layout);
        }
        mImageLoader = new ImageLoader(getActivity());
        final int[] colors = ThemeColors.getColors(pm);
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
                        new AsyncAddChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChapterAdapter.getSelectedChapters());
                        break;
                    case R.id.mark_as_read_and_delete_images:
                        new MarkSelectedAsRead(selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        new DeleteImages(serverBase, selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case R.id.delete_images:
                        new DeleteImages(serverBase, selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case R.id.remove_chapter:
                        finish = false;
                        Snackbar confirm = Snackbar.make(MainActivity.cLayout, R.string.delete_confirm, Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.yes, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        SparseBooleanArray selection = mChapterAdapter.getSelection();
                                        int[] selected = new int[selection.size()];
                                        for (int j = 0; j < selection.size(); j++) {
                                            selected[j] = selection.keyAt(j);
                                        }
                                        Arrays.sort(selected);

                                        if(selection.size() < 8) {
                                            // Remove chapters on UI Thread
                                            for (int i = selection.size() - 1; i >= 0; i--) {
                                                Chapter chapter = mChapterAdapter.getItem(selection.keyAt(i));
                                                chapter.delete(getActivity(), mManga, serverBase);
                                                mChapterAdapter.remove(chapter);
                                                mode.finish();
                                            }
                                        } else {
                                            new RemoveChapters(serverBase, selection.size(), mode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        }
                                    }
                                });
                        confirm.getView().setBackgroundColor(MainActivity.colors[0]);
                        confirm.setActionTextColor(Color.WHITE);
                        confirm.show();
                        break;
                    case R.id.reset_chapter:
                        new ResetChapters(serverBase, selection.size()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            if (mActivity != null)
                mChapterAdapter = new ChapterAdapter(mActivity, chapters, !(mServerBase instanceof FromFolder));
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
        if (swipeReLayout != null)
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
                Snackbar confirm = Snackbar.make(MainActivity.cLayout, R.string.download_remain_confirmation, Snackbar.LENGTH_INDEFINITE);
                confirm.setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Chapter> chapters = Database.getChapters(getActivity(), mMangaId, Database.COL_CAP_DOWNLOADED + " != 1", true);
                        for (Chapter chapter : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(getActivity(), chapter, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Download add pool error", e);
                            }
                        }
                    }
                });
                confirm.getView().setBackgroundColor(MainActivity.colors[0]);
                confirm.setActionTextColor(Color.WHITE);
                confirm.show();
            }
            break;
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
                Snackbar confirm = Snackbar.make(MainActivity.cLayout, R.string.download_unread_confirmation, Snackbar.LENGTH_INDEFINITE);
                confirm.setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Chapter> chapters = Database.getChapters(getActivity(), MangaFragment.this.mMangaId, Database.COL_CAP_STATE + " < 1", true);
                        for (Chapter c : chapters) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(getActivity(), c, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Download add pool error", e);
                            }
                        }
                    }
                });
                confirm.getView().setBackgroundColor(MainActivity.colors[0]);
                confirm.setActionTextColor(Color.WHITE);
                confirm.show();
                break;
            }
            case R.id.sort_number:
                pm.edit().putInt(CHAPTERS_ORDER, 1).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.NUMBERS_DESC);
                item.setChecked(true);
                break;

            case R.id.sort_number_asc:
                pm.edit().putInt(CHAPTERS_ORDER, 2).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.NUMBERS_ASC);
                item.setChecked(true);
                break;

            case R.id.sort_title:
                pm.edit().putInt(CHAPTERS_ORDER, 3).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.TITLE_DESC);
                item.setChecked(true);
                break;

            case R.id.sort_title_asc:
                pm.edit().putInt(CHAPTERS_ORDER, 4).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.TITLE_ASC);
                item.setChecked(true);
                break;

            case R.id.sort_as_added_to_db_asc_chapters:
                pm.edit().putInt(CHAPTERS_ORDER, 5).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.DATABASE_ADDED_ASC);
                item.setChecked(true);
                break;

            case R.id.sort_as_added_to_db_desc_chapters:
                pm.edit().putInt(CHAPTERS_ORDER, 0).apply();
                mChapterAdapter.sort_chapters(Chapter.Comparators.DATABASE_ADDED_DESC);
                item.setChecked(true);
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
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        searchForNewChapters.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manga, menu);
        mMenuItemReaderSense = menu.findItem(R.id.action_sentido);
        mMenuItemReaderType = menu.findItem(R.id.action_reader);
        int sortList[] = {
                R.id.sort_as_added_to_db_desc_chapters, R.id.sort_number,
                R.id.sort_number_asc, R.id.sort_title,
                R.id.sort_title_asc, R.id.sort_as_added_to_db_asc_chapters
        };
        menu.findItem(sortList[chapters_order]).setChecked(true);
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

    private class AsyncAddChapters extends AsyncTask<Chapter, Void, Void> {

        @Override
        protected Void doInBackground(Chapter... chapters) {
            List<Chapter> chaptersList = Arrays.asList(chapters);
            Collections.sort(chaptersList, Chapter.Comparators.NUMBERS_ASC);
            for (Chapter chapter : chaptersList) {
                try {
                    DownloadPoolService.addChapterDownloadPool(getActivity(), chapter, false);
                } catch (Exception e) {
                    Log.e(TAG, "Download add pool error", e);
                }
            }
            return null;
        }
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
            if (isAdded()) {
                if (error != null && error.length() > 1 && mActivity != null) {
                    Util.getInstance().showFastSnackBar(error, mActivity);
                } else {
                    try {
                        asyncdialog.dismiss();
                        Database.updateChapter(getActivity(), result);
                        DownloadPoolService.addChapterDownloadPool(getActivity(), result, true);
                        int first = mListView.getFirstVisiblePosition();
                        Database.updateMangaLastIndex(getActivity(), mManga.getId(), first);
                        Intent intent = new Intent(getActivity(), ActivityReader.class);
                        intent.putExtra(MangaFragment.CHAPTER_ID, result.getId());
                        MangaFragment.this.startActivity(intent);
                    } catch (Exception e) {
                        if (e.getMessage() != null && mActivity != null) {
                            Util.getInstance().showFastSnackBar(e.getMessage(), mActivity);
                        }
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
                int diff = s.searchForNewChapters(mManga.getId(), getActivity(), false);//always full update
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
            if (isAdded()) {
                getActivity().setTitle(orgMsg);
            }
            if (result > 0) {
                if (isAdded()) {
                    Util.getInstance().showFastSnackBar(getString(R.string.mgs_update_found, result), mActivity);
                }
            } else if (errorMsg != null && errorMsg.length() > 2) {
                if (isAdded()) {
                    Util.getInstance().showFastSnackBar(errorMsg, mActivity);
                }
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
                int chaptersOrder;
                if (pm != null)
                    chaptersOrder = pm.getInt(CHAPTERS_ORDER, 1);
                else
                    chaptersOrder = chapters_order;
                switch (chaptersOrder) {
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
                    case 5:
                        Collections.sort(chapters, Chapter.Comparators.DATABASE_ADDED_ASC);
                        break;
                    case 0:
                        Collections.sort(chapters, Chapter.Comparators.DATABASE_ADDED_DESC);
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
        private int selectionSize = 0;
        private Chapter chapter;

        public MarkSelectedAsRead(int selectionSize) {
            super();
            this.selectionSize = selectionSize;
            mNotifyID_MarkSelectedAsRead = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_MarkSelectedAsRead, getString(R.string.marking_as_read), "");
        }

        @Override
        protected Void doInBackground(Void... params) {
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                if (mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i)).getReadStatus() != 1) {
                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                    chapter.markRead(getActivity(), true);
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            mChapterAdapter.notifyDataSetChanged();
            Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_MarkSelectedAsRead, getString(R.string.marking_as_read), chapter.getTitle(), true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Util.getInstance().cancelNotification(mNotifyID_MarkSelectedAsRead);
        }
    }

    private class MarkSelectedAsUnread extends AsyncTask<Void, Integer, Void> {
        private int selectionSize = 0;
        private Chapter chapter;

        public MarkSelectedAsUnread(int selectionSize) {
            super();
            this.selectionSize = selectionSize;
            mNotifyID_MarkSelectedAsUnread = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_MarkSelectedAsUnread, getString(R.string.marking_as_unread), "");
        }

        @Override
        protected Void doInBackground(Void... params) {
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                if (mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i)).getReadStatus() != 0) {
                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                    chapter.markRead(getActivity(), false);
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            mChapterAdapter.notifyDataSetChanged();
            Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_MarkSelectedAsUnread, getString(R.string.marking_as_unread), chapter.getTitle(), true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Util.getInstance().cancelNotification(mNotifyID_MarkSelectedAsUnread);
        }
    }

    private class DeleteImages extends AsyncTask<Void, Integer, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private Chapter chapter;

        public DeleteImages(ServerBase serverBase, int selectionSize) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            mNotifyID_DeleteImages = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_DeleteImages, getString(R.string.deleting), "");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                if(isAdded()) {
                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                    chapter.freeSpace(getActivity(), mManga, serverBase);
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            mChapterAdapter.notifyDataSetChanged();
            Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_DeleteImages, getString(R.string.deleting), chapter.getTitle(), true);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Util.getInstance().cancelNotification(mNotifyID_DeleteImages);
        }
    }

    private class RemoveChapters extends AsyncTask<Void, Integer, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private Chapter chapter;
        private ActionMode mode;
        int j = 0;

        public RemoveChapters(ServerBase serverBase, int selectionSize, ActionMode mode) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            this.mode = mode;
            mNotifyID_RemoveChapters = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_RemoveChapters, getString(R.string.removing_chapters), "");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            long initTime = System.currentTimeMillis();
            for (int i = selectionSize - 1; i >= 0; i--) {
                if(isAdded()) {
                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                    chapter.delete(getActivity(), mManga, serverBase);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mChapterAdapter.remove(chapter);
                            mode.finish();
                        }
                    });
                    j++;
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(j);
                        initTime = System.currentTimeMillis();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_RemoveChapters, getString(R.string.removing_chapters), chapter.getTitle(), true);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Util.getInstance().cancelNotification(mNotifyID_RemoveChapters);
        }
    }

    private class ResetChapters extends AsyncTask<Void, Integer, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private Chapter chapter;

        public ResetChapters(ServerBase serverBase, int selectionSize) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            mNotifyID_ResetChapters = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_ResetChapters, getString(R.string.resetting_chapters), "");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                if(isAdded()) {
                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i));
                    chapter.reset(getActivity(), mManga, serverBase);
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_ResetChapters, getString(R.string.resetting_chapters), chapter.getTitle(), true);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Util.getInstance().cancelNotification(mNotifyID_ResetChapters);
        }
    }

}
