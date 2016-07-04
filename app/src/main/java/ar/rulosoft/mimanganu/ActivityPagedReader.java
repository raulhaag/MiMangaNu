package ar.rulosoft.mimanganu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ar.rulosoft.mimanganu.MangaFragment.Direction;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.UnScrolledViewPager;
import ar.rulosoft.mimanganu.componentes.UnScrolledViewPagerVertical;
import ar.rulosoft.mimanganu.componentes.readers.L2RReader;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.ChapterDownload.OnErrorListener;
import ar.rulosoft.mimanganu.services.DownloadListener;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.services.StateChangeListener;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.TapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.InitialPosition;
import it.sephiroth.android.library.imagezoom.graphics.FastBitmapDrawable;

public class ActivityPagedReader extends AppCompatActivity
        implements DownloadListener, OnSeekBarChangeListener, TapListener, OnErrorListener {

    private static final String TAG = "ActivityPagedReader";
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String ORIENTATION = "orientation";
    private static final String ADJUST_KEY = "ajustar_a";
    private static final String MAX_TEXTURE = "max_texture";
    private static int mTextureMax;
    private static DisplayType mScreenFit;
    boolean firedMessage = false;
    private Direction mDirection;
    private InitialPosition iniPosition = InitialPosition.LEFT_UP;
    // These are values, which should be fetched from preference
    private SharedPreferences pm;
    private boolean mKeepOn; // false = normal  | true = screen on
    private int mOrientation; // 0 = free | 1 = landscape | 2 = portrait
    private float mScrollFactor = 1f;
    // These are layout components
    private PageAdapter mPageAdapter;
    private UnScrolledViewPager mViewPager;
    private UnScrolledViewPagerVertical mViewPagerV;
    private RelativeLayout mControlsLayout, mScrollSelect;
    private LinearLayout mSeekerLayout;
    private SeekBar mSeekBar;
    private Toolbar mActionBar;
    private Chapter mChapter, nextChapter, previousChapter = null;
    private Manga mManga;
    private ServerBase mServerBase;
    private TextView mCurrentPage, mSeekerPage, mScrollSensitiveText;
    private MenuItem displayMenu, keepOnMenuItem, screenRotationMenuItem;
    private Button mButtonMinus, mButtonPlus;
    private boolean controlVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mScreenFit = DisplayType.valueOf(pm.getString(ADJUST_KEY, DisplayType.FIT_TO_WIDTH.toString()));
        mTextureMax = Integer.parseInt(pm.getString(MAX_TEXTURE, "2048"));
        mOrientation = pm.getInt(ORIENTATION, 0);
        mKeepOn = pm.getBoolean(KEEP_SCREEN_ON, false);
        mScrollFactor = Float.parseFloat(pm.getString("scroll_speed", "1"));

        int chapterId = getIntent().getExtras().getInt(MangaFragment.CHAPTER_ID);
        if (savedInstanceState != null) {
            chapterId = savedInstanceState.getInt(MangaFragment.CHAPTER_ID);
        }
        mChapter = Database.getChapter(this, chapterId);

        mManga = Database.getFullManga(this, mChapter.getMangaID());
        if (mManga.getScrollSensitive() > 0) {
            mScrollFactor = mManga.getScrollSensitive();
        }

        if (mManga.getReadingDirection() != -1) {
            mDirection = Direction.values()[mManga.getReadingDirection()];
        } else {
            mDirection = Direction.values()[Integer.parseInt(pm.getString(MangaFragment.DIRECTION, "" + Direction.R2L.ordinal()))];
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
            int anterior = -1;
            boolean lastPageChange = false;

            @Override
            public void onPageSelected(int arg0) {
                mPageAdapter.setCurrentPage(arg0);
                iniPosition = (anterior < arg0) ? InitialPosition.LEFT_UP : InitialPosition.LEFT_BOTTOM;
                anterior = arg0;
                switch (mDirection) {
                    case L2R: {
                        mSeekBar.setProgress(mChapter.getPages() - arg0 - 1);
                        if (arg0 <= 1) {
                            mChapter.setReadStatus(Chapter.READ);
                        } else if (mChapter.getReadStatus() == Chapter.READ) {
                            mChapter.setReadStatus(Chapter.READING);
                        }
                        break;
                    }
                    case R2L:
                    case VERTICAL: {
                        mSeekBar.setProgress(arg0);
                        if (arg0 >= mChapter.getPages() - 1) {
                            mChapter.setReadStatus(Chapter.READ);
                        } else if (mChapter.getReadStatus() == Chapter.READ) {
                            mChapter.setReadStatus(Chapter.READING);
                        }
                        break;
                    }
                }
                mCurrentPage.setText(String.format("%s/%s", mSeekBar.getProgress() + 1, mChapter.getPages()));
                mChapter.setPagesRead(mPageAdapter.currentPage + 1);
                Database.updateChapter(ActivityPagedReader.this, mChapter);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mPageAdapter != null) {
                    int lastIdx = mPageAdapter.getCount() - 1;
                    if (mDirection == Direction.L2R) {
                        position = lastIdx - position;
                    }
                    if (lastPageChange && position == lastIdx && !firedMessage) {
                        firedMessage = true;
                        onEndDrag();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int lastIdx = mPageAdapter.getCount() - 1;
                int curItem = mPageAdapter.currentPage;
                lastPageChange = curItem == lastIdx && state == 1;
            }
        };

        if (mDirection == Direction.VERTICAL) {
            setContentView(R.layout.activity_paged_reader_v);
            mViewPagerV = (UnScrolledViewPagerVertical) findViewById(R.id.pager);
            mViewPagerV.setOnPageChangeListener(pageChangeListener);
        } else {
            setContentView(R.layout.activity_paged_reader);
            mViewPager = (UnScrolledViewPager) findViewById(R.id.pager);
            mViewPager.addOnPageChangeListener(pageChangeListener);
        }

        mServerBase = ServerBase.getServer(mManga.getServerId());
        if (DownloadPoolService.actual != null)
            DownloadPoolService.setDownloadListener(this);

        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        mActionBar.setTitleTextColor(Color.WHITE);

        mControlsLayout = (RelativeLayout) findViewById(R.id.controls);
        mControlsLayout.setAlpha(0f);
        mControlsLayout.setVisibility(View.GONE);

        mSeekerPage = (TextView) findViewById(R.id.gotoPage);
        mSeekerPage.setAlpha(.9f);

        mSeekBar = (SeekBar) findViewById(R.id.seeker);
        mSeekBar.setOnSeekBarChangeListener(this);
        if (mDirection == Direction.L2R) {
            mCurrentPage = (TextView) findViewById(R.id.pageLeft);
            mSeekBar.setRotation(180);
        } else
            mCurrentPage = (TextView) findViewById(R.id.pageRight);
        mCurrentPage.setText(String.format("%s/%s", mSeekBar.getProgress() + 1, mChapter.getPages()));
        mCurrentPage.setVisibility(View.VISIBLE);

        mSeekerLayout = (LinearLayout) findViewById(R.id.seeker_layout);

        mScrollSelect = (RelativeLayout) findViewById(R.id.scroll_selector);
        mButtonMinus = (Button) findViewById(R.id.minus);
        mButtonPlus = (Button) findViewById(R.id.plus);
        mScrollSensitiveText = (TextView) findViewById(R.id.scroll_level);
        mScrollSensitiveText.setText(String.valueOf(mScrollFactor));

        int reader_bg = ThemeColors.getReaderColor(pm);
        mActionBar.setBackgroundColor(reader_bg);
        mSeekerLayout.setBackgroundColor(reader_bg);
        mSeekerPage.setBackgroundColor(reader_bg);
        mSeekBar.setBackgroundColor(reader_bg);
        mScrollSelect.setBackgroundColor(reader_bg);

        if(pm.getBoolean("hide_sensitivity_scrollbar", false))
            mScrollSelect.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(reader_bg);
            window.setStatusBarColor(reader_bg);
        }

        mChapter.setReadStatus(Chapter.READING);
        Database.updateChapter(ActivityPagedReader.this, mChapter);
        setSupportActionBar(mActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hideSystemUI();

        mButtonMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(-.5f);
            }
        });
        mButtonPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(.5f);
            }
        });
        mScrollSensitiveText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ActivityPagedReader.this, getString(R.string.scroll_speed), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        loadChapter(mChapter, ActivityReader.LoadMode.SAVED);
    }

    public void loadChapter(Chapter nChapter, ActivityReader.LoadMode mode) {
        mChapter = nChapter;
        if (!mChapter.isDownloaded()) {
            try {
                DownloadPoolService.addChapterDownloadPool(ActivityPagedReader.this, mChapter, true);
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Toast.makeText(ActivityPagedReader.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        setTitle(mChapter.getTitle());
        if (nChapter.getPages() == 0) {
            new GetPageTask().execute(nChapter);
        } else {
            DownloadPoolService.setDownloadListener(this);
            mSeekBar.setProgress(0);
            mChapter.setReadStatus(Chapter.READING);
            Database.updateChapter(ActivityPagedReader.this, mChapter);
            mPageAdapter = new PageAdapter();
            if (mDirection == Direction.VERTICAL)
                mViewPagerV.setAdapter(mPageAdapter);
            else mViewPager.setAdapter(mPageAdapter);
            mActionBar.setTitle(mChapter.getTitle());
            mSeekBar.setMax(mChapter.getPages() - 1);
            if (mChapter.getPagesRead() >= 1) {
                if (mDirection == Direction.R2L)
                    mViewPager.setCurrentItem(mChapter.getPagesRead() - 1);
                else if (mDirection == Direction.VERTICAL)
                    mViewPagerV.setCurrentItem(mChapter.getPagesRead() - 1);
                else mViewPager.setCurrentItem(
                            mChapter.getPages() - mChapter.getPagesRead());
            } else {
                if (mDirection == Direction.L2R)
                    mViewPager.setCurrentItem(mChapter.getPages() + 1);
            }
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
        }

        if (nextChapter != null) {
            if (!nextChapter.isDownloaded()) {
                if (pm.getBoolean("download_next_chapter_automatically", false)) {
                    try {
                        DownloadPoolService.addChapterDownloadPool(this, nextChapter, false);
                        Util.getInstance().toast(this, "Downloading: " + nextChapter.getTitle());
                    } catch (Exception e) {
                        Log.e("ServB", "Download add pool error", e);
                    }
                }
            }
        }

        switch (mode) {
            case START:
                if(mDirection == Direction.R2L)
                    setCurrentItem(0);
                else
                    setCurrentItem(mChapter.getPages());
                break;
            case END:
                if(mDirection == Direction.R2L)
                    setCurrentItem(mChapter.getPages());
                else
                    setCurrentItem(0);
                break;
        }

    }

    private void modScrollSensitive(float diff) {
        if ((mScrollFactor + diff) >= .5 && (mScrollFactor + diff) <= 5) {
            mScrollFactor += diff;
            Database.updateMangaScrollSensitive(ActivityPagedReader.this, mManga.getId(), mScrollFactor);
            mScrollSensitiveText.setText(String.valueOf(mScrollFactor));
            mPageAdapter.setPageScroll(mScrollFactor);
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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

    public void setAdapter(PagerAdapter adapter) {
        if (mDirection == Direction.VERTICAL)
            mViewPagerV.setAdapter(adapter);
        else mViewPager.setAdapter(adapter);
    }

    private int getCurrentItem() {
        if (mDirection == Direction.VERTICAL)
            return mViewPagerV.getCurrentItem();
        else return mViewPager.getCurrentItem();
    }

    private void setCurrentItem(int pos) {
        if (mDirection == Direction.VERTICAL)
            mViewPagerV.setCurrentItem(pos);
        else mViewPager.setCurrentItem(pos);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(MangaFragment.CHAPTER_ID, mChapter.getId());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mPageAdapter != null) {
            mChapter.setPagesRead(mPageAdapter.currentPage + 1);
        }
        Database.updateChapterPage(ActivityPagedReader.this, mChapter.getId(), mChapter.getPagesRead());
        DownloadPoolService.detachListener(mChapter.getId());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_paged_reader, menu);
        displayMenu = menu.findItem(R.id.action_ajustar);
        keepOnMenuItem = menu.findItem(R.id.action_keep_screen_on);
        screenRotationMenuItem = menu.findItem(R.id.action_orientation);
        if (mKeepOn) {
            keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_on);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mOrientation == 1) {
            ActivityPagedReader.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_landscape);
        } else if (mOrientation == 2) {
            ActivityPagedReader.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_portrait);
        }
        updateIcon(mScreenFit, false);
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
                mPageAdapter.updateDisplayType();
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
                return true;
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
            }
            case R.id.re_download_image:
                mPageAdapter.getCurrentPage().reDownloadImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageDownloaded(final int cid, final int page) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mChapter.getId() == cid && mPageAdapter != null)
                    mPageAdapter.pageDownloaded(page);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // This is happening, if you swipe left and right on the bar
        if (mSeekerPage != null)
            mSeekerPage.setText(String.format("%s", progress + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // This is happening, if you touch on the bar (no swipe)
        mSeekerPage.setVisibility(SeekBar.VISIBLE);
        mSeekerPage.setText(String.format("%s", seekBar.getProgress() + 1));
    }

    public void onEndDrag() {
        LayoutInflater inflater = getLayoutInflater();
        boolean deleteImages = pm.getBoolean("delete_images", false);
        boolean seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
        if (nextChapter != null) {
            if (!seamlessChapterTransition) {
                View v = inflater.inflate(R.layout.dialog_next_chapter, null);
                final CheckBox checkBox = (CheckBox) v.findViewById(R.id.delete_images_oc);
                checkBox.setChecked(deleteImages);
                new AlertDialog.Builder(ActivityPagedReader.this)
                        .setTitle(mChapter.getTitle() + " " + getString(R.string.finalizado))
                        .setView(v)
                        .setIcon(R.drawable.ic_launcher)
                        .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firedMessage = false;
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean del_images = checkBox.isChecked();
                                if (pm != null)
                                    pm.edit().putBoolean("delete_images", del_images).apply();
                                mChapter.setReadStatus(Chapter.READ);
                                mChapter.setPagesRead(mChapter.getPages());
                                Database.updateChapter(ActivityPagedReader.this, mChapter);
                                Chapter pChapter = mChapter;
                                loadChapter(nextChapter, ActivityReader.LoadMode.START);
                                if (del_images) {
                                    pChapter.freeSpace(ActivityPagedReader.this);
                                }
                                firedMessage = false;
                            }
                        })
                        .show();
            }
        } else {
            displayLastChapterDialog();
        }
    }

    private void displayLastChapterDialog() {
        LayoutInflater inflater = getLayoutInflater();
        boolean deleteImages = pm.getBoolean("delete_images", false);
        View v = inflater.inflate(R.layout.dialog_no_more_chapters, null);
        final CheckBox checkBox = (CheckBox) v.findViewById(R.id.delete_images_oc);
        checkBox.setChecked(deleteImages);
        new AlertDialog.Builder(ActivityPagedReader.this)
                .setTitle(mChapter.getTitle() + " " + getString(R.string.finalizado))
                .setView(v)
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firedMessage = false;
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean del_images = checkBox.isChecked();
                        if (pm != null)
                            pm.edit().putBoolean("delete_images", del_images).apply();
                        if(mViewPager != null)
                            mViewPager.setAdapter(null);
                        if (del_images) {
                            mChapter.freeSpace(ActivityPagedReader.this);
                        }
                        firedMessage = false;
                        dialog.dismiss();
                        onBackPressed();
                    }
                })
                .show();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // This is happening, if you lift your finger off the bar
        try {
            if (mDirection == Direction.R2L || mDirection == Direction.VERTICAL)
                setCurrentItem(seekBar.getProgress());
            else {
                setCurrentItem(mChapter.getPages() - seekBar.getProgress() - 1);
            }
            mSeekerPage.setVisibility(SeekBar.INVISIBLE);
        } catch (Exception e) {
            // sometimes gets a null, just in case, don't stop the app
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onCenterTap();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCenterTap() {
        if (controlVisible) {
            hideSystemUI();
            controlVisible = false;
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(mControlsLayout, "alpha", .90f, 0f);
            anim2.addListener(new AnimatorListener() {
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
            anim2.addListener(new AnimatorListener() {
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

    private void updateDBAndLoadChapter(Chapter chapter, int readOrUnread, int pagesRead, ActivityReader.LoadMode mode) {
        mChapter.setReadStatus(readOrUnread);
        mChapter.setPagesRead(pagesRead);
        Database.updateChapter(ActivityPagedReader.this, mChapter);
        loadChapter(chapter, mode);
        firedMessage = false;
        if (!chapter.isDownloaded())
            mPageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLeftTap() {
        int act = getCurrentItem();
        if (act > 0)
            setCurrentItem(--act);
        else
            act--;

        if (act == -1) {
            boolean seamlessChapterTransition;
            boolean seamlessChapterTransitionDeleteRead;
            if (mDirection.equals(Direction.R2L) || mDirection.equals(Direction.VERTICAL)) {
                if (previousChapter != null) {
                    seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
                    if (seamlessChapterTransition) {
                        updateDBAndLoadChapter(previousChapter, Chapter.UNREAD, 0, ActivityReader.LoadMode.SAVED);
                        Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);
                    }
                }
            } else if (mDirection.equals(Direction.L2R)) {
                if (nextChapter != null) {
                    seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
                    seamlessChapterTransitionDeleteRead = pm.getBoolean("seamless_chapter_transitions_delete_read", false);
                    if (seamlessChapterTransition) {
                        Chapter tmpChapter = mChapter;

                        updateDBAndLoadChapter(nextChapter, Chapter.READ, mChapter.getPages(), ActivityReader.LoadMode.START);
                        Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);

                        if (seamlessChapterTransitionDeleteRead) {
                            tmpChapter.freeSpace(ActivityPagedReader.this);
                            Util.getInstance().toast(getApplicationContext(), getResources().getString(R.string.deleted, tmpChapter.getTitle()), 0);
                        }
                    }
                } else {
                    displayLastChapterDialog();
                }
            }
        }
    }

    @Override
    public void onRightTap() {
        int act = getCurrentItem();
        if (act < mPageAdapter.getCount()) setCurrentItem(++act);

        if (mPageAdapter.getCount() == act) {
            boolean seamlessChapterTransition;
            boolean seamlessChapterTransitionDeleteRead;
            if (mDirection.equals(Direction.R2L) || mDirection.equals(Direction.VERTICAL)) {
                if (nextChapter != null) {
                    seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
                    seamlessChapterTransitionDeleteRead = pm.getBoolean("seamless_chapter_transitions_delete_read", false);
                    if (seamlessChapterTransition) {
                        Chapter tmpChapter = mChapter;

                        updateDBAndLoadChapter(nextChapter, Chapter.READ, mChapter.getPages(), ActivityReader.LoadMode.START);
                        Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);

                        if (seamlessChapterTransitionDeleteRead) {
                            tmpChapter.freeSpace(ActivityPagedReader.this);
                            Util.getInstance().toast(getApplicationContext(), getResources().getString(R.string.deleted, tmpChapter.getTitle()), 0);
                        }
                    }
                } else {
                    displayLastChapterDialog();
                }
            } else if (mDirection.equals(Direction.L2R)) {
                if (previousChapter != null) {
                    seamlessChapterTransition = pm.getBoolean("seamless_chapter_transitions", false);
                    if (seamlessChapterTransition) {
                        updateDBAndLoadChapter(previousChapter, Chapter.UNREAD, 0, ActivityReader.LoadMode.SAVED);
                        Util.getInstance().toast(getApplicationContext(), mChapter.getTitle(), 0);
                    }
                }
            }
        }
    }

    @Override
    public void onError(final Chapter chapter) {
        ActivityPagedReader.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(ActivityPagedReader.this)
                            .setTitle(chapter.getTitle() + " " + getString(R.string.error))
                            .setMessage(getString(R.string.demaciados_errores))
                            .setIcon(R.drawable.ic_launcher)
                            .setNeutralButton(getString(android.R.string.ok), null)
                            .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DownloadPoolService.retryError(getApplicationContext(), mChapter, ActivityPagedReader.this);
                                    dialog.dismiss();
                                    DownloadPoolService.setDownloadListener(ActivityPagedReader.this);
                                }
                            })
                            .show();
                } catch (Exception e) {
                    // lost references fixed con detachListener
                }
            }
        });
    }

    public class Page extends RelativeLayout implements StateChangeListener {
        public ImageViewTouch visor;
        public ActivityPagedReader activity = ActivityPagedReader.this;
        ProgressBar loading;
        Runnable r = null;
        boolean imageLoaded = false;
        int index = 0;
        private String path = null;

        public Page(Context context) {
            super(context);
            init();
        }

        public Page(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public Page(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public void init() {
            String infService = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li =
                    (LayoutInflater) getContext().getSystemService(infService);
            li.inflate(R.layout.view_reader_page, this, true);
            visor = (ImageViewTouch) findViewById(R.id.visor);
            visor.setDisplayType(mScreenFit);
            visor.setTapListener(ActivityPagedReader.this);
            visor.setScaleEnabled(false);
            loading = (ProgressBar) findViewById(R.id.loading);
            loading.bringToFront();
            visor.setScrollFactor(activity.mScrollFactor);
        }

        public void unloadImage() {
            if (visor != null) {
                if (visor.getDrawable() != null)
                    ((FastBitmapDrawable) visor.getDrawable()).getBitmap().recycle();
                visor.setImageDrawable(null);
                visor.setImageBitmap(null);
            }
            imageLoaded = false;
        }

        public void setImage() {
            if (!imageLoaded && visor != null)
                new SetImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void setImage(String path) {
            this.path = path;
            setImage();
        }

        public boolean canScroll(int dx) {
            return visor == null || visor.canScroll(dx);
        }

        public boolean canScrollV(int dx) {
            return visor == null || visor.canScrollV(dx);
        }

        @Override
        public void onChange(SingleDownload singleDownload) {
            if (singleDownload.status == SingleDownload.Status.DOWNLOAD_OK) {
                setImage();
            } else {
                if (singleDownload.status.ordinal() > SingleDownload.Status.DOWNLOAD_OK.ordinal()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ActivityPagedReader.this, R.string.error_downloading_image, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }

        public void reDownloadImage() {
            new ReDownloadImage().execute();
        }

        public class SetImageTask extends AsyncTask<Void, Void, Bitmap> {

            @Override
            protected void onPreExecute() {
                if (loading != null)
                    loading.setVisibility(ProgressBar.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                boolean notLoaded = true;
                Bitmap bitmap = null;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Config.RGB_565;
                while (notLoaded) {
                    try {
                        bitmap = BitmapFactory.decodeFile(path, opts);
                        notLoaded = false;
                    } catch (OutOfMemoryError oom) {
                        try {
                            Thread.sleep(3000);//time to free memory
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null && visor != null) {
                    imageLoaded = true;
                    visor.setScaleEnabled(true);
                    if (activity.mDirection == Direction.VERTICAL)
                        visor.setInitialPosition(activity.iniPosition);
                    else visor.setInitialPosition(InitialPosition.LEFT_UP);
                    if ((result.getHeight() > mTextureMax ||
                            result.getWidth() > mTextureMax) &&
                            Build.VERSION.SDK_INT >= 11) {
                        visor.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                    visor.setAlpha(0f);
                    visor.setImageBitmap(result);
                    if (activity != null && index == activity.getCurrentItem()) {
                        ObjectAnimator.ofFloat(visor, "alpha", 1f).setDuration(500).start();
                    } else {
                        visor.setAlpha(1f);
                    }
                    loading.setVisibility(ProgressBar.INVISIBLE);
                }
                super.onPostExecute(result);
            }
        }

        public class ReDownloadImage extends AsyncTask<Void, Void, Void> {
            String error = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (loading != null)
                    loading.setVisibility(ProgressBar.VISIBLE);
                unloadImage();
            }

            @Override
            protected Void doInBackground(Void... params) {
                File f = new File(path);
                if (f.exists()) {
                    f.delete();
                }
                try {
                    SingleDownload s = new SingleDownload(mServerBase.getImageFrom(mChapter, index + 1), path, 0, 0, new ChapterDownload(mChapter), mServerBase.needRefererForImages());
                    s.setChangeListener(Page.this);
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
                    Toast.makeText(ActivityPagedReader.this, error, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class PageAdapter extends PagerAdapter {

        int currentPage = 0;
        Page[] pages = new Page[mChapter.getPages()];


        public Page getCurrentPage() {
            return pages[currentPage];
        }

        public void setCurrentPage(int currentPage) {
            if (mDirection == Direction.L2R)
                currentPage = mChapter.getPages() - 1 - currentPage;
            this.currentPage = currentPage;
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    if (Math.abs(i - currentPage) <= 1 && !pages[i].imageLoaded) {
                        pages[i].setImage();
                    } else if (Math.abs(i - currentPage) > 1 && pages[i].imageLoaded) {
                        pages[i] = null;
                    }
                }
            }
        }

        public Page getPage(int position) {
            return pages[position];
        }

        @Override
        public int getCount() {
            return mChapter.getPages();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mDirection == Direction.L2R)
                position = mChapter.getPages() - 1 - position;
            Page page = pages[position];
            if (pages[position] != null) {
                container.addView(page, 0);
            } else {
                Context context = ActivityPagedReader.this;
                page = new Page(context);
                if (!(mServerBase instanceof FromFolder)) {
                    page.setImage(DownloadPoolService.generateBasePath(mServerBase, mManga, mChapter, getApplicationContext()) + "/" + (position + 1) + ".jpg");
                } else {
                    try {
                        page.setImage(mServerBase.getImageFrom(mChapter, position));
                    } catch (Exception ignore) {
                    }
                }
                container.addView(page, 0);
                page.index = position;
                pages[position] = page;
            }
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((Page) object);
        }

        public void updateDisplayType() {
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    pages[i].visor.setDisplayType(mScreenFit);
                }
            }
        }

        public void setPageScroll(float pageScroll) {
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    pages[i].visor.setScrollFactor(pageScroll);
                }
            }
        }

        public void pageDownloaded(int page) {
            if (pages[page] != null && currentPage == page) {
                pages[page].setImage();
            }
        }
    }

    public class GetPageTask extends AsyncTask<Chapter, Void, Chapter> {
        ProgressDialog asyncDialog = new ProgressDialog(ActivityPagedReader.this);
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
            ServerBase s = ServerBase.getServer(ActivityPagedReader.this.mManga.getServerId());
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
                Toast.makeText(ActivityPagedReader.this, error, Toast.LENGTH_LONG).show();
            } else {
                try {
                    Database.updateChapter(ActivityPagedReader.this, result);
                    DownloadPoolService.addChapterDownloadPool(ActivityPagedReader.this, result, true);
                    DownloadPoolService.setDownloadListener(ActivityPagedReader.this);
                    loadChapter(result, ActivityReader.LoadMode.SAVED);
                } catch (Exception e) {
                    Toast.makeText(ActivityPagedReader.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            super.onPostExecute(result);
        }
    }
}
