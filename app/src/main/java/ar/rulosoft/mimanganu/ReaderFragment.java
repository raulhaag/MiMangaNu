package ar.rulosoft.mimanganu;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ReaderOptions;
import ar.rulosoft.mimanganu.componentes.readers.Reader;
import ar.rulosoft.mimanganu.componentes.readers.Reader.Direction;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadListener;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.services.StateChangeListener;
import ar.rulosoft.mimanganu.utils.Paths;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

import static ar.rulosoft.mimanganu.componentes.ReaderOptions.BLUE_FILTER_CHECK;
import static ar.rulosoft.mimanganu.componentes.ReaderOptions.BLUE_FILTER_LEVEL;


public class ReaderFragment extends Fragment implements StateChangeListener, DownloadListener,
        SeekBar.OnSeekBarChangeListener, ChapterDownload.OnErrorListener, Reader.ReaderListener,
        MainActivity.OnKeyUpListener, MainActivity.OnBackListener, ReaderOptions.OptionListener {

    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    public static final String ORIENTATION = "orientation";
    public static final String ADJUST_KEY = "ajustar_a";
    private static final String MAX_TEXTURE = "max_texture";
    private static int mTextureMax;
    public Reader mReader;
    boolean updatedValue = false;//just a flag to no seek when the reader seek
    private Direction direction;
    // These are values, which should be fetched from preference
    private SharedPreferences pm;
    private float mScrollFactor = 1f;
    // These are layout components
    private RelativeLayout mControlsLayout;
    private SeekBar mSeekBar;
    private Toolbar mActionBar;
    private Chapter mChapter, nextChapter, previousChapter;
    private Manga mManga;
    private ServerBase mServerBase;
    private TextView pageTextView, mScrollSensitiveText;
    private ReaderOptions readerOptions;
    private Reader.Type readerType = Reader.Type.CONTINUOUS;
    private boolean controlVisible = false;
    private AlertDialog mDialog = null;
    private boolean reDownloadingImage, freshStart = true;
    private int reader_bg;
    private TextView titleTextView;
    private LinearLayout loadDialog;
    private GetPageTask getPageTask = new GetPageTask();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        int chapterId = getArguments().getInt(MangaFragment.CHAPTER_ID);
        if (savedInstanceState != null) {
            chapterId = savedInstanceState.getInt(MangaFragment.CHAPTER_ID);
        }
        mChapter = Database.getChapter(getActivity(), chapterId);
        if (mChapter == null) {
            Log.e("ERROR", "CAN'T LOAD CHAPTER");
            //can't get chapter
            return;
        }
        mManga = Database.getFullManga(getActivity(), mChapter.getMangaID());
        mServerBase = ServerBase.getServer(mManga.getServerId(), getContext());
        pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mTextureMax = Integer.parseInt(pm.getString(MAX_TEXTURE, "2048"));

        mScrollFactor = Float.parseFloat(pm.getString("scroll_speed", "1"));
        int intReaderType = pm.getBoolean("reader_type", true) ? 1 : 2;
        if (mManga.getReaderType() != 0) {
            intReaderType = mManga.getReaderType();
        }
        if (intReaderType != 2)
            readerType = Reader.Type.PAGED;
        if (mManga.getReadingDirection() != -1) {
            direction = Direction.values()[mManga.getReadingDirection()];
        } else {
            direction = Direction.values()[Integer.parseInt(pm.getString(MangaFragment.DIRECTION, "" + Direction.R2L.ordinal()))];
        }
        if (mManga.getScrollSensitive() > 0) {
            mScrollFactor = mManga.getScrollSensitive();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout scrollSelect;
        RelativeLayout seekerLayout;
        Button buttonMinus, buttonPlus;

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        mActionBar = view.findViewById(R.id.action_bar);
        mControlsLayout = view.findViewById(R.id.controls);
        pageTextView = view.findViewById(R.id.pages);
        titleTextView = view.findViewById(R.id.title);
        mSeekBar = view.findViewById(R.id.seeker);
        readerOptions = view.findViewById(R.id.reader_options);
        seekerLayout = view.findViewById(R.id.seeker_layout);
        scrollSelect = view.findViewById(R.id.scroll_selector);
        buttonMinus = view.findViewById(R.id.minus);
        buttonPlus = view.findViewById(R.id.plus);
        mScrollSensitiveText = view.findViewById(R.id.scroll_level);
        loadDialog = view.findViewById(R.id.loadDialog);
        reader_bg = ThemeColors.getReaderColor(pm);
        mActionBar.setTitleTextColor(Color.WHITE);
        mControlsLayout.setAlpha(0f);
        mControlsLayout.setVisibility(View.GONE);
        readerOptions.setOptionListener(ReaderFragment.this);
        readerOptions.setActivity(getActivity());
        readerOptions.setManga(mManga);
        readerOptions.setBackgroundColor(reader_bg);

        mScrollSensitiveText.setText(getString(R.string.factor_suffix, mScrollFactor));
        mActionBar.setBackgroundColor(reader_bg);
        seekerLayout.setBackgroundColor(reader_bg);
        mSeekBar.setBackgroundColor(reader_bg);
        scrollSelect.setBackgroundColor(reader_bg);

        if (pm.getBoolean("hide_sensitivity_scrollbar", false))
            scrollSelect.setVisibility(View.INVISIBLE);
        if (pm.getBoolean("hide_actionbar", false))
            mActionBar.setVisibility(View.INVISIBLE);


        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(-.25f);
            }
        });

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(.25f);
            }
        });
        mScrollSensitiveText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.scroll_speed), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mSeekBar.setOnSeekBarChangeListener(this);
        freshStart = true;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar ab = ((MainActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        if (!pm.getBoolean("show_status_bar", true)) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setNavigationBarColor(reader_bg);
            window.setStatusBarColor(reader_bg);
        }
        hideSystemUI();
        if (freshStart) {
            setReader();
            freshStart = false;
        }
        if (controlVisible) {
            controlVisible = false;
            onMenuRequired();//before rotate retry menu state (if showed)
        }
        // don't know why is needed to set again in every start ()
        initMenu();
    }

    private void setReader() {
        if (mReader != null) {
            mReader.freeMemory();
        }
        mReader = Reader.getNewReader(getActivity(), direction, readerType);
        if (direction == Direction.L2R) {
            mSeekBar.setRotation(180);
        } else {
            mSeekBar.setRotation(0);
        }
        mReader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        readerOptions.setReader(mReader);
        if (getView() != null) {
            ((FrameLayout) getView().findViewById(R.id.reader_placeholder)).removeAllViews();
            ((FrameLayout) getView().findViewById(R.id.reader_placeholder)).addView(mReader);
            mReader.setMaxTexture(mTextureMax);
            mReader.setScreenFit(readerOptions.getScreenFit());
            mReader.setReaderListener(this);
            mReader.setScrollSensitive(mScrollFactor);
            loadChapter(mChapter, LoadMode.SAVED);
        }

        if (pm.getBoolean(BLUE_FILTER_CHECK, false)) {
            mReader.setBlueFilter((100 - pm.getInt(BLUE_FILTER_LEVEL, 30)) / 100f);
        }
    }

    private void loadChapter(Chapter nChapter, LoadMode mode) {
        if (mChapter != null) {
            DownloadPoolService.detachListener(mChapter.getId());
            Database.updateChapter(getActivity(), mChapter);
        }
        mChapter = nChapter;
        if (mChapter == null) {
            return;
        }
        if (!mChapter.isDownloaded()) {
            try {
                DownloadPoolService.addChapterDownloadPool(getActivity(), mChapter, true);
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        titleTextView.setText(mChapter.getTitle());
        if (mChapter.getPages() == 0) {
            new GetPageTask().execute(mChapter);
        } else {
            if (mChapter.getPagesRead() == 0)
                mChapter.setPagesRead(1);
            DownloadPoolService.setDownloadListener(this);
            mChapter.setReadStatus(Chapter.READING);
            Database.updateChapter(getActivity(), mChapter);
            mReader.reset();
            ArrayList<String> pages = new ArrayList<>();
            if (!(mServerBase instanceof FromFolder)) {
                for (int i = 0; i < mChapter.getPages(); i++) {
                    pages.add(Paths.generateBasePath(mServerBase, mManga, mChapter, getActivity()) + "/" + (i + 1) + ".jpg");
                }
            } else {
                for (int i = 0; i < mChapter.getPages(); i++) {
                    try {
                        pages.add(mServerBase.getImageFrom(mChapter, i + 1));
                    } catch (Exception ignore) {
                    }
                }
            }
            mReader.setPaths(pages);
            mActionBar.setTitle(mManga.getTitle());
            mSeekBar.setMax(mChapter.getPages() - 1);
            DownloadPoolService.attachListener(this, mChapter.getId());
            boolean next = false;
            for (int i = 0; i < mManga.getChapters().size(); i++) {
                if (mManga.getChapters().get(i).getId() == mChapter.getId()) {
                    if (i > 0) {
                        next = true;
                        nextChapter = mManga.getChapters().get(i - 1);
                    }
                    if (i + 1 < mManga.getChapters().size()) {
                        previousChapter = mManga.getChapters().get(i + 1);
                    }
                    break;
                }
            }
            if (!next)
                nextChapter = null;
            if (nextChapter != null) {
                if (!nextChapter.isDownloaded()) {
                    if (pm.getBoolean("download_next_chapter_automatically", false)) {
                        if (DownloadPoolService.isNewDownload(nextChapter.getId())) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(getActivity(), nextChapter, false);
                                Util.getInstance().toast(getActivity(), getResources().getString(R.string.downloading) + " " + nextChapter.getTitle());
                            } catch (Exception e) {
                                Log.e("ServB", "Download add pool error", e);
                            }
                        }
                    }
                }
            }
            switch (mode) {
                case START:
                    mReader.seekPage(1);
                    break;
                case END:
                    mReader.seekPage(mChapter.getPages());
                    break;
                case SAVED:
                    if (mChapter.getPagesRead() == 0)
                        mChapter.setPagesRead(1);
                    mReader.seekPage(mChapter.getPagesRead());
            }
        }
        mReader.postInvalidateDelayed(200);
    }

    private void modScrollSensitive(float diff) {
        if ((mScrollFactor + diff) >= .5 && (mScrollFactor + diff) <= 5) {
            mScrollFactor += diff;
            Database.updateMangaScrollSensitive(getActivity(), mManga.getId(), mScrollFactor);
            mScrollSensitiveText.setText(getString(R.string.factor_suffix, mScrollFactor));
            mReader.setScrollSensitive(mScrollFactor);
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void initMenu() {
        mActionBar.getMenu().clear();
        mActionBar.inflateMenu(R.menu.menu_reader);
        mActionBar.setNavigationIcon(R.drawable.ic_back);
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mActionBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return ReaderFragment.this.onMenuItemClick(item);
            }
        });
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.re_download_image:
                if (!reDownloadingImage)
                    reDownloadCurrentImage();
                else
                    Util.getInstance().toast(getActivity(), getString(R.string.dont_spam_redownload_button));
                break;
            case R.id.action_config_reader:
                readerOptions.switchOptions();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageDownloaded(int cid, int page) {
        if (cid == mChapter.getId())
            mReader.reloadImage(page);
    }

    @Override
    public void onPause() {
        try {
            if (mDialog != null)
                mDialog.dismiss();
            DownloadPoolService.setDownloadListener(null);
            DownloadPoolService.detachListener(mChapter.getId());
            Database.updateChapter(getActivity(), mChapter);
            if (mReader.isLastPageVisible()) {
                mChapter.setPagesRead(mChapter.getPages());
                mChapter.setReadStatus(Chapter.READ);
                Database.updateChapter(getActivity(), mChapter);
            } else {
                mChapter.setPagesRead(mReader.getCurrentPage());
                Database.updateChapterPage(getActivity(), mChapter.getId(), mChapter.getPagesRead());
            }
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ActionBar ab = ((MainActivity) getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.show();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(MangaFragment.CHAPTER_ID, mChapter.getId());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (readerOptions != null) {
            readerOptions.startStopBrightLevel(false);
        }
        if (mReader != null) {
            mReader.freeMemory();
        }
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        DownloadPoolService.attachListener(this, mChapter.getId());
        DownloadPoolService.setDownloadListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setNavigationBarColor(reader_bg);
            window.setStatusBarColor(reader_bg);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (pageTextView != null)
            pageTextView.setText(String.format(Locale.getDefault(), "%d / %d", seekBar.getProgress() + 1, mChapter.getPages()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        pageTextView.setText(String.format(Locale.getDefault(), "%d / %d", seekBar.getProgress() + 1, mChapter.getPages()));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int pm = seekBar.getProgress();
        mReader.goToPage(pm + 1);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onMenuRequired();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (readerOptions.isVisible()) {
            readerOptions.switchOptions();
            return true;
        }
        ((MainActivity) getActivity()).setColorToBars();
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        return false;
    }

    @Override
    public void onMenuRequired() {
        if (controlVisible) {
            hideSystemUI();
            controlVisible = false;
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(mControlsLayout, "alpha", .90f, 0f);
            anim2.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mControlsLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            anim2.start();
        } else {
            showSystemUI();
            controlVisible = true;
            ObjectAnimator anim2 =
                    ObjectAnimator.ofFloat(mControlsLayout, "alpha", 0f, .90f);
            anim2.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mControlsLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            anim2.start();
        }
    }

    @Override
    public void onError(final Chapter chapter) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(chapter.getTitle() + " " + getString(R.string.error))
                            .setMessage(getString(R.string.demaciados_errores))
                            .setIcon(R.mipmap.ic_launcher)
                            .setNeutralButton(getString(android.R.string.ok), null)
                            .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DownloadPoolService.retryError(getActivity(), mChapter, ReaderFragment.this);
                                    dialog.dismiss();
                                    mDialog = null;
                                    DownloadPoolService.setDownloadListener(ReaderFragment.this);
                                }
                            })
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                    // lost references fixed con detachListener
                }
            }
        });

    }

    public void onPageChanged(int page) {
        updatedValue = true;
        mChapter.setPagesRead(page);
        mSeekBar.setProgress(page - 1);
        if (mReader.isLastPageVisible()) {
            mChapter.setPagesRead(mChapter.getPages());
            mChapter.setReadStatus(Chapter.READ);
        } else if (mChapter.getReadStatus() == Chapter.READ) {
            mChapter.setReadStatus(Chapter.READING);
        }
        /*  TODO need revision of next tree lines
        if (mChapter.isDownloaded() && !new File(mReader.getPath(page)).exists()) {
            reDownloadCurrentImage();
        }*/
    }

    @Override
    public void onStartOver() {
        if (previousChapter != null) {
            boolean seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
            if (seamlessChapterTransition) {
                mChapter.setReadStatus(Chapter.UNREAD);
                mChapter.setPagesRead(1);
                loadChapter(previousChapter, LoadMode.END);
                Util.getInstance().showSlowSnackBar(mChapter.getTitle(), mControlsLayout, getActivity());
            }
        }
    }

    @Override
    public void onEndOver() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        boolean imagesDelete = pm.getBoolean("delete_images", false);
        boolean seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
        boolean seamlessChapterTransitionDeleteRead = pm.getBoolean("seamless_chapter_transitions_delete_read", false);
        if (nextChapter != null) {
            if (!seamlessChapterTransition) {
                View v = inflater.inflate(R.layout.dialog_next_chapter, null);
                final CheckBox checkBox = v.findViewById(R.id.delete_images_oc);
                checkBox.setChecked(imagesDelete);
                mDialog = new AlertDialog.Builder(getActivity())

                        .setTitle(getString(R.string.finished_reading, mChapter.getTitle()))
                        .setView(v)
                        .setIcon(R.mipmap.ic_launcher)
                        .setNegativeButton(getString(R.string.back), null)
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean del_images = checkBox.isChecked();
                                if (pm != null)
                                    pm.edit().putBoolean("delete_images", del_images).apply();
                                mChapter.setReadStatus(Chapter.READ);
                                mChapter.setPagesRead(mChapter.getPages());
                                Database.updateChapter(getActivity(), mChapter);
                                Chapter pChapter = mChapter;
                                loadChapter(nextChapter, LoadMode.START);
                                if (del_images) {
                                    pChapter.freeSpace(getActivity());
                                }
                                if (mDialog != null && mDialog.isShowing())
                                    mDialog.dismiss();
                                mDialog = null;
                            }
                        })
                        .setNeutralButton(R.string.dont_ask_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean del_images = checkBox.isChecked();
                                if (pm != null)
                                    pm.edit().putBoolean("delete_images", del_images).apply();
                                mChapter.setReadStatus(Chapter.READ);
                                mChapter.setPagesRead(mChapter.getPages());
                                Database.updateChapter(getActivity(), mChapter);
                                Chapter pChapter = mChapter;
                                loadChapter(nextChapter, LoadMode.START);
                                if (del_images) {
                                    pChapter.freeSpace(getActivity());
                                }
                                if (mDialog != null && mDialog.isShowing())
                                    mDialog.dismiss();
                                mDialog = null;
                                pm.edit().putBoolean("seamless_chapter_transitions", true).apply();
                            }
                        })
                        .show();
            } else {
                mChapter.setReadStatus(Chapter.READ);
                mChapter.setPagesRead(mChapter.getPages());
                Chapter tmpChapter = mChapter;
                nextChapter = Database.getChapter(getContext(), nextChapter.getId()); //update chapter info (if was downloaded etc)
                loadChapter(nextChapter, LoadMode.START);
                //Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);
                Util.getInstance().showSlowSnackBar(mChapter.getTitle(), mControlsLayout, getActivity());
                if (seamlessChapterTransitionDeleteRead) {
                    tmpChapter.freeSpace(getActivity());
                    Util.getInstance().toast(getActivity(), getResources().getString(R.string.deleted, tmpChapter.getTitle()), 0);
                }
            }
        } else {
            final Chapter tmpChapter = mChapter;
            View v = inflater.inflate(R.layout.dialog_no_more_chapters, null);
            final CheckBox checkBox = v.findViewById(R.id.delete_images_oc);
            checkBox.setChecked(imagesDelete);
            mDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.finished_reading, mManga.getTitle()))
                    .setView(v)
                    .setIcon(R.mipmap.ic_launcher)
                    .setNegativeButton(R.string.back, null)
                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean del_images = checkBox.isChecked();
                            if (pm != null)
                                pm.edit().putBoolean("delete_images", del_images).apply();
                            if (del_images) {
                                try {
                                    tmpChapter.freeSpace(getActivity());
                                } catch (Exception e) {
                                    //TODO catch context lost if user close while freeSpace is working (need to add a little service for this)
                                }
                            }
                            if (mDialog != null)
                                mDialog.dismiss();
                            controlVisible = true; //just to close
                            mDialog = null;
                            if (getActivity() != null)
                                getActivity().onBackPressed();
                        }
                    })
                    .show();

        }
    }

    @Override
    public void onChange(final SingleDownload singleDownload) {
        final Activity activity = getActivity();
        if (activity != null) {
            mReader.reloadImage(singleDownload.getIndex());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mReader.invalidate();
                    if (singleDownload.status.ordinal() > SingleDownload.Status.DOWNLOAD_OK.ordinal()) {
                        Toast.makeText(activity, R.string.error_downloading_image, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onStatusChanged(ChapterDownload chapterDownload) {
        //can be used to inform download complete but i think is no needed
    }

    public void reDownloadCurrentImage() {
        ReDownloadImage r = new ReDownloadImage();
        r.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onOptionChange(ReaderOptions.OptionType optionType) {
        switch (optionType) {
            case TYPE:
                readerType = readerOptions.getReaderType();
                setReader();
                break;
            case ADJUST:
                mReader.setScreenFit(readerOptions.getScreenFit());
                break;
            case DIRECTION:
                direction = readerOptions.getDirection();
                setReader();
                break;
        }
    }

    private enum LoadMode {START, END, SAVED}

    private class GetPageTask extends AsyncTask<Chapter, Void, Chapter> {
        String error;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (loadDialog != null) {
                loadDialog.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Chapter doInBackground(Chapter... arg0) {
            Chapter c = arg0[0];
            ServerBase s = ServerBase.getServer(ReaderFragment.this.mManga.getServerId(), getContext());
            try {
                if (c.getPages() < 1) {
                    s.chapterInit(c);
                    Database.updateChapter(getContext(), c);
                }
            } catch (Exception e) {
                if (e.getMessage() != null)
                    error = e.getMessage();
                else
                    error = e.getLocalizedMessage();
            }
            if (c.getPages() < 1) {
                error = "Error"; // using a localized string can cause errors
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (this.getStatus() == Status.RUNNING && loadDialog != null) {
                loadDialog.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(Chapter result) {
            super.onPostExecute(result);
            if (error != null && error.length() > 1) {
                Util.getInstance().toast(getActivity(), error);
            } else {
                try {
                    Database.updateChapter(getActivity(), result);
                    DownloadPoolService.addChapterDownloadPool(getActivity(), result, true);
                    loadChapter(result, LoadMode.SAVED);
                } catch (Exception e) {
                    Log.e("ReaderFragment", "Exception", e);
                    //Toast.makeText(getContext(), Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
                }
            }
            if (loadDialog != null) {
                loadDialog.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ReDownloadImage extends AsyncTask<Void, Void, Void> {
        int idx;
        String path;
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reDownloadingImage = true;
            idx = mReader.getCurrentPage();
            mReader.freePage(idx);
            path = mReader.getPath(idx);
            File f = new File(path);
            if (f.exists()) {
                if (!f.delete()) {
                    Log.e("ReaderFragment", "failed to delete '" + path + "' before re-downloading.");
                }
            }
            mReader.reloadImage(idx);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SingleDownload s = new SingleDownload(getContext(), mServerBase.getImageFrom(mChapter, idx), path, idx, mChapter.getId(), new ChapterDownload(mChapter), mServerBase.needRefererForImages());
                s.setChangeListener(ReaderFragment.this);
                new Thread(s).start();
            } catch (Exception e) {
                error = Log.getStackTraceString(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!error.isEmpty()) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }
            reDownloadingImage = false;
        }
    }
}
