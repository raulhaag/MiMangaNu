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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.ActivityManga.Direction;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadListener;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.readers.library.L2RReader;
import ar.rulosoft.readers.library.R2LReader;
import ar.rulosoft.readers.library.Reader;
import ar.rulosoft.readers.library.VerticalReader;

public class ActivityReader extends AppCompatActivity implements DownloadListener, SeekBar.OnSeekBarChangeListener, VerticalReader.OnTapListener, ChapterDownload.OnErrorListener, VerticalReader.OnViewReadyListener, VerticalReader.OnEndFlingListener {

    // These are magic numbers
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String ORIENTATION = "orientation";
    private static final String MAX_TEXTURE = "max_texture";
    private static int mTextureMax;
    public Reader mReader;
    boolean updatedValue = false;//just a flag to no seek when the reader seek
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
    private Chapter mChapter, nextChapter;
    private Manga mManga;
    private ServerBase mServerBase;
    private TextView mSeekerPage, mScrollSensitiveText;
    private MenuItem keepOnMenuItem, screenRotationMenuItem;
    private Button mButtonMinus, mButtonPlus;

    private boolean controlVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        mChapter = Database.getChapter(this, getIntent().getExtras().getInt(ActivityManga.CAPITULO_ID));
        mManga = Database.getFullManga(this, mChapter.getMangaID());

        pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mTextureMax = Integer.parseInt(pm.getString(MAX_TEXTURE, "2048"));
        mOrientation = pm.getInt(ORIENTATION, 0);
        mKeepOn = pm.getBoolean(KEEP_SCREEN_ON, false);
        mScrollFactor = Float.parseFloat(pm.getString("scroll_speed", "1"));
        if (mManga.getReadingDirection() != -1) {
            direction = Direction.values()[mManga.getReadingDirection()];
        } else {
            direction = Direction.values()[Integer.parseInt(pm.getString(ActivityManga.DIRECCION, "" + Direction.R2L.ordinal()))];
        }

        if (mManga.getScrollSensitive() > 0) {
            mScrollFactor = mManga.getScrollSensitive();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mServerBase = ServerBase.getServer(mManga.getServerId());
        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        mActionBar.setTitleTextColor(Color.WHITE);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(reader_bg);
            window.setStatusBarColor(reader_bg);
        }

        setSupportActionBar(mActionBar);
        hideSystemUI();

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(-.5f);
            }
        });

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modScrollSensitive(.5f);
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
        if(mReader != null) {
            mReader.freeMemory();
        }
        if (direction == Direction.R2L) {
            mReader = new R2LReader(this);
        } else if (direction == Direction.L2R) {
            mReader = new L2RReader(this);
        } else {
            mReader = new VerticalReader(this);
        }
        mReader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.reader_placeholder)).removeAllViews();
        ((FrameLayout) findViewById(R.id.reader_placeholder)).addView(mReader);
        mReader.setMaxTexture(mTextureMax);
        mReader.setViewReadyListener(this);
        mReader.setTapListener(this);
        mReader.mScrollSensitive = mScrollFactor;
        mReader.setOnEndFlingListener(this);
        loadChapter(mChapter);
    }

    private void loadChapter(Chapter nChapter) {
        mChapter = nChapter;
        if (nChapter.getPages() == 0) {
            new GetPageTask().execute(nChapter);
        } else {
            DownloadPoolService.setDownloadListener(this);
            mSeekBar.setProgress(0);
            mChapter.setReadStatus(Chapter.READING);
            Database.updateChapter(ActivityReader.this, mChapter);
            mReader.reset();
            ArrayList<String> pages = new ArrayList<>();
            for (int i = 0; i < mChapter.getPages(); i++) {
                pages.add(DownloadPoolService.generarRutaBase(mServerBase, mManga, mChapter, getApplicationContext()) + "/" + (i + 1) + ".jpg");
            }
            mReader.setPaths(pages);
            mActionBar.setTitle(mChapter.getTitle());
            mSeekBar.setMax(mChapter.getPages() - 1);
            mReader.setPageChangeListener(new VerticalReader.OnPageChangeListener() {
                @Override
                public void onPageChanged(int page) {
                    updatedValue = true;
                    mChapter.setPagesRead(page + 1);
                    mSeekBar.setProgress(page);
                    if (mReader.isLastPageVisible()) {
                        mChapter.setPagesRead(mChapter.getPages());
                        mChapter.setReadStatus(Chapter.READ);
                    } else if (mChapter.getReadStatus() == Chapter.READ) {
                        mChapter.setReadStatus(Chapter.READING);
                    }
                }
            });
            DownloadPoolService.attachListener(this, mChapter.getId());
            boolean next = false;
            for (int i = 0; i < mManga.getChapters().size(); i++) {
                if (mManga.getChapters().get(i).getId() == mChapter.getId()) {
                    if (i > 0) {
                        next = true;
                        nextChapter = mManga.getChapters().get(i - 1);
                        break;
                    }
                }
            }
            if (!next)
                nextChapter = null;
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                Database.updadeReadOrder(ActivityReader.this, this.direction.ordinal(), mManga.getId());
                setReader();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageDownloaded(int cid, int page) {
        if (cid == mChapter.getId())
            mReader.reloadImage(page);
    }

    @Override
    protected void onPause() {
        Database.updateChapter(ActivityReader.this, mChapter);
        if (mReader.isLastPageVisible())
            Database.updateChapterPage(ActivityReader.this, mChapter.getId(), mChapter.getPages());
        else
            Database.updateChapterPage(ActivityReader.this, mChapter.getId(), mChapter.getPagesRead());
        DownloadPoolService.detachListener(mChapter.getId());
        super.onPause();
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
        mReader.seekPage(mChapter.getPagesRead() - 1);
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
        mReader.goToPage(pm);
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
    public void onLeftTap() {
        mReader.goToPage(mReader.currentPage - 1);
    }

    @Override
    public void onRightTap() {
        mReader.goToPage(mReader.currentPage + 1);
    }

    @Override
    public void onError(final Chapter chapter) {
        ActivityReader.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(ActivityReader.this)
                            .setTitle(chapter.getTitle() + " " + getString(R.string.error))
                            .setMessage(getString(R.string.demaciados_errores))
                            .setIcon(R.drawable.ic_launcher)
                            .setNeutralButton(getString(android.R.string.ok), null)
                            .show();
                } catch (Exception e) {
                    // lost references fixed con detachListener
                }
            }
        });

    }

    @Override
    public void onViewReady() {
        mReader.seekPage(mChapter.getPagesRead() - 1);
    }

    @Override
    public void onEndFling() {
        if (nextChapter != null) {
            new AlertDialog.Builder(ActivityReader.this)
                    .setTitle(mChapter.getTitle() + " " + getString(R.string.finalizado))
                    .setMessage(R.string.read_next)
                    .setIcon(R.drawable.ic_launcher)
                    .setNegativeButton(getString(android.R.string.no), null)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mChapter.setReadStatus(Chapter.READ);
                            mChapter.setPagesRead(mChapter.getPages());
                            Database.updateChapter(ActivityReader.this, mChapter);
                            loadChapter(nextChapter);
                        }
                    })
                    .show();
        } else {
            new AlertDialog.Builder(ActivityReader.this)
                    .setTitle(mChapter.getTitle() + " " + getString(R.string.finalizado))
                    .setMessage(R.string.last_chapter)
                    .setIcon(R.drawable.ic_launcher)
                    .setPositiveButton(getString(android.R.string.ok), null)
                    .show();
        }
    }

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
                Database.updateChapter(ActivityReader.this, result);
                DownloadPoolService.agregarDescarga(ActivityReader.this, result, true);
                loadChapter(result);
            }
            super.onPostExecute(result);
        }
    }

}
