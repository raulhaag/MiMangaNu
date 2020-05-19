package ar.rulosoft.mimanganu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fedorvlasov.lazylist.ImageLoader;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.adapters.ChapterAdapter;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.ControlInfoNoScroll;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ReaderOptions;
import ar.rulosoft.mimanganu.componentes.Shortcuts;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.Paths;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

import static java.lang.Runtime.getRuntime;

public class MangaFragment extends Fragment implements MainActivity.OnBackListener {
    public static final String DIRECTION = "direcciondelectura";
    public static final String CHAPTERS_ORDER = "chapters_order";
    public static final String CHAPTER_ID = "cap_id";
    public static final String CHAPTERS_HIDE_READ = "hide_read_chapters";
    public static final String TAG = "MangaFragment";
    private SwipeRefreshLayout swipeReLayout;
    private Manga mManga;
    private int mMangaId;
    private SearchForNewChapters searchForNewChapters = new SearchForNewChapters();
    private RemoveChapters removeChapters = null;
    private ResetChapters resetChapters = null;
    private DeleteImages deleteImages = null;
    private MarkSelectedAsRead markSelectedAsRead = null;
    private MarkSelectedAsUnread markSelectedAsUnread = null;
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
    private int mNotifyID_RemoveChapters = (int) System.currentTimeMillis();
    private int mNotifyID_ResetChapters = (int) System.currentTimeMillis();
    private ReaderOptions readerOptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mMangaId = getArguments().getInt(MainFragment.MANGA_ID, -1);
        return inflater.inflate(R.layout.fragment_manga, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pm = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        super.onCreate(savedInstanceState);
        if (mMangaId == -1) {
            requireActivity().onBackPressed();
        }
        mManga = Database.getManga(requireActivity(), mMangaId);
        mServerBase = ServerBase.getServer(mManga.getServerId(), requireContext());
        if (getView() == null) {
            try {
                finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            mListView = getView().findViewById(R.id.list);
            swipeReLayout = getView().findViewById(R.id.str);
            readerOptions = getView().findViewById(R.id.reader_options);
            mImageLoader = new ImageLoader(requireActivity());
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
            mInfo = new ControlInfoNoScroll(requireActivity());
            mListView.addHeaderView(mInfo);
            mInfo.setColor(MainActivity.darkTheme, colors[0]);
            mInfo.enableTitleCopy(requireActivity(), mManga.getTitle());
            ChapterAdapter.setColor(MainActivity.darkTheme, colors[1], colors[0]);
            mListView.setOnItemClickListener((parent, view, position, id) -> {
                Chapter c = (Chapter) mListView.getAdapter().getItem(position);
                int first = mListView.getFirstVisiblePosition();
                Database.updateMangaLastIndex(requireActivity(), mManga.getId(), first);
                Bundle bundle = new Bundle();
                bundle.putInt(MangaFragment.CHAPTER_ID, c.getId());
                ReaderFragment readerFragment = new ReaderFragment();
                readerFragment.setArguments(bundle);
                ((MainActivity) requireActivity()).replaceFragment(readerFragment, "ReaderFragment");
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
                    MenuInflater inflater = requireActivity().getMenuInflater();
                    inflater.inflate(R.menu.listitem_capitulo_menu_cab, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(final android.view.ActionMode mode, MenuItem item) {
                    final SparseBooleanArray selection = mChapterAdapter.getSelection();
                    final ServerBase serverBase = ServerBase.getServer(mManga.getServerId(), requireContext());
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
                            Chapter[] cs = mChapterAdapter.getSelectedChapters();
                            markSelectedAsRead = new MarkSelectedAsRead();
                            markSelectedAsRead.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cs);
                            deleteImages = new DeleteImages(serverBase, selection.size());
                            deleteImages.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cs);
                            break;
                        case R.id.delete_images:
                            deleteImages = new DeleteImages(serverBase, selection.size());
                            deleteImages.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChapterAdapter.getSelectedChapters());
                            break;
                        case R.id.remove_chapter:
                            finish = false;
                            new AlertDialog.Builder(requireContext())
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
                                            removeChapters = new RemoveChapters(serverBase, selection.size(), mode);
                                            removeChapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChapterAdapter.getSelectedChapters());
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                            break;
                        case R.id.reset_chapter:
                            resetChapters = new ResetChapters(serverBase, selection.size());
                            resetChapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChapterAdapter.getSelectedChapters());
                            break;
                        case R.id.mark_selected_as_read:
                            markSelectedAsRead = new MarkSelectedAsRead();
                            markSelectedAsRead.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChapterAdapter.getSelectedChapters());
                            break;
                        case R.id.mark_selected_as_unread:
                            markSelectedAsUnread = new MarkSelectedAsUnread();
                            markSelectedAsUnread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChapterAdapter.getSelectedChapters());
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

            requireActivity().setTitle(mManga.getTitle());
            Database.updateMangaRead(requireContext(), mManga.getId());
            loadInfo(mManga);
            chapters_order = pm.getInt(CHAPTERS_ORDER, 1);
            hide_read = pm.getBoolean(CHAPTERS_HIDE_READ, false);
            Shortcuts.addShortCuts(mManga, requireContext());
        }
    }

    public void loadInfo(Manga manga) {
        if (mInfo != null && manga != null && isAdded()) {
            mInfo.setStatus(manga.isFinished() ? getResources().getString(R.string.finalizado) : getResources().getString(R.string.en_progreso));
            mInfo.setServer(ServerBase.getServer(manga.getServerId(), requireContext()).getServerName());

            if (manga.getSynopsis() != null) {
                mInfo.setSynopsis(manga.getSynopsis());
            } else {
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
            if (manga.getLastUpdate() != null) {
                mInfo.setLastUpdate(manga.getLastUpdate());
            } else {
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
            if (fvi != 0 && mListView != null) mListView.setSelection(fvi);
        }
    }

    @Override
    public void onPause() {
        if (mListView.getAdapter() != null) {
            int first = mListView.getFirstVisiblePosition();
            Database.updateMangaLastIndex(requireActivity(), mManga.getId(), first);
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
                requireActivity().onBackPressed();
                return true;
            case R.id.action_config_reader:
                readerOptions.switchOptions();
                break;
            case R.id.action_download_remaining: {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.download_remain_confirmation)
                        .setNegativeButton(getString(android.R.string.no), (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                            ArrayList<Chapter> chapters = Database.getChapters(requireContext(), mMangaId, Database.COL_CAP_DOWNLOADED + " != 1", true);
                            for (Chapter chapter : chapters) {
                                try {
                                    DownloadPoolService.addChapterDownloadPool(requireActivity(), chapter, false);
                                } catch (Exception e) {
                                    Log.e(TAG, "Download add pool error", e);
                                }
                            }
                            dialog.dismiss();
                        })
                        .show();
            }
            break;
            case R.id.mark_all_as_read: {
                Database.markAllChapters(requireContext(), this.mMangaId, true);
                new SetChaptersPageCountAsRead().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case R.id.mark_all_as_unread: {
                Database.markAllChapters(requireContext(), this.mMangaId, false);
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
                new AlertDialog.Builder(requireContext())
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
                                ServerBase serverBase = ServerBase.getServer(mManga.getServerId(), requireContext());
                                String path = Paths.generateBasePath(serverBase, mManga, requireActivity());
                                Util.getInstance().deleteRecursive(new File(path));
                                Database.deleteManga(requireActivity(), mManga.getId());
                                dialog.dismiss();
                                requireActivity().onBackPressed();
                            }
                        })
                        .show();
                break;
            }
            case R.id.action_view_download: {
                ((MainActivity) requireActivity()).replaceFragment(new DownloadsFragment(), "DownloadFragment");
                break;
            }
            case R.id.action_download_unread: {
                new AlertDialog.Builder(requireContext())
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
                                ArrayList<Chapter> chapters = Database.getChapters(requireActivity(), MangaFragment.this.mMangaId, Database.COL_CAP_STATE + " != 1" + " AND " + Database.COL_CAP_DOWNLOADED + " != 1", true);
                                for (Chapter c : chapters) {
                                    try {
                                        DownloadPoolService.addChapterDownloadPool(requireActivity(), c, false);
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
        ((MainActivity) requireActivity()).enableHomeButton(true);
        ((MainActivity) requireActivity()).setTitle(mManga.getTitle());
        Chapter.Comparators.setManga_title(mManga.getTitle());
        new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (mChapterAdapter != null) {
            DownloadPoolService.setStateChangeListener(mChapterAdapter);
        }
        mListView.setSelection(mManga.getLastIndex());
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manga, menu);
        int[] sortList = {
                R.id.sort_as_added_to_db_desc_chapters, R.id.sort_number,
                R.id.sort_number_asc, R.id.sort_title,
                R.id.sort_title_asc, R.id.sort_as_added_to_db_asc_chapters
        };
        menu.findItem(sortList[chapters_order]).setChecked(true);
        menu.findItem(R.id.action_hide_read).setChecked(hide_read);
    }

    @Override
    public boolean onBackPressed() {
        if (readerOptions.isVisible()) {
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
                    DownloadPoolService.addChapterDownloadPool(requireActivity(), chapter, false);
                } catch (Exception e) {
                    Log.e(TAG, "Download add pool error", e);
                }
            }
            return null;
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
            orgMsg = requireActivity().getTitle().toString();
            requireActivity().setTitle(msg + " " + orgMsg);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            int result = 0;
            ServerBase s = ServerBase.getServer(mManga.getServerId(), requireContext());
            mangaId = mManga.getId();
            try {
                int diff = s.searchForNewChapters(mManga.getId(), requireContext(), false);//always full update
                result += diff;
            } catch (Exception e) {
                error = Log.getStackTraceString(e);
                Log.e(TAG, "Exception", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Manga manga = Database.getManga(requireActivity(), mangaId);
            loadInfo(manga);
            new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            swipeReLayout.setRefreshing(false);
            if (isAdded()) {
                requireActivity().setTitle(orgMsg);
            }
            if (result > 0) {
                if (isAdded()) {
                    Util.getInstance().showFastSnackBar(getString(R.string.mgs_update_found, "" + result), getView(), requireContext());
                }
            } else if (!error.isEmpty()) {
                if (isAdded()) {
                    Util.getInstance().toast(requireContext(), error);
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
            if (hide_read) {
                condition = Database.COL_CAP_STATE + " != 1";
            }
            chapters = Database.getChapters(requireActivity(), mMangaId, condition);
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
                    assert chapter != null;
                    chapter.markRead(requireActivity(), false);
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
                    assert chapter != null;
                    chapter.markRead(requireActivity(), true);
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

    private class MarkSelectedAsRead extends AsyncTask<Chapter, Void, Void> {
        @Override
        protected Void doInBackground(Chapter... s) {
            StringBuilder sb = new StringBuilder("UPDATE ").append(Database.TABLE_CHAPTERS)
                    .append(" SET ").append(Database.COL_CAP_STATE).append(" = 1 ").append(", ")
                    .append(Database.COL_CAP_PAG_READ).append(" = ").append(Database.COL_CAP_PAGES)
                    .append(" WHERE ").append(Database.COL_CAP_ID).append(" IN (");
            for (Chapter c : s) {
                sb.append(c.getId()).append(",");
                c.setPagesRead(c.getPages());
                c.setReadStatus(1);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(");");
            Database.getDatabase(requireContext()).execSQL(sb.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mChapterAdapter.notifyDataSetChanged();
        }
    }

    private class MarkSelectedAsUnread extends AsyncTask<Chapter, Void, Void> {
        @Override
        protected Void doInBackground(Chapter... s) {
            StringBuilder sb = new StringBuilder("UPDATE ").append(Database.TABLE_CHAPTERS)
                    .append(" SET ").append(Database.COL_CAP_STATE).append(" = 0 ")
                    .append(", ").append(Database.COL_CAP_PAG_READ).append(" = 0 WHERE ")
                    .append(Database.COL_CAP_ID)
                    .append(" IN (");
            for (Chapter c : s) {
                sb.append(c.getId()).append(",");
                c.setPagesRead(0);
                c.setReadStatus(0);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(");");
            Database.getDatabase(requireContext()).execSQL(sb.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mChapterAdapter.notifyDataSetChanged();
        }
    }

    private class DeleteImages extends AsyncTask<Chapter, Chapter, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private int current = 0;
        private ExecutorService es;

        DeleteImages(ServerBase serverBase, int selectionSize) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (selectionSize > 7)
                Util.getInstance().createNotificationWithProgressbar(requireContext(), mNotifyID_DeleteImages, getString(R.string.deleting), "");
        }

        @Override
        protected Integer doInBackground(Chapter... params) {
            es = Executors.newFixedThreadPool(getRuntime().availableProcessors());
            for (Chapter c : params) {
                es.execute(() -> {
                    c.freeSpace(requireActivity(), mManga, serverBase);
                    publishProgress(c);
                });
            }
            es.shutdown();
            try {
                es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Chapter... values) {
            super.onProgressUpdate(values);
            current++;
            if (isAdded() && !isCancelled()) {
                if (selectionSize > 7 && values[0] != null)
                    Util.getInstance().changeNotificationWithProgressbar(selectionSize, current, mNotifyID_ResetChapters, getString(R.string.deleting), values[0].getTitle(), true);
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

    private class RemoveChapters extends AsyncTask<Chapter, Chapter, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private ActionMode mode;
        private int current = 0;
        private ExecutorService es;

        RemoveChapters(ServerBase serverBase, int selectionSize, ActionMode mode) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            this.mode = mode;
            mNotifyID_RemoveChapters = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getInstance().createNotificationWithProgressbar(requireContext(), mNotifyID_RemoveChapters, getString(R.string.removing_chapters), "");
        }

        @Override
        protected Integer doInBackground(Chapter... params) {
            es = Executors.newFixedThreadPool(getRuntime().availableProcessors());
            for (Chapter c : params) {
                es.execute(() -> {
                    c.delete(requireContext(), mManga, serverBase);
                    mChapterAdapter.onlyRemove(c);
                    publishProgress(c);
                });
            }
            es.shutdown();
            try {
                es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Chapter... values) {
            super.onProgressUpdate(values);
            current++;
            if (isAdded() && !isCancelled() && values[0] != null) {
                Util.getInstance().changeNotificationWithProgressbar(selectionSize - 1, current, mNotifyID_RemoveChapters, getString(R.string.removing_chapters), values[0].getTitle(), true);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isAdded())
                new SortAndLoadChapters().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Util.getInstance().cancelNotification(mNotifyID_RemoveChapters);
            mode.finish();
        }

        @Override
        protected void onCancelled() {
            Util.getInstance().cancelNotification(mNotifyID_RemoveChapters);
            es.shutdownNow();
        }

    }

    private class ResetChapters extends AsyncTask<Chapter, Chapter, Integer> {
        private ServerBase serverBase;
        private int selectionSize = 0;
        private int current = 0;
        private ExecutorService es;

        ResetChapters(ServerBase serverBase, int selectionSize) {
            this.serverBase = serverBase;
            this.selectionSize = selectionSize;
            mNotifyID_ResetChapters = (int) System.currentTimeMillis();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (selectionSize > 7)
                Util.getInstance().createNotificationWithProgressbar(requireContext(), mNotifyID_ResetChapters, getString(R.string.resetting_chapters), "");
        }

        @Override
        protected Integer doInBackground(Chapter... params) {
            es = Executors.newFixedThreadPool(getRuntime().availableProcessors());
            for (Chapter c : params) {
                es.execute(() -> {
                    c.reset(requireContext(), mManga, serverBase);
                    publishProgress(c);
                });
            }
            es.shutdown();
            try {
                es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Chapter... values) {
            super.onProgressUpdate(values);
            current++;
            if (isAdded() && !isCancelled()) {
                if (selectionSize > 7 && values[0] != null)
                    Util.getInstance().changeNotificationWithProgressbar(selectionSize, current, mNotifyID_ResetChapters, getString(R.string.resetting_chapters), values[0].getTitle(), true);
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
            es.shutdownNow();
        }
    }

}
