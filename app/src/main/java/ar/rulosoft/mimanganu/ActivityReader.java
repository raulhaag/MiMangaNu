package ar.rulosoft.mimanganu;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.readers.Reader;
import ar.rulosoft.mimanganu.componentes.readers.Reader.Direction;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadListener;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.services.StateChangeListener;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

public class ActivityReader extends AppCompatActivity implements StateChangeListener, DownloadListener, SeekBar.OnSeekBarChangeListener, ChapterDownload.OnErrorListener, Reader.ReaderListener {

    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String ORIENTATION = "orientation";
    private static final String MAX_TEXTURE = "max_texture";
    private static final String ADJUST_KEY = "ajustar_a";
    private static int mTextureMax;
    private static DisplayType mScreenFit;
    public Reader mReader;
    boolean updatedValue = false;//just a flag to no seek when the reader seek
    FrameLayout rLayout = null;
    private Direction direction;
    // These are values, which should be fetched from preference
    private SharedPreferences pm;
    private boolean mKeepOn; // false = normal  | true = screen on
    private int mOrientation; // 0 = free | 1 = landscape | 2 = portrait
    private float mScrollFactor = 1f;
    // These are layout components
    private RelativeLayout mControlsLayout, mScrollSelect;
    private LinearLayout mSeekerLayout;
    private SeekBar mSeekBar;
    private Toolbar mActionBar;
    private Chapter mChapter, nextChapter, previousChapter;
    private Manga mManga;
    private ServerBase mServerBase;
    private TextView mSeekerPage, mScrollSensitiveText;
    private MenuItem keepOnMenuItem, screenRotationMenuItem;
    private Button mButtonMinus, mButtonPlus;
    private Reader.Type readerType = Reader.Type.CONTINUOUS;
    private boolean controlVisible = false;
    private MenuItem displayMenu;
    private AlertDialog mDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        int chapterId = getIntent().getExtras().getInt(MangaFragment.CHAPTER_ID);
        if (savedInstanceState != null) {
            chapterId = savedInstanceState.getInt(MangaFragment.CHAPTER_ID);
        }
        mChapter = Database.getChapter(this, chapterId);
        mManga = Database.getFullManga(this, mChapter.getMangaID());
        pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mScreenFit = DisplayType.valueOf(pm.getString(ADJUST_KEY, ImageViewTouchBase.DisplayType.FIT_TO_WIDTH.toString()));
        mTextureMax = Integer.parseInt(pm.getString(MAX_TEXTURE, "2048"));
        mOrientation = pm.getInt(ORIENTATION, 0);
        mKeepOn = pm.getBoolean(KEEP_SCREEN_ON, false);
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mServerBase = ServerBase.getServer(mManga.getServerId());
        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        mActionBar.setTitleTextColor(Color.WHITE);
        if (pm.getBoolean("show_status_bar", false)) {
            rLayout = (FrameLayout) findViewById(R.id.parent);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, (int) (24 * getResources().getDisplayMetrics().density), 0, 0);
            rLayout.setLayoutParams(lp);
        }
        mControlsLayout = (RelativeLayout) findViewById(R.id.controls);
        mControlsLayout.setAlpha(0f);
        mControlsLayout.setVisibility(View.GONE);

        mSeekerPage = (TextView) findViewById(R.id.page);
        mSeekerPage.setAlpha(.9f);
        mSeekerPage.setTextColor(Color.WHITE);

        mSeekBar = (SeekBar) findViewById(R.id.seeker);
        mSeekerLayout = (LinearLayout) findViewById(R.id.seeker_layout);
        mScrollSelect = (RelativeLayout) findViewById(R.id.scroll_selector);
        mButtonMinus = (Button) findViewById(R.id.minus);
        mButtonPlus = (Button) findViewById(R.id.plus);
        mScrollSensitiveText = (TextView) findViewById(R.id.scroll_level);

        mScrollSensitiveText.setText(mScrollFactor + "x");
        int reader_bg = ThemeColors.getReaderColor(pm);
        mActionBar.setBackgroundColor(reader_bg);
        mSeekerLayout.setBackgroundColor(reader_bg);
        mSeekerPage.setBackgroundColor(reader_bg);
        mSeekBar.setBackgroundColor(reader_bg);
        mScrollSelect.setBackgroundColor(reader_bg);

        if (pm.getBoolean("hide_sensitivity_scrollbar", false))
            mScrollSelect.setVisibility(View.INVISIBLE);
        if (pm.getBoolean("hide_actionbar", false))
            mActionBar.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(reader_bg);
            window.setStatusBarColor(reader_bg);
        }

        setSupportActionBar(mActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hideSystemUI();

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(-.25f);
            }
        });

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(.25f);
            }
        });
        mScrollSensitiveText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ActivityReader.this, getString(R.string.scroll_speed), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mSeekBar.setOnSeekBarChangeListener(this);
        setReader();
    }

    private void setReader() {
        if (mReader != null) {
            mReader.freeMemory();
        }
        mReader = Reader.getNewReader(getApplicationContext(), direction, readerType);
        if (direction == Direction.L2R) {
            mSeekBar.setRotation(180);
        } else {
            mSeekBar.setRotation(0);
        }
        mReader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.reader_placeholder)).removeAllViews();
        ((FrameLayout) findViewById(R.id.reader_placeholder)).addView(mReader);
        mReader.setMaxTexture(mTextureMax);
        mReader.setScreenFit(mScreenFit);
        mReader.setReaderListener(this);
        mReader.setScrollSensitive(mScrollFactor);
        if (displayMenu != null)
            if (mReader.hasFitFeature()) {
                displayMenu.setVisible(true);
            } else {
                displayMenu.setVisible(false);
            }
        loadChapter(mChapter, LoadMode.SAVED);
    }

    private void loadChapter(Chapter nChapter, LoadMode mode) {
        if (mChapter != null) {
            DownloadPoolService.detachListener(mChapter.getId());
            Database.updateChapter(ActivityReader.this, mChapter);
        }
        mChapter = nChapter;
        if (!mChapter.isDownloaded()) {
            try {
                DownloadPoolService.addChapterDownloadPool(ActivityReader.this, mChapter, true);
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Toast.makeText(ActivityReader.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        setTitle(mChapter.getTitle());
        if (mChapter.getPages() == 0) {
            new GetPageTask().execute(mChapter);
        } else {
            DownloadPoolService.setDownloadListener(this);
            mChapter.setReadStatus(Chapter.READING);
            Database.updateChapter(ActivityReader.this, mChapter);
            mReader.reset();
            ArrayList<String> pages = new ArrayList<>();
            if (!(mServerBase instanceof FromFolder)) {
                for (int i = 0; i < mChapter.getPages(); i++) {
                    pages.add(DownloadPoolService.generateBasePath(mServerBase, mManga, mChapter, getApplicationContext()) + "/" + (i + 1) + ".jpg");
                }
            } else {
                for (int i = 0; i < mChapter.getPages(); i++) {
                    try {
                        pages.add(mServerBase.getImageFrom(mChapter, i));
                    } catch (Exception ignore) {
                    }
                }
            }
            mReader.setPaths(pages);
            mActionBar.setTitle(mChapter.getTitle());
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
                        try {
                            DownloadPoolService.addChapterDownloadPool(this, nextChapter, false);
                            Util.getInstance().toast(this, getResources().getString(R.string.downloading) + " " + nextChapter.getTitle());
                        } catch (Exception e) {
                            Log.e("ServB", "Download add pool error", e);
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
                    mReader.seekPage(mChapter.getPagesRead());
            }
        }
        mReader.postInvalidateDelayed(200);
    }

    private void modScrollSensitive(float diff) {
        if ((mScrollFactor + diff) >= .5 && (mScrollFactor + diff) <= 5) {
            mScrollFactor += diff;
            Database.updateMangaScrollSensitive(ActivityReader.this, mManga.getId(), mScrollFactor);
            mScrollSensitiveText.setText(mScrollFactor + "x");
            mReader.setScrollSensitive(mScrollFactor);
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(pm.getBoolean("show_status_bar", false)) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
        }
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
        displayMenu = menu.findItem(R.id.action_ajustar);
        keepOnMenuItem = menu.findItem(R.id.action_keep_screen_on);
        screenRotationMenuItem = menu.findItem(R.id.action_orientation);
        if (mKeepOn) {
            keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_on);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mOrientation == 1) {
            ActivityReader.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_landscape);
        } else if (mOrientation == 2) {
            ActivityReader.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_portrait);
        }
        MenuItem mMenuItem = menu.findItem(R.id.action_sentido);

        if (direction == Direction.R2L) {
            this.direction = Direction.R2L;
            mMenuItem.setIcon(R.drawable.ic_action_clasico);
        } else if (direction == Direction.L2R) {
            this.direction = Direction.L2R;
            mMenuItem.setIcon(R.drawable.ic_action_inverso);
        } else {
            this.direction = Direction.VERTICAL;
            mMenuItem.setIcon(R.drawable.ic_action_verical);
        }
        if (mReader != null)
            if (mReader.hasFitFeature()) {
                displayMenu.setVisible(true);
            } else {
                displayMenu.setVisible(false);
            }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_ajustar: {
                mScreenFit = mScreenFit.getNext();
                SharedPreferences.Editor editor = pm.edit();
                editor.putString(ADJUST_KEY, mScreenFit.toString());
                editor.apply();
                mReader.setScreenFit(mScreenFit);
                updateIcon(mScreenFit, true);
                return true;
            }
            case R.id.action_keep_screen_on: {
                if (!mKeepOn) {
                    keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_on);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Toast.makeText(getApplicationContext(), getString(R.string.stay_awake_on),
                            Toast.LENGTH_SHORT).show();
                } else {
                    keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_off);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Toast.makeText(getApplicationContext(), getString(R.string.stay_awake_off),
                            Toast.LENGTH_SHORT).show();
                }
                mKeepOn = !mKeepOn;

                SharedPreferences.Editor editor = pm.edit();
                editor.putBoolean(KEEP_SCREEN_ON, mKeepOn);
                editor.apply();
                break;
            }
            case R.id.action_orientation: {
                if (mOrientation == 0) {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_landscape);
                    Toast.makeText(getApplicationContext(), getString(R.string.lock_on_landscape),
                            Toast.LENGTH_SHORT).show();
                } else if (mOrientation == 1) {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_portrait);
                    Toast.makeText(getApplicationContext(), getString(R.string.lock_on_portrait),
                            Toast.LENGTH_SHORT).show();
                } else if (mOrientation == 2) {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_free);
                    Toast.makeText(getApplicationContext(), getString(R.string.rotation_no_locked),
                            Toast.LENGTH_SHORT).show();
                }
                mOrientation = (mOrientation + 1) % 3;
                SharedPreferences.Editor editor = pm.edit();
                editor.putInt(ORIENTATION, mOrientation);
                editor.apply();
                break;
            }
            case R.id.action_sentido: {
                if (direction == Direction.R2L) {
                    item.setIcon(R.drawable.ic_action_inverso);
                    this.direction = Direction.L2R;
                } else if (direction == Direction.L2R) {
                    item.setIcon(R.drawable.ic_action_verical);
                    this.direction = Direction.VERTICAL;
                } else {
                    item.setIcon(R.drawable.ic_action_clasico);
                    this.direction = Direction.R2L;
                }
                mManga.setReadingDirection(this.direction.ordinal());
                Database.updateReadOrder(ActivityReader.this, this.direction.ordinal(), mManga.getId());
                setReader();
                break;
            }
            case R.id.re_download_image:
                reDownloadCurrentImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageDownloaded(int cid, int page) {
        if (cid == mChapter.getId())
            mReader.reloadImage(page + 1);
    }

    @Override
    protected void onPause() {
        try {
            if (mDialog != null)
                mDialog.dismiss();
            DownloadPoolService.setDownloadListener(null);
            DownloadPoolService.detachListener(mChapter.getId());
            Database.updateChapter(ActivityReader.this, mChapter);
            if (mReader.isLastPageVisible()) {
                mChapter.setPagesRead(mChapter.getPages());
                mChapter.setReadStatus(Chapter.READ);
                Database.updateChapter(ActivityReader.this, mChapter);
            } else {
                Database.updateChapterPage(ActivityReader.this, mChapter.getId(), mReader.getCurrentPage());
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
    protected void onDestroy() {
        mReader.freeMemory();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadPoolService.attachListener(this, mChapter.getId());
        DownloadPoolService.setDownloadListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mSeekerPage != null) mSeekerPage.setText("" + (progress + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeekerPage.setText("" + (seekBar.getProgress() + 1));
        mSeekerPage.setVisibility(SeekBar.VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeekerPage.setVisibility(SeekBar.INVISIBLE);
        int pm = seekBar.getProgress();
        mReader.goToPage(pm + 1);//start on 0
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onMenuRequired();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        ActivityReader.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDialog = new AlertDialog.Builder(ActivityReader.this)
                            .setTitle(chapter.getTitle() + " " + getString(R.string.error))
                            .setMessage(getString(R.string.demaciados_errores))
                            .setIcon(R.drawable.ic_launcher)
                            .setNeutralButton(getString(android.R.string.ok), null)
                            .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DownloadPoolService.retryError(getApplicationContext(), mChapter, ActivityReader.this);
                                    dialog.dismiss();
                                    mDialog = null;
                                    DownloadPoolService.setDownloadListener(ActivityReader.this);
                                }
                            })
                            .show();
                } catch (Exception e) {
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
    }

    @Override
    public void onStartOver() {
        if (previousChapter != null) {
            boolean seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
            if (seamlessChapterTransition) {
                mChapter.setReadStatus(Chapter.UNREAD);
                mChapter.setPagesRead(1);
                loadChapter(previousChapter, LoadMode.END);
                //Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);
            }
        }
    }

    @Override
    public void onEndOver() {
        LayoutInflater inflater = getLayoutInflater();
        boolean imagesDelete = pm.getBoolean("delete_images", false);
        boolean seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
        boolean seamlessChapterTransitionDeleteRead = pm.getBoolean("seamless_chapter_transitions_delete_read", false);
        if (nextChapter != null) {
            if (!seamlessChapterTransition) {
                View v = inflater.inflate(R.layout.dialog_next_chapter, null);
                final CheckBox checkBox = (CheckBox) v.findViewById(R.id.delete_images_oc);
                checkBox.setChecked(imagesDelete);
                mDialog = new AlertDialog.Builder(ActivityReader.this)
                        .setTitle(mChapter.getTitle() + " " + getString(R.string.finalizado))
                        .setView(v)
                        .setIcon(R.drawable.ic_launcher)
                        .setNegativeButton(getString(android.R.string.no), null)
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean del_images = checkBox.isChecked();
                                if (pm != null)
                                    pm.edit().putBoolean("delete_images", del_images).apply();
                                mChapter.setReadStatus(Chapter.READ);
                                mChapter.setPagesRead(mChapter.getPages());
                                Database.updateChapter(ActivityReader.this, mChapter);
                                Chapter pChapter = mChapter;
                                loadChapter(nextChapter, LoadMode.START);
                                if (del_images) {
                                    pChapter.freeSpace(ActivityReader.this);
                                }
                                if (mDialog != null && mDialog.isShowing())
                                    mDialog.dismiss();
                                mDialog = null;
                            }
                        })
                        .show();
            } else {
                mChapter.setReadStatus(Chapter.READ);
                mChapter.setPagesRead(mChapter.getPages());
                Chapter tmpChapter = mChapter;
                loadChapter(nextChapter, LoadMode.START);
                //Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);
                Util.getInstance().showSlowSnackBar(mChapter.getTitle(), mControlsLayout, getApplicationContext());
                if (seamlessChapterTransitionDeleteRead) {
                    tmpChapter.freeSpace(ActivityReader.this);
                    Util.getInstance().toast(getApplicationContext(), getResources().getString(R.string.deleted, tmpChapter.getTitle()), 0);
                }
            }
        } else {
            final Chapter tmpChapter = mChapter;
            View v = inflater.inflate(R.layout.dialog_no_more_chapters, null);
            final CheckBox checkBox = (CheckBox) v.findViewById(R.id.delete_images_oc);
            checkBox.setChecked(imagesDelete);
            mDialog = new AlertDialog.Builder(ActivityReader.this)
                    .setTitle(mChapter.getTitle() + " " + getString(R.string.finalizado))
                    .setView(v)
                    .setIcon(R.drawable.ic_launcher)
                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean del_images = checkBox.isChecked();
                            if (pm != null)
                                pm.edit().putBoolean("delete_images", del_images).apply();
                            if (del_images) {
                                tmpChapter.freeSpace(ActivityReader.this);
                            }
                            mDialog.dismiss();
                            mDialog = null;
                            onBackPressed();
                        }
                    })
                    .show();

        }
    }

    @Override
    public void onChange(final SingleDownload singleDownload) {
        if (singleDownload.status == SingleDownload.Status.DOWNLOAD_OK) {
            mReader.reloadImage(singleDownload.getIndex());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mReader.invalidate();
                }
            });
        } else {
            if (singleDownload.status.ordinal() > SingleDownload.Status.DOWNLOAD_OK.ordinal()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ActivityReader.this, R.string.error_downloading_image, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    public void reDownloadCurrentImage() {
        ReDownloadImage r = new ReDownloadImage();
        r.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateIcon(DisplayType displayType, boolean showMsg) {
        if (displayMenu != null) {
            String msg = "";
            switch (displayType) {
                case NONE:
                    displayMenu.setIcon(R.drawable.ic_action_original);
                    msg = getString(R.string.no_scale);
                    break;
                case FIT_TO_HEIGHT:
                    displayMenu.setIcon(R.drawable.ic_action_ajustar_alto);
                    msg = getString(R.string.ajuste_alto);
                    break;
                case FIT_TO_WIDTH:
                    displayMenu.setIcon(R.drawable.ic_action_ajustar_ancho);
                    msg = getString(R.string.ajuste_ancho);
                    break;
                case FIT_TO_SCREEN:
                    displayMenu.setIcon(R.drawable.ic_action_ajustar_diagonal);
                    msg = getString(R.string.mejor_ajuste);
                    break;
                default:
                    break;
            }
            if (showMsg)
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

        }
    }

    enum LoadMode {START, END, SAVED}

    public class GetPageTask extends AsyncTask<Chapter, Void, Chapter> {
        ProgressDialog asyncDialog = new ProgressDialog(ActivityReader.this);
        String error;

        @Override
        protected void onPreExecute() {
            asyncDialog.setMessage(getResources().getString(R.string.iniciando));
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Chapter doInBackground(Chapter... arg0) {
            Chapter c = arg0[0];
            ServerBase s = ServerBase.getServer(ActivityReader.this.mManga.getServerId());
            try {
                if (c.getPages() < 1) s.chapterInit(c);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    error = e.getMessage();
                else
                    error = e.getLocalizedMessage();
            }
            if (c.getPages() < 1) {
                error = getString(R.string.error);
            }
            return c;
        }

        @Override
        protected void onPostExecute(Chapter result) {
            try {
                asyncDialog.dismiss();
            } catch (Exception e) {
                // ignore error
            }
            if (error != null && error.length() > 1) {
                Toast.makeText(ActivityReader.this, error, Toast.LENGTH_LONG).show();
            } else {
                try {
                    Database.updateChapter(ActivityReader.this, result);
                    DownloadPoolService.addChapterDownloadPool(ActivityReader.this, result, true);
                    loadChapter(result, LoadMode.SAVED);
                } catch (Exception e) {
                    Toast.makeText(ActivityReader.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            super.onPostExecute(result);
        }
    }

    public class ReDownloadImage extends AsyncTask<Void, Void, Void> {
        int idx;
        String path;
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!isDestroyed())
                mActionBar.setVisibility(View.INVISIBLE);
            idx = mReader.getCurrentPage();
            mReader.freePage(idx);
            path = mReader.getPath(idx);
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            mReader.reloadImage(idx);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SingleDownload s = new SingleDownload(mServerBase.getImageFrom(mChapter, idx), path, idx, mChapter.getId(), new ChapterDownload(mChapter), mServerBase.needRefererForImages());
                s.setChangeListener(ActivityReader.this);
                new Thread(s).start();
            } catch (Exception e) {
                error = e.getMessage();
                if (error == null) {
                    error = "null";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (error.length() > 3) {
                Toast.makeText(ActivityReader.this, error, Toast.LENGTH_LONG).show();
            }
            if(!isDestroyed())
                mActionBar.setVisibility(View.VISIBLE);
        }
    }

}
