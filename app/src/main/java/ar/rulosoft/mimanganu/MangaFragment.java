package ar.rulosoft.mimanganu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ActionMode;
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

import java.io.File;
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
import ar.rulosoft.mimanganu.componentes.ReaderOptions;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.Paths;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

public class MangaFragment extends Fragment implements MainActivity.OnBackListener {
    public static final String DIRECTION = "direcciondelectura";
    public static final String CHAPTERS_ORDER = "chapters_order";
    public static final String CHAPTER_ID = "cap_id";
    public static final String CHAPTERS_HIDE_READ = "hide_read_chapters";
    private static final String TAG = "MangaFragment";
    public SwipeRefreshLayout swipeReLayout;
    public Manga mManga;
    public int mMangaId;
    private SearchForNewChapters searchForNewChapters = new SearchForNewChapters();
    private RemoveChapters removeChapters = null;
    private ResetChapters resetChapters = null;
    private DeleteImages deleteImages = null;
    private MarkSelectedAsRead markSelectedAsRead = null;
    private MarkSelectedAsUnread markSelectedAsUnread = null;
    private GetPagesTask getPagesTask = null;
    private ChapterAdapter mChapterAdapter;
    private SharedPreferences pm;
    private ImageLoader mImageLoader;
    private ListView mListView;
    private int chapters_order; // 0 = db_desc | 1 = chapter number | 2 = chapter number asc | 3 = title | 4 = title asc | 5 = db_asc
    private boolean hide_read;
    private ControlInfoNoScroll mInfo;
    private ServerBase mServerBase;
    private MainActivity mActivity;
    private int mNotifyID_DeleteImages = (int) System.currentTimeMillis();
    private int mNotifyID_MarkSelectedAsRead = (int) System.currentTimeMillis();
    private int mNotifyID_MarkSelectedAsUnread = (int) System.currentTimeMillis();
    private int mNotifyID_RemoveChapters = (int) System.currentTimeMillis();
    private int mNotifyID_ResetChapters = (int) System.currentTimeMillis();
    private ReaderOptions readerOptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        ((MainActivity)getActivity()).setOnBackListener(this);
        mMangaId = getArguments().getInt(MainFragment.MANGA_ID, -1);
        return inflater.inflate(R.layout.fragment_manga, container, false);
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
        mServerBase = ServerBase.getServer(mManga.getServerId(), getContext());
        if (getView() == null) {
            try {
                finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }else {
            mListView = getView().findViewById(R.id.list);
            swipeReLayout = getView().findViewById(R.id.str);
            readerOptions = getView().findViewById(R.id.reader_options);
            mImageLoader = new ImageLoader(getActivity());
            final int[] colors = ThemeColors.getColors(pm);
            readerOptions.setBackgroundColor(colors[0]);
            readerOptions.setManga(mManga);
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
            mInfo.enableTitleCopy(getActivity(), mManga.getTitle());
            ChapterAdapter.setColor(MainActivity.darkTheme, colors[1], colors[0]);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Chapter c = (Chapter) mListView.getAdapter().getItem(position);
                    getPagesTask = new GetPagesTask();
                    getPagesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, c);
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
                    final ServerBase serverBase = ServerBase.getServer(mManga.getServerId(), getContext());
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
                            markSelectedAsRead = new MarkSelectedAsRead(selection.size());
                            markSelectedAsRead.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            deleteImages = new DeleteImages(serverBase, selection.size());
                            deleteImages.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            break;
                        case R.id.delete_images:
                            deleteImages = new DeleteImages(serverBase, selection.size());
                            deleteImages.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            break;
                        case R.id.remove_chapter:
                            finish = false;
                            new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.app_name)
                                    .setMessage(R.string.delete_confirm)
                                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (selection.size() < 8) {
                                                // Remove chapters on UI Thread
                                                for (int i = selection.size() - 1; i >= 0; i--) {
                                                    Chapter chapter = mChapterAdapter.getItem(selection.keyAt(i));
                                                    chapter.delete(getActivity(), mManga, serverBase);
                                                    mChapterAdapter.remove(chapter);
                                                    mode.finish();
                                                }
                                            } else {
                                                removeChapters = new RemoveChapters(serverBase, selection.size(), mode);
                                                removeChapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                            break;
                        case R.id.reset_chapter:
                            resetChapters = new ResetChapters(serverBase, selection.size());
                            resetChapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            break;
                        case R.id.mark_selected_as_read:
                            markSelectedAsRead = new MarkSelectedAsRead(selection.size());
                            markSelectedAsRead.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            break;
                        case R.id.mark_selected_as_unread:
                            markSelectedAsUnread = new MarkSelectedAsUnread(selection.size());
                            markSelectedAsUnread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            hide_read = pm.getBoolean(CHAPTERS_HIDE_READ, false);
        }
    }

    public void loadInfo(Manga manga) {
        if (mInfo != null && manga != null && isAdded()) {
            mInfo.setStatus(manga.isFinished()?getResources().getString(R.string.finalizado):getResources().getString(R.string.en_progreso));
            mInfo.setServer(ServerBase.getServer(manga.getServerId(), getContext()).getServerName());

            if (manga.getSynopsis() != null) {
                mInfo.setSynopsis(manga.getSynopsis());
            }
            else {
                mInfo.setSynopsis(getResources().getString(R.string.nodisponible));
            }
            if (manga.getAuthor() != null) {
                mInfo.setAuthor(manga.getAuthor());
            } else {
                mInfo.setAuthor(getResources().getString(R.string.nodisponible));
            }
            if (manga.getGenre() != null) {
                mInfo.setGenre(manga.getGenre());
            } else {
                mInfo.setGenre(getResources().getString(R.string.nodisponible));
            }
            if (manga.getLastUpdate()!= null) {
                mInfo.setLastUpdate(manga.getLastUpdate());
            }
            else {
                mInfo.setLastUpdate(getResources().getString(R.string.nodisponible));
            }
            mImageLoader.displayImg(manga.getImages(), mInfo);
        }
    }

    public void loadChapters(ArrayList<Chapter> chapters) {
        if (isAdded()) {
            int fvi = 0;
            if (mChapterAdapter != null) {
                fvi = mListView.getFirstVisiblePosition();
                mChapterAdapter.replaceData(chapters);
            } else {
                if (mActivity != null)
                    mChapterAdapter = new ChapterAdapter(mActivity, chapters, !(mServerBase instanceof FromFolder));
                DownloadPoolService.setStateChangeListener(mChapterAdapter);
            }
            if (mListView != null) {
                mListView.setAdapter(mChapterAdapter);
                mListView.setSelection(mManga.getLastIndex());
            }
            if (fvi != 0) mListView.setSelection(fvi);
        }
    }

    @Override
    public void onPause() {
        if (mListView.getAdapter() != null) {
            int first = mListView.getFirstVisiblePosition();
            Database.updateMangaLastIndex(getActivity(), mManga.getId(), first);
        }
        DownloadPoolService.setStateChangeListener(null);
        super.onPause();
        if (swipeReLayout != null)
            swipeReLayout.clearAnimation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_config_reader:
                readerOptions.switchOptions();
                break;
            case R.id.action_download_remaining: {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.download_remain_confirmation)
                        .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<Chapter> chapters = Database.getChapters(getActivity(), mMangaId, Database.COL_CAP_DOWNLOADED + " != 1", true);
                                for (Chapter chapter : chapters) {
                                    try {
                                        DownloadPoolService.addChapterDownloadPool(getActivity(), chapter, false);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Download add pool error", e);
                                    }
                                }
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            break;
            case R.id.mark_all_as_read: {
                Database.markAllChapters(getActivity(), this.mMangaId, true);
                new SetChaptersPageCountAsRead().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case R.id.mark_all_as_unread: {
                Database.markAllChapters(getActivity(), this.mMangaId, false);
                new SetChaptersPageCountAsUnread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case R.id.action_hide_read: {
                item.setChecked(!item.isChecked());
                hide_read = item.isChecked();
                pm.edit().putBoolean(CHAPTERS_HIDE_READ, hide_read).apply();
                new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if (mChapterAdapter != null) {
                    DownloadPoolService.setStateChangeListener(mChapterAdapter);
                }
                break;
            }
            case R.id.delete: {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.app_name)
                        .setMessage(getString(R.string.manga_delete_confirm, mManga.getTitle()))
                        .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DownloadPoolService.forceStop(mManga.getId());
                                ServerBase serverBase = ServerBase.getServer(mManga.getServerId(), getContext());
                                String path = Paths.generateBasePath(serverBase, mManga, getActivity());
                                Util.getInstance().deleteRecursive(new File(path));
                                Database.deleteManga(getActivity(), mManga.getId());
                                dialog.dismiss();
                                getActivity().onBackPressed();
                            }
                        })
                        .show();
                break;
            }
            case R.id.action_view_download: {
                ((MainActivity) getActivity()).replaceFragment(new DownloadsFragment(), "DownloadFragment");
                break;
            }
            case R.id.action_download_unread: {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.download_unread_confirmation)
                        .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<Chapter> chapters = Database.getChapters(getActivity(), MangaFragment.this.mMangaId, Database.COL_CAP_STATE + " != 1" + " AND " + Database.COL_CAP_DOWNLOADED + " != 1", true);
                                for (Chapter c : chapters) {
                                    try {
                                        DownloadPoolService.addChapterDownloadPool(getActivity(), c, false);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Download add pool error", e);
                                    }
                                }
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
            case R.id.sort_number:
                pm.edit().putInt(CHAPTERS_ORDER, 1).apply();
                if (mChapterAdapter != null)
                    mChapterAdapter.sort_chapters(Chapter.Comparators.NUMBERS_DESC);
                else
                    Log.e("MangaFragment", "can't sort chapters mChapterAdapter is null");
                item.setChecked(true);
                break;

            case R.id.sort_number_asc:
                pm.edit().putInt(CHAPTERS_ORDER, 2).apply();
                if (mChapterAdapter != null)
                    mChapterAdapter.sort_chapters(Chapter.Comparators.NUMBERS_ASC);
                else
                    Log.e("MangaFragment", "can't sort chapters mChapterAdapter is null");
                item.setChecked(true);
                break;

            case R.id.sort_title:
                pm.edit().putInt(CHAPTERS_ORDER, 3).apply();
                if (mChapterAdapter != null)
                    mChapterAdapter.sort_chapters(Chapter.Comparators.TITLE_DESC);
                else
                    Log.e("MangaFragment", "can't sort chapters mChapterAdapter is null");
                item.setChecked(true);
                break;

            case R.id.sort_title_asc:
                pm.edit().putInt(CHAPTERS_ORDER, 4).apply();
                if (mChapterAdapter != null)
                    mChapterAdapter.sort_chapters(Chapter.Comparators.TITLE_ASC);
                else
                    Log.e("MangaFragment", "can't sort chapters mChapterAdapter is null");
                item.setChecked(true);
                break;

            case R.id.sort_as_added_to_db_asc_chapters:
                pm.edit().putInt(CHAPTERS_ORDER, 5).apply();
                if (mChapterAdapter != null)
                    mChapterAdapter.sort_chapters(Chapter.Comparators.DATABASE_ADDED_ASC);
                else
                    Log.e("MangaFragment", "can't sort chapters mChapterAdapter is null");
                item.setChecked(true);
                break;

            case R.id.sort_as_added_to_db_desc_chapters:
                pm.edit().putInt(CHAPTERS_ORDER, 0).apply();
                if (mChapterAdapter != null)
                    mChapterAdapter.sort_chapters(Chapter.Comparators.DATABASE_ADDED_DESC);
                else
                    Log.e("MangaFragment", "can't sort chapters mChapterAdapter is null");
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
        if (mChapterAdapter != null) {
            DownloadPoolService.setStateChangeListener(mChapterAdapter);
        }
        mListView.setSelection(mManga.getLastIndex());
//        readerOptions.setValues(); //to update data before reader change values
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
        if (searchForNewChapters != null)
            searchForNewChapters.cancel(true);
        if (removeChapters != null)
            removeChapters.cancel(true);
        if (resetChapters != null)
            resetChapters.cancel(true);
        if (deleteImages != null)
            deleteImages.cancel(true);
        if (markSelectedAsRead != null)
            markSelectedAsRead.cancel(true);
        if (markSelectedAsUnread != null)
            markSelectedAsUnread.cancel(true);
        if (getPagesTask != null)
            getPagesTask.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manga, menu);
        int sortList[] = {
                R.id.sort_as_added_to_db_desc_chapters, R.id.sort_number,
                R.id.sort_number_asc, R.id.sort_title,
                R.id.sort_title_asc, R.id.sort_as_added_to_db_asc_chapters
        };
        menu.findItem(sortList[chapters_order]).setChecked(true);
        menu.findItem(R.id.action_hide_read).setChecked(hide_read);
    }

    @Override
    public boolean onBackPressed() {
        if(readerOptions.isVisible()){
            readerOptions.switchOptions();
            return true;
        }
        return false;
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
        ProgressDialog asyncProgressDialog = new ProgressDialog(getContext());
        String error = "";

        @Override
        protected void onPreExecute() {
            try {
                asyncProgressDialog.setMessage(getResources().getString(R.string.iniciando));
                asyncProgressDialog.show();
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }

        @Override
        protected Chapter doInBackground(Chapter... arg0) {
            Chapter c = arg0[0];
            ServerBase s = ServerBase.getServer(mManga.getServerId(), getContext());
            try {
                if (c.getPages() < 1) s.chapterInit(c);
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    error = e.getMessage();
                } else {
                    error = "NullPointerException";
                }
                Log.e(TAG, "ChapterInit error", e);
            } finally {
                publishProgress();
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                if ((asyncProgressDialog != null) && isAdded() && asyncProgressDialog.isShowing()) {
                    asyncProgressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Chapter result) {
            if (isAdded()) {
                if (!error.isEmpty()) {
                    Util.getInstance().toast(getContext(), error);
                } else {
                    try {
                        if ((asyncProgressDialog != null) && isAdded() && asyncProgressDialog.isShowing()) {
                            asyncProgressDialog.dismiss();
                        }
                        Database.updateChapter(getActivity(), result);
                        DownloadPoolService.addChapterDownloadPool(getActivity(), result, true);
                        int first = mListView.getFirstVisiblePosition();
                        Database.updateMangaLastIndex(getActivity(), mManga.getId(), first);
                        Bundle bundle = new Bundle();
                        bundle.putInt(MangaFragment.CHAPTER_ID, result.getId());
                        ReaderFragment readerFragment = new ReaderFragment();
                        readerFragment.setArguments(bundle);
                        ((MainActivity) getActivity()).replaceFragment(readerFragment, "ReaderFragment");
                    } catch (Exception e) {
                        Log.e(TAG, "Exception", e);
                        if (e.getMessage() != null) {
                            error = e.getMessage();
                        } else {
                            error = "NullPointerException";
                        }
                        Util.getInstance().toast(getContext(), error);
                    }
                }
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            try {
                if ((asyncProgressDialog != null) && isAdded() && asyncProgressDialog.isShowing()) {
                    asyncProgressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }
    }

    private class SearchForNewChapters extends AsyncTask<Void, Void, Integer> {
        boolean running = false;
        SearchForNewChapters actual = null;
        int mangaId = 0;
        String msg;
        String orgMsg;
        String error = "";

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
            ServerBase s = ServerBase.getServer(mManga.getServerId(), getContext());
            mangaId = mManga.getId();
            try {
                int diff = s.searchForNewChapters(mManga.getId(), getActivity(), false);//always full update
                result += diff;
            } catch (Exception e) {
                error = Log.getStackTraceString(e);
                Log.e(TAG, "Exception", e);
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
                    Util.getInstance().showFastSnackBar(getString(R.string.mgs_update_found, "" + result), getView(), getContext());
                }
            } else if (!error.isEmpty()) {
                if (isAdded()) {
                    Util.getInstance().toast(getContext(), error);
                }
            }
            running = false;
            actual = null;
        }
    }

    private class SortAndLoadChapters extends AsyncTask<Void, Void, Void> {
        ArrayList<Chapter> chapters;

        @Override
        protected Void doInBackground(Void... params) {
            String condition = "1";
            if(hide_read) {
                condition = Database.COL_CAP_STATE + " != 1";
            }
            chapters = Database.getChapters(getActivity(), mMangaId, condition);
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

    private class SetChaptersPageCountAsUnread extends AsyncTask<Void, Integer, Void> {
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
            if (isAdded())
                new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class SetChaptersPageCountAsRead extends AsyncTask<Void, Integer, Void> {
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
            if (isAdded())
                new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class MarkSelectedAsRead extends AsyncTask<Void, Integer, Void> {
        private int selectionSize = 0;
        private Chapter chapter;
        private int threads = Runtime.getRuntime().availableProcessors();
        private int ticket = threads;

        MarkSelectedAsRead(int selectionSize) {
            super();
            this.selectionSize = selectionSize;
            mNotifyID_MarkSelectedAsRead = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (selectionSize > 7)
                Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_MarkSelectedAsRead, getString(R.string.marking_as_read), "");
        }

        @Override
        protected Void doInBackground(Void... params) {
            ticket = threads;
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                final int idxNow = i;
                while (ticket < 1) {
                    if (System.currentTimeMillis() - initTime > 250) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
                ticket--;

                if (mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i)).getReadStatus() != 1) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isAdded() && !isCancelled()) {
                                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(idxNow));
                                    chapter.markRead(getActivity(), true);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            } finally {
                                ticket++;
                            }

                        }
                    }).start();
                } else {
                    ticket++;
                }
            } //for loop

            publishProgress(selectionSize);
            while (ticket < threads) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "After sleep failure", e);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded() && !isCancelled()) {
                mChapterAdapter.notifyDataSetChanged();
                if (selectionSize > 7 && chapter != null)
                    Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_MarkSelectedAsRead, getString(R.string.marking_as_read), chapter.getTitle(), true);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mChapterAdapter.notifyDataSetChanged();
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_MarkSelectedAsRead);
        }

        @Override
        protected void onCancelled() {
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_MarkSelectedAsRead);
        }
    }

    private class MarkSelectedAsUnread extends AsyncTask<Void, Integer, Void> {
        private int selectionSize = 0;
        private Chapter chapter;
        private int threads = Runtime.getRuntime().availableProcessors();
        private int ticket = threads;

        MarkSelectedAsUnread(int selectionSize) {
            super();
            this.selectionSize = selectionSize;
            mNotifyID_MarkSelectedAsUnread = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (selectionSize > 7)
                Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_MarkSelectedAsUnread, getString(R.string.marking_as_unread), "");
        }

        @Override
        protected Void doInBackground(Void... params) {
            ticket = threads;
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                final int idxNow = i;
                while (ticket < 1) {
                    if (System.currentTimeMillis() - initTime > 250) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
                ticket--;

                if (mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(i)).getReadStatus() != 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isAdded() && !isCancelled()) {
                                    chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(idxNow));
                                    chapter.markRead(getActivity(), false);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            } finally {
                                ticket++;
                            }

                        }
                    }).start();
                } else {
                    ticket++;
                }
            } //for loop

            publishProgress(selectionSize);
            while (ticket < threads) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "After sleep failure", e);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded() && !isCancelled()) {
                mChapterAdapter.notifyDataSetChanged();
                if (selectionSize > 7 && chapter != null)
                    Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_MarkSelectedAsUnread, getString(R.string.marking_as_unread), chapter.getTitle(), true);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mChapterAdapter.notifyDataSetChanged();
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_MarkSelectedAsUnread);
        }

        @Override
        protected void onCancelled() {
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_MarkSelectedAsUnread);
        }
    }

    private class DeleteImages extends AsyncTask<Void, Integer, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private Chapter chapter;
        private int threads = Runtime.getRuntime().availableProcessors();
        private int ticket = threads;

        DeleteImages(ServerBase serverBase, int selectionSize) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            mNotifyID_DeleteImages = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (selectionSize > 7)
                Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_DeleteImages, getString(R.string.deleting), "");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            ticket = threads;
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                final int idxNow = i;
                while (ticket < 1) {
                    if (System.currentTimeMillis() - initTime > 250) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
                ticket--;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isAdded() && !isCancelled()) {
                                chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(idxNow));
                                chapter.freeSpace(getActivity(), mManga, serverBase);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        } finally {
                            ticket++;
                        }

                    }
                }).start();
            } //for loop

            publishProgress(selectionSize);
            while (ticket < threads) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "After sleep failure", e);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded() && !isCancelled()) {
                mChapterAdapter.notifyDataSetChanged();
                if (selectionSize > 7 && chapter != null)
                    Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_DeleteImages, getString(R.string.deleting), chapter.getTitle(), true);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mChapterAdapter.notifyDataSetChanged();
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_DeleteImages);
        }

        @Override
        protected void onCancelled() {
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_DeleteImages);
        }
    }

    private class RemoveChapters extends AsyncTask<Void, Integer, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private Chapter chapter;
        private ActionMode mode;
        private int j = 0;

        RemoveChapters(ServerBase serverBase, int selectionSize, ActionMode mode) {
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
                if (isAdded() && !isCancelled()) {
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
                    if (System.currentTimeMillis() - initTime > 250) {
                        publishProgress(j);
                        initTime = System.currentTimeMillis();
                    }
                }
            }
            publishProgress(selectionSize - 1);

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded() && !isCancelled() && chapter != null) {
                Util.getInstance().changeNotificationWithProgressbar(selectionSize - 1, values[0], mNotifyID_RemoveChapters, getString(R.string.removing_chapters), chapter.getTitle(), true);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isAdded())
                new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Util.getInstance().cancelNotification(mNotifyID_RemoveChapters);
        }

        @Override
        protected void onCancelled() {
            Util.getInstance().cancelNotification(mNotifyID_RemoveChapters);
        }

    }

    private class ResetChapters extends AsyncTask<Void, Integer, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private Chapter chapter;
        private int threads = Runtime.getRuntime().availableProcessors();
        private int ticket = threads;

        ResetChapters(ServerBase serverBase, int selectionSize) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            mNotifyID_ResetChapters = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (selectionSize > 7)
                Util.getInstance().createNotificationWithProgressbar(getContext(), mNotifyID_ResetChapters, getString(R.string.resetting_chapters), "");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            ticket = threads;
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < selectionSize; i++) {
                final int idxNow = i;
                while (ticket < 1) {
                    if (System.currentTimeMillis() - initTime > 250) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                }
                ticket--;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isAdded() && !isCancelled()) {
                                chapter = mChapterAdapter.getItem(mChapterAdapter.getSelection().keyAt(idxNow));
                                chapter.reset(getActivity(), mManga, serverBase);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        } finally {
                            ticket++;
                        }

                    }
                }).start();
            } //for loop

            publishProgress(selectionSize);
            while (ticket < threads) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "After sleep failure", e);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded() && !isCancelled()) {
                if (selectionSize > 7 && chapter != null)
                    Util.getInstance().changeNotificationWithProgressbar(selectionSize, values[0], mNotifyID_ResetChapters, getString(R.string.resetting_chapters), chapter.getTitle(), true);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isAdded())
                new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_ResetChapters);
        }

        @Override
        protected void onCancelled() {
            if (selectionSize > 7)
                Util.getInstance().cancelNotification(mNotifyID_ResetChapters);
        }
    }

}
