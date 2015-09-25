package ar.rulosoft.mimanganu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.ActivityManga.Direction;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.UnScrolledViewPager;
import ar.rulosoft.mimanganu.componentes.UnScrolledViewPagerVertical;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload.OnErrorListener;
import ar.rulosoft.mimanganu.services.DownloadListener;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.TapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.InitialPosition;

public class ActivityLector extends AppCompatActivity
        implements DownloadListener, OnSeekBarChangeListener, TapListener, OnErrorListener {

    // These are magic numbers
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    public static final String ORIENTATION = "orientation";
    public static final String ADJUST_KEY = "ajustar_a";
    public static final String MAX_TEXTURE = "max_texture";
    private static int mTextureMax;
    private static DisplayType mScreenFit;

    public Direction mDirection;
    public InitialPosition iniPosition = InitialPosition.LEFT_UP;
    // These are values, which should be fetched from preference
    private SharedPreferences pm;
    private boolean mKeepOn; // false = normal  | true = screen on
    private int mOrientation; // 0 = free | 1 = landscape | 2 = portrait
    private float mScrollFactor = 1f;
    // These are layout components
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private UnScrolledViewPager mViewPager;
    private UnScrolledViewPagerVertical mViewPagerV;
    private RelativeLayout mControlsLayout, mScrollSelect;
    private LinearLayout mSeekerLayout;
    private SeekBar mSeekBar;
    private Toolbar mActionBar;
    private Chapter mChapter;
    private Manga mManga;
    private ServerBase mServerBase;
    private TextView mSeekerPage, mScrollSensitiveText;
    private MenuItem displayMenu, keepOnMenuItem, screenRotationMenuItem;
    private LastPageFragment mLastPageFrag;
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

        mChapter = Database.getChapter(this, getIntent().getExtras().getInt(ActivityManga.CAPITULO_ID));
        mManga = Database.getFullManga(this, mChapter.getMangaID());
        if (mManga.getScrollSensitive() > 0) {
            mScrollFactor = mManga.getScrollSensitive();
        }

        if (mManga.getReadingDirection() != -1) {
            mDirection = Direction.values()[mManga.getReadingDirection()];
        } else {
            mDirection = Direction.values()[Integer.parseInt(pm.getString(ActivityManga.DIRECCION, "" + Direction.R2L.ordinal()))];
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
            int anterior = -1;

            @Override
            public void onPageSelected(int arg0) {

                iniPosition = (anterior < arg0) ? InitialPosition.LEFT_UP : InitialPosition.LEFT_BOTTOM;
                anterior = arg0;

                switch (mDirection) {
                    case L2R: {
                        mChapter.setPagesRead(mChapter.getPages() - arg0 + ((arg0 > 0) ? 1 : 0));
                        mSeekBar.setProgress(mChapter.getPages() - arg0);
                        if (arg0 <= 1) {
                            mChapter.setReadStatus(Chapter.READ);
                        } else if (mChapter.getReadStatus() == Chapter.READ) {
                            mChapter.setReadStatus(Chapter.READING);
                        }
                        break;
                    }
                    case R2L:
                    case VERTICAL: {
                        mChapter.setPagesRead(arg0 + ((arg0 < mChapter.getPages()) ? 1 : 0));
                        mSeekBar.setProgress(arg0);
                        if (arg0 >= mChapter.getPages() - 1) {
                            mChapter.setReadStatus(Chapter.READ);
                        } else if (mChapter.getReadStatus() == Chapter.READ) {
                            mChapter.setReadStatus(Chapter.READING);
                        }
                        break;
                    }
                }
                Database.updateChapter(ActivityLector.this, mChapter);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        };

        if (mDirection == Direction.VERTICAL) {
            setContentView(R.layout.activity_lector_v);
            mViewPagerV = (UnScrolledViewPagerVertical) findViewById(R.id.pager);
            mViewPagerV.setOnPageChangeListener(pageChangeListener);
        } else {
            setContentView(R.layout.activity_lector);
            mViewPager = (UnScrolledViewPager) findViewById(R.id.pager);
            mViewPager.addOnPageChangeListener(pageChangeListener);
        }

        mServerBase = ServerBase.getServer(mManga.getServerId());
        if (DownloadPoolService.actual != null)
            DownloadPoolService.actual.setDownloadListener(this);
        mLastPageFrag = new LastPageFragment();

        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        mActionBar.setTitle(mChapter.getTitle());
        mActionBar.setTitleTextColor(Color.WHITE);

        mControlsLayout = (RelativeLayout) findViewById(R.id.controls);
        mControlsLayout.setAlpha(0f);
        mControlsLayout.setVisibility(View.GONE);

        mSeekerPage = (TextView) findViewById(R.id.page);
        mSeekerPage.setAlpha(.9f);
        mSeekerPage.setTextColor(Color.WHITE);

        mSeekBar = (SeekBar) findViewById(R.id.seeker);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(mChapter.getPages());
        if (mDirection == Direction.L2R) mSeekBar.setRotation(180);

        mSeekerLayout = (LinearLayout) findViewById(R.id.seeker_layout);

        mScrollSelect = (RelativeLayout) findViewById(R.id.scroll_selector);
        mButtonMinus = (Button) findViewById(R.id.minus);
        mButtonPlus = (Button) findViewById(R.id.plus);
        mScrollSensitiveText = (TextView) findViewById(R.id.scroll_level);
        mScrollSensitiveText.setText("" + mScrollFactor);

        int reader_bg = ThemeColors.getReaderColor(pm);
        mActionBar.setBackgroundColor(reader_bg);
        mSeekerLayout.setBackgroundColor(reader_bg);
        mSeekerPage.setBackgroundColor(reader_bg);
        mSeekBar.setBackgroundColor(reader_bg);
        mScrollSelect.setBackgroundColor(reader_bg);

        mChapter.setReadStatus(Chapter.READING);
        Database.updateChapter(ActivityLector.this, mChapter);
        setSupportActionBar(mActionBar);
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
                Toast.makeText(ActivityLector.this, getString(R.string.scroll_speed), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    private void modScrollSensitive(float diff) {
        if ((mScrollFactor + diff) >= .5 && (mScrollFactor + diff) <= 5) {
            mScrollFactor += diff;
            Database.updateMangaScrollSensitive(ActivityLector.this, mManga.getId(), mScrollFactor);
            mScrollSensitiveText.setText("" + mScrollFactor);
            mSectionsPagerAdapter.setPageScroll(mScrollFactor);
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

    public void actualizarIcono(DisplayType displayType, boolean showMsg) {
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

    @Override
    protected void onResume() {
        mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        if (mDirection == Direction.VERTICAL)
            mViewPagerV.setAdapter(mSectionsPagerAdapter);
        else mViewPager.setAdapter(mSectionsPagerAdapter);

        if (mChapter.getPagesRead() > 1) {
            if (mDirection == Direction.R2L)
                mViewPager.setCurrentItem(mChapter.getPagesRead() - 1);
            else if (mDirection == Direction.VERTICAL)
                mViewPagerV.setCurrentItem(mChapter.getPagesRead() - 1);
            else mViewPager.setCurrentItem(
                        mChapter.getPages() - mChapter.getPagesRead() + 1);
        } else {
            if (mDirection == Direction.L2R)
                mViewPager.setCurrentItem(mChapter.getPages() + 1);
        }
        DownloadPoolService.attachListener(this, mChapter.getId());
        super.onResume();
    }

    public void setAdapter(SectionsPagerAdapter adapter) {
        if (mDirection == Direction.VERTICAL)
            mViewPagerV.setAdapter(adapter);
        else mViewPager.setAdapter(adapter);
    }

    public int getCurrentItem() {
        if (mDirection == Direction.VERTICAL)
            return mViewPagerV.getCurrentItem();
        else return mViewPager.getCurrentItem();
    }

    public void setCurrentItem(int pos) {
        if (mDirection == Direction.VERTICAL)
            mViewPagerV.setCurrentItem(pos);
        else mViewPager.setCurrentItem(pos);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Database.updateChapterPage(ActivityLector.this, mChapter.getId(), mChapter.getPagesRead());
    }

    @Override
    protected void onPause() {
        Database.updateChapterPage(ActivityLector.this, mChapter.getId(), mChapter.getPagesRead());
        DownloadPoolService.detachListener(mChapter.getId());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lector, menu);
        displayMenu = menu.findItem(R.id.action_ajustar);
        keepOnMenuItem = menu.findItem(R.id.action_keep_screen_on);
        screenRotationMenuItem = menu.findItem(R.id.action_orientation);
        if (mKeepOn) {
            keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_on);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mOrientation == 1) {
            ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_landscape);
        } else if (mOrientation == 2) {
            ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_portrait);
        }
        actualizarIcono(mScreenFit, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ajustar: {
                mScreenFit = mScreenFit.getNext();
                SharedPreferences.Editor editor = pm.edit();
                editor.putString(ADJUST_KEY, mScreenFit.toString());
                editor.apply();
                mSectionsPagerAdapter.actualizarDisplayTipe();
                actualizarIcono(mScreenFit, true);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImagenDescargada(final int cid, final int pagina) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment fragment =
                        mSectionsPagerAdapter.getIfOnMemory(pagina);
                if (fragment != null &&
                        !((PageFragment) fragment).imageLoaded) {
                    ((PageFragment) fragment).setImage();
                }
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // This is happening, if you swipe left and right on the bar
        if (mSeekerPage != null) mSeekerPage.setText("" + (progress + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // This is happening, if you touch on the bar (no swipe)
        mSeekerPage.setText("" + (seekBar.getProgress() + 1));
        mSeekerPage.setVisibility(SeekBar.VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // This is happening, if you lift your finger off the bar
        try {
            mSeekerPage.setVisibility(SeekBar.INVISIBLE);
            if (mDirection == Direction.R2L || mDirection == Direction.VERTICAL)
                setCurrentItem(seekBar.getProgress());
            else {
                setCurrentItem(mChapter.getPages() - seekBar.getProgress());
            }
        } catch (Exception e) {
            // sometimes gets a null, just in case, don't stop the app
        }
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

    @Override
    public void onLeftTap() {
        int act = getCurrentItem();
        if (act > 0) setCurrentItem(--act);
    }

    @Override
    public void onRightTap() {
        int act = getCurrentItem();
        if (act < mSectionsPagerAdapter.getCount()) setCurrentItem(++act);
    }

    @Override
    public void onError(final Chapter chapter) {
        ActivityLector.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(ActivityLector.this)
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

    public static class PageFragment extends Fragment {

        private static final String RUTA = "ruta";
        public ImageViewTouch visor;
        public ActivityLector activity;
        ProgressBar cargando;
        TapListener mTapListener;
        Runnable r = null;
        boolean imageLoaded = false;
        private String ruta = null;
        private int index;

        public PageFragment() {
        }

        public static PageFragment newInstance(String ruta, ActivityLector activity, int index) {
            PageFragment fragment = new PageFragment();
            Bundle args = new Bundle();
            args.putString(RUTA, ruta);
            fragment.activity = activity;
            fragment.setArguments(args);
            fragment.setRetainInstance(false);
            fragment.index = index;
            return fragment;
        }

        public void setTapListener(TapListener nTapListener) {
            mTapListener = nTapListener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_activity_lector_pagina, container, false);
            visor = (ImageViewTouch) rootView.findViewById(R.id.visor);

            if (r != null) new Thread(r).start();
            else visor.setDisplayType(mScreenFit);
            visor.setTapListener(mTapListener);
            visor.setScaleEnabled(false);

            cargando = (ProgressBar) rootView.findViewById(R.id.cargando);
            cargando.bringToFront();
            if (getArguments() != null)
                ruta = getArguments().getString(RUTA);
            visor.setScrollFactor(activity.mScrollFactor);
            return rootView;
        }

        public void setDisplayType(final DisplayType displayType) {
            if (visor != null) {
                visor.setDisplayType(displayType);
            } else {
                r = new Runnable() {
                    @Override
                    public void run() {
                        visor.setDisplayType(displayType);
                        r = null;
                    }
                };
            }
        }

        @Override
        public void onResume() {
            try {
                visor = (ImageViewTouch) getView().findViewById(R.id.visor);
            } catch (NullPointerException n) {
                // ignore null pointer exception
            }
            if (visor == null) cargando.setVisibility(ProgressBar.VISIBLE);
            else if (ruta != null) setImage();
            super.onResume();
        }

        @Override
        public void onPause() {
            try {
                ((BitmapDrawable) visor.getDrawable()).getBitmap().recycle();
            } catch (Exception e) {
                // Do nothing?
            }
            visor.setImageBitmap(null);
            imageLoaded = false;
            super.onPause();
        }

        public boolean canScroll(int dx) {
            return visor == null || visor.canScroll(dx);
        }

        public boolean canScrollV(int dx) {
            return visor == null || visor.canScrollV(dx);
        }

        public void setImage() {
            if (!imageLoaded && visor != null)
                new SetImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void setImage(String ruta) {
            this.ruta = ruta;
            setImage();
        }

        public class SetImageTask extends AsyncTask<Void, Void, Bitmap> {

            @Override
            protected void onPreExecute() {
                if (cargando != null)
                    cargando.setVisibility(ProgressBar.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Config.RGB_565;
                bitmap = BitmapFactory.decodeFile(ruta, opts);
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

                    cargando.setVisibility(ProgressBar.INVISIBLE);
                } else if (ruta != null) {
                    File f = new File(ruta);
                    if (f.exists()) {
                        f.delete();
                    }
                    /*
                     * if (!ColaDeDescarga.corriendo) {
					 * ColaDeDescarga.add(((ActivityReader)
					 * getActivity()).capitulo);
					 * ColaDeDescarga.iniciarCola(getActivity()); }/
					 */
                }
                super.onPostExecute(result);
            }
        }
    }

    public static class LastPageFragment extends Fragment { //need to be static
        Button btnNext, btnPrev;
        Chapter chNext = null, chPrev = null;
        ActivityLector l;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rView = inflater.inflate(R.layout.fragment_pagina_final, container, false);
            btnNext = (Button) rView.findViewById(R.id.button_next);
            btnPrev = (Button) rView.findViewById(R.id.button_previous);
            return rView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            l = (ActivityLector) getActivity();
            int cid = l.mChapter.getId();
            ArrayList<Chapter> caps = l.mManga.getChapters();
            for (int i = 0; i < caps.size(); i++) {
                if (caps.get(i).getId() == cid) {
                    if (i > 0) {
                        chNext = caps.get(i - 1);
                    }
                    if (i < caps.size() - 1) {
                        chPrev = caps.get(i + 1);
                    }
                }
            }

            if (chNext == null) {
                btnNext.setVisibility(Button.GONE);
            } else {
                btnNext.setText(getString(R.string.next) + "\n\n" + chNext.getTitle());
                btnNext.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new GetPageTask().execute(chNext);
                    }
                });
            }

            if (chPrev == null) {
                btnPrev.setVisibility(Button.GONE);
            } else {
                btnPrev.setText(getString(R.string.previous) + "\n\n" + chPrev.getTitle());
                btnPrev.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new GetPageTask().execute(chPrev);
                    }
                });
            }

            super.onActivityCreated(savedInstanceState);
        }

        public class GetPageTask extends AsyncTask<Chapter, Void, Chapter> {
            ProgressDialog asyncDialog = new ProgressDialog(getActivity());
            String error = "";

            @Override
            protected void onPreExecute() {
                asyncDialog.setMessage(getResources().getString(R.string.iniciando));
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Chapter doInBackground(Chapter... arg0) {
                Chapter c = arg0[0];
                ServerBase s = ServerBase.getServer(l.mManga.getServerId());
                try {
                    if (c.getPages() < 1) s.chapterInit(c);
                } catch (Exception e) {
                    error = e.getMessage();
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
                if (error.length() > 1) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                } else {
                    Database.updateChapter(getActivity(), result);
                    DownloadPoolService.agregarDescarga(getActivity(), result, true);
                    Intent intent =
                            new Intent(getActivity(), ActivityLector.class);
                    intent.putExtra(ActivityManga.CAPITULO_ID, result.getId());
                    getActivity().startActivity(intent);
                    Database.updateChapter(l, l.mChapter);
                    l.finish();
                }
                super.onPostExecute(result);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<PageFragment> fragments = new ArrayList<>(6);
        int[] pos = {-1, -1, -1, -1, -1, -1};
        int idx = 0;
        FragmentManager fm = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
            int i = pos.length;
            while (--i >= 0) fragments.add(new PageFragment());
        }

        private int getNextPos() {
            int np = idx % pos.length;
            idx++;
            return np;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment rsta;
            if (mDirection == Direction.R2L || mDirection == Direction.VERTICAL)
                if (position == mChapter.getPages())
                    rsta = mLastPageFrag;
                else {
                    rsta = getFragmentIn(position);
                }
            else {
                if (position == 0) rsta = mLastPageFrag;
                else {
                    int pos = (mChapter.getPages() - position);
                    rsta = getFragmentIn(pos);
                }
            }
            return rsta;
        }

        public Fragment getFragmentIn(int position) {
            PageFragment f = null;
            for (int i = 0; i < pos.length; i++) {
                if (pos[i] == position) {
                    f = fragments.get(i);
                    break;
                }
            }
            if (f == null) {
                String ruta = DownloadPoolService.generarRutaBase(mServerBase, mManga, mChapter,
                        getApplicationContext()) + "/" + (position + 1) + ".jpg";
                int idx;
                do {
                    idx = getNextPos();
                    if (pos[idx] == -1) break;
                } while (pos[idx] + 1 > getCurrentItem() && pos[idx] - 1 < getCurrentItem());
                pos[idx] = position;
                Fragment old = fragments.get(idx);
                fm.beginTransaction().remove(old).commit();
                // old = null;
                fragments.set(idx, PageFragment.newInstance(ruta, ActivityLector.this, position));
                f = fragments.get(idx);
                f.setTapListener(ActivityLector.this);
            }
            return f;
        }

        @Override
        public int getCount() {
            return mChapter.getPages() + 1;
        }

        public void actualizarDisplayTipe() {
            for (PageFragment iterable_element : fragments) {
                if (iterable_element != null) {
                    iterable_element.setDisplayType(mScreenFit);
                }
            }
        }

        public Fragment getCurrentFragment() {
            return getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + R.id.pager + ":" + getCurrentItem());
        }

        public Fragment getIfOnMemory(int idx) {
            Fragment fragment = null;
            for (int i = 0; i < pos.length; i++) {
                if (pos[i] == idx) {
                    fragment = fragments.get(i);
                    break;
                }
            }
            return fragment;
        }

        public void setPageScroll(float nScroll) {
            for (PageFragment pageFragment : fragments) {
                if (pageFragment.visor != null)
                    pageFragment.visor.setScrollFactor(nScroll);
            }
        }
    }
}
