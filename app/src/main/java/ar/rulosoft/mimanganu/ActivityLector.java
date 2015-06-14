package ar.rulosoft.mimanganu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.ActivityManga.Direccion;
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

public class ActivityLector extends ActionBarActivity implements DownloadListener, OnSeekBarChangeListener, TapListener, OnErrorListener {

    public static final String KEEP_SCREEN_ON = "keep_on";
    public static final String ORIENTATION = "orientation";
    public static final String AJUSTE_KEY = "ajustar_a";
    public static int MAX_TEXTURE = 2048;

    public static DisplayType AJUSTE_PAGINA = DisplayType.FIT_TO_WIDTH;
    public Direccion direccion = Direccion.VERTICAL;
    public InitialPosition iniPosition = InitialPosition.LEFT_UP;
    SectionsPagerAdapter mSectionsPagerAdapter;
    UnScrolledViewPager mViewPager;
    UnScrolledViewPagerVertical mViewPagerV;
    LinearLayout seeker_Layout;
    SeekBar seekBar;
    Toolbar actionToolbar;
    Chapter chapter;
    boolean controlVisible = false;
    UltimaPaginaFragment ultimaPaginaFragment;
    Manga manga;
    ServerBase s;
    TextView seekerPage;
    MenuItem displayMenu, keepOnMenuItem, screenRotationMenuItem;
    SharedPreferences pm;
    //0 = normal  | 1 = screen on
    int keepOn;
    //0 = free | 1 = landscape | 2 = portrait
    int orientation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AJUSTE_PAGINA = DisplayType.valueOf(pm.getString(AJUSTE_KEY, DisplayType.FIT_TO_WIDTH.toString()));
        MAX_TEXTURE = Integer.parseInt(pm.getString("max_texture", "2048"));
        keepOn = pm.getInt(KEEP_SCREEN_ON, 0);
        orientation = pm.getInt(ORIENTATION, 0);
        chapter = Database.getChapter(this, getIntent().getExtras().getInt(ActivityManga.CAPITULO_ID));
        manga = Database.getFullManga(this, chapter.getMangaID());
        if (manga.getReadingDirection() != -1)
            direccion = Direccion.values()[manga.getReadingDirection()];
        else
            direccion = Direccion.values()[Integer.parseInt(pm.getString(ActivityManga.DIRECCION, "" + Direccion.R2L.ordinal()))];
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
            int anterior = -1;

            @Override
            public void onPageSelected(int arg0) {
                if (anterior < arg0) {
                    iniPosition = InitialPosition.LEFT_UP;
                } else {
                    iniPosition = InitialPosition.LEFT_BOTTOM;
                }
                anterior = arg0;
                if (direccion == Direccion.R2L || direccion == Direccion.VERTICAL)
                    if (arg0 < chapter.getPages())
                        chapter.setPagesRead(arg0 + 1);
                    else {
                        chapter.setPagesRead(arg0);
                    }
                else
                    chapter.setPagesRead(chapter.getPages() - arg0 + 1);

                // if (actionBar.isShowing()) {
                if (direccion == Direccion.R2L || direccion == Direccion.VERTICAL)
                    seekBar.setProgress(arg0);
                else
                    seekBar.setProgress(chapter.getPages() - arg0);
                // }
                if (arg0 >= chapter.getPages() - 1) {
                    chapter.setReadStatus(Chapter.READ);
                    Database.updateChapter(ActivityLector.this, chapter);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        };

        if (direccion == Direccion.VERTICAL) {
            setContentView(R.layout.activity_lector_v);
            mViewPagerV = (UnScrolledViewPagerVertical) findViewById(R.id.pager);
            mViewPagerV.setOnPageChangeListener(pageChangeListener);
        } else {
            setContentView(R.layout.activity_lector);
            mViewPager = (UnScrolledViewPager) findViewById(R.id.pager);
            mViewPager.setOnPageChangeListener(pageChangeListener);
        }
        seekBar = (SeekBar) findViewById(R.id.seeker);
        seeker_Layout = (LinearLayout) findViewById(R.id.seeker_layout);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(chapter.getPages());
        actionToolbar = (Toolbar) findViewById(R.id.action_bar);
        actionToolbar.setTitle(chapter.getTitle());
        actionToolbar.setTitleTextColor(Color.WHITE);
        s = ServerBase.getServer(manga.getServerId());
        if (DownloadPoolService.actual != null)
            DownloadPoolService.actual.setDownloadListener(this);
        ultimaPaginaFragment = new UltimaPaginaFragment();
        seekerPage = (TextView) findViewById(R.id.page);
        actionToolbar.setAlpha(0);
        seeker_Layout.setAlpha(0f);
        actionToolbar.setVisibility(View.GONE);
        seeker_Layout.setVisibility(View.GONE);

        int[] colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            actionToolbar.setBackgroundDrawable(new ColorDrawable(colors[0]));
            seeker_Layout.setBackgroundDrawable(new ColorDrawable(colors[0]));
            seekBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
        } else {
            actionToolbar.setBackground(new ColorDrawable(colors[0]));
            seeker_Layout.setBackground(new ColorDrawable(colors[0]));
            seekBar.setBackground(new ColorDrawable(colors[0]));
        }
        chapter.setReadStatus(Chapter.READING);
        Database.updateChapter(ActivityLector.this, chapter);
        setSupportActionBar(actionToolbar);
    }

    @Override
    public void onBackPressed() {
        if (!controlVisible)
            super.onBackPressed();
        else
            onCenterTap();
    }

    public void actualizarIcono(DisplayType displayType) {
        if (displayMenu != null)
            switch (displayType) {
                case NONE:
                    displayMenu.setIcon(R.drawable.ic_action_original);
                    break;
                case FIT_TO_HEIGHT:
                    displayMenu.setIcon(R.drawable.ic_action_ajustar_alto);
                    break;
                case FIT_TO_WIDTH:
                    displayMenu.setIcon(R.drawable.ic_action_ajustar_ancho);
                    break;
                case FIT_TO_SCREEN:
                    displayMenu.setIcon(R.drawable.ic_action_ajustar_diagonal);
                    break;
                default:
                    break;
            }
    }

    @Override
    protected void onResume() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        if (direccion == Direccion.VERTICAL)
            mViewPagerV.setAdapter(mSectionsPagerAdapter);
        else
            mViewPager.setAdapter(mSectionsPagerAdapter);

        if (chapter.getPagesRead() > 1) {
            if (direccion == Direccion.R2L)
                mViewPager.setCurrentItem(chapter.getPagesRead() - 1);
            else if (direccion == Direccion.VERTICAL)
                mViewPagerV.setCurrentItem(chapter.getPagesRead() - 1);
            else
                mViewPager.setCurrentItem(chapter.getPages() - chapter.getPagesRead() + 1);
        } else {
            if (direccion == Direccion.L2R)
                mViewPager.setCurrentItem(chapter.getPages() + 1);
        }
        DownloadPoolService.attachListener(this, chapter.getId());
        super.onResume();
    }

    public void setAdapter(SectionsPagerAdapter adapter) {
        if (direccion == Direccion.VERTICAL)
            mViewPagerV.setAdapter(adapter);
        else
            mViewPager.setAdapter(adapter);
    }

    public int getCurrentItem() {
        if (direccion == Direccion.VERTICAL)
            return mViewPagerV.getCurrentItem();
        else
            return mViewPager.getCurrentItem();
    }

    public void setCurrentItem(int pos) {
        if (direccion == Direccion.VERTICAL)
            mViewPagerV.setCurrentItem(pos);
        else
            mViewPager.setCurrentItem(pos);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
        Database.UpdateChapterPage(ActivityLector.this, chapter.getId(), chapter.getPagesRead());
    }

    @Override
    protected void onPause() {
        Database.UpdateChapterPage(ActivityLector.this, chapter.getId(), chapter.getPagesRead());
        DownloadPoolService.detachListener(chapter.getId());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lector, menu);
        displayMenu = menu.findItem(R.id.action_ajustar);
        keepOnMenuItem = menu.findItem(R.id.action_keep_screen_on);
        screenRotationMenuItem = menu.findItem(R.id.action_orientation);
        if (keepOn > 0) {
            keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_on);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (orientation == 1) {
            ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_landscape);
        } else if (orientation == 2) {
            ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_portrait);
        }
        actualizarIcono(AJUSTE_PAGINA);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ajustar) {
            AJUSTE_PAGINA = AJUSTE_PAGINA.getNext();
            SharedPreferences.Editor editor = pm.edit();
            editor.putString(AJUSTE_KEY, AJUSTE_PAGINA.toString()).commit();
            editor.commit();
            mSectionsPagerAdapter.actualizarDisplayTipe();
            actualizarIcono(AJUSTE_PAGINA);
            return true;
        } else if (id == R.id.action_keep_screen_on) {
            if (keepOn == 0) {
                keepOn = 1;
                keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_on);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                keepOn = 0;
                keepOnMenuItem.setIcon(R.drawable.ic_action_mantain_screen_off);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            SharedPreferences.Editor editor = pm.edit();
            editor.putInt(KEEP_SCREEN_ON, keepOn);
            editor.commit();
            return true;
        } else if (id == R.id.action_orientation) {
            if (orientation == 0) {
                orientation = 1;
                ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_landscape);
            } else if (orientation == 1) {
                orientation = 2;
                ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_portrait);
            } else if (orientation == 2) {
                orientation = 0;
                ActivityLector.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                screenRotationMenuItem.setIcon(R.drawable.ic_action_screen_free);
            }
            SharedPreferences.Editor editor = pm.edit();
            editor.putInt(ORIENTATION, orientation);
            editor.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImagenDescargada(final int cid, final int pagina) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = mSectionsPagerAdapter.getIfOnMemory(pagina);
                if (fragment != null && !((PlaceholderFragment) fragment).imageLoaded) {
                    ((PlaceholderFragment) fragment).setImagen();
                }
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekerPage != null)
            seekerPage.setText("" + (progress + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (direccion == Direccion.R2L || direccion == Direccion.VERTICAL)
            seekerPage.setText("" + (getCurrentItem() + 1));
        else {
            seekerPage.setText("" + (chapter.getPages() - getCurrentItem()));
        }
        seekerPage.setText("" + getCurrentItem());
        seekerPage.setVisibility(SeekBar.VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekerPage.setVisibility(SeekBar.INVISIBLE);
        if (direccion == Direccion.R2L || direccion == Direccion.VERTICAL)
            setCurrentItem(seekBar.getProgress());
        else {
            setCurrentItem(chapter.getPages() - seekBar.getProgress());
        }
    }

    @Override
    public void onCenterTap() {

        if (controlVisible) {
            controlVisible = false;
            ObjectAnimator anim = ObjectAnimator.ofFloat(actionToolbar, "alpha", .90f, 0f);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    actionToolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            anim.start();
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(seeker_Layout, "alpha", .90f, 0f);
            anim2.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    seeker_Layout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            anim2.start();
        } else {
            controlVisible = true;
            ObjectAnimator anim = ObjectAnimator.ofFloat(actionToolbar, "alpha", 0f, .90f);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    actionToolbar.setVisibility(View.VISIBLE);
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
            anim.start();
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(seeker_Layout, "alpha", 0f, .90f);
            anim2.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    seeker_Layout.setVisibility(View.VISIBLE);
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
        if (act > 0) {
            setCurrentItem(--act);
        }
    }

    @Override
    public void onRightTap() {
        int a = mSectionsPagerAdapter.getCount();
        int act = getCurrentItem();
        if (act < a) {
            setCurrentItem(++act);
        }

    }

    @Override
    public void onError(final Chapter chapter) {
        ActivityLector.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(ActivityLector.this).setTitle(chapter.getTitle() + " " + getString(R.string.error))
                            .setMessage(getString(R.string.demaciados_errores)).setIcon(R.drawable.ic_launcher)
                            .setNeutralButton(getString(android.R.string.ok), null).show();
                } catch (Exception e) {
                    // lost references fixed con detachListener
                }
            }
        });
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String RUTA = "ruta";
        public ImageViewTouch visor;
        ActivityLector activity;
        ProgressBar cargando;
        TapListener mTapListener;
        Runnable r = null;
        boolean imageLoaded = false;
        private String ruta = null;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(String ruta, ActivityLector activity) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(RUTA, ruta);
            fragment.activity = activity;
            fragment.setArguments(args);
            fragment.setRetainInstance(false);
            return fragment;
        }

        public void setTapListener(TapListener nTapListener) {
            mTapListener = nTapListener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_activity_lector_pagina, container, false);
            visor = (ImageViewTouch) rootView.findViewById(R.id.visor);
            if (r != null) {
                new Thread(r).start();
            } else
                visor.setDisplayType(AJUSTE_PAGINA);
            visor.setTapListener(mTapListener);
            visor.setScaleEnabled(false);
            cargando = (ProgressBar) rootView.findViewById(R.id.cargando);
            cargando.bringToFront();
            if (getArguments() != null)
                ruta = getArguments().getString(RUTA);

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
            visor = (ImageViewTouch) getView().findViewById(R.id.visor);
            if (visor == null) {
                cargando.setVisibility(ProgressBar.VISIBLE);
            } else if (ruta != null)
                setImagen();

            super.onResume();
        }

        @Override
        public void onPause() {
            try {
                ((BitmapDrawable) visor.getDrawable()).getBitmap().recycle();
            } catch (Exception exception) {

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

        public void setImagen() {
            if (!imageLoaded && visor != null)
                new SetImagen().execute();
        }

        public void setImagen(String ruta) {
            this.ruta = ruta;
            setImagen();
        }

        public class SetImagen extends AsyncTask<Void, Void, Bitmap> {

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

            @SuppressLint("NewApi")
            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    imageLoaded = true;
                    visor.setScaleEnabled(true);
                    if (activity.direccion == Direccion.VERTICAL)
                        visor.setInitialPosition(activity.iniPosition);
                    else
                        visor.setInitialPosition(InitialPosition.LEFT_UP);
                    if ((result.getHeight() > MAX_TEXTURE || result.getWidth() > MAX_TEXTURE) && Build.VERSION.SDK_INT >= 11) {
                        visor.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                    visor.setImageBitmap(result);
                    cargando.setVisibility(ProgressBar.INVISIBLE);
                } else if (ruta != null) {
                    File f = new File(ruta);
                    if (f.exists()) {
                        f.delete();
                    }
                    /*
                     * if (!ColaDeDescarga.corriendo) {
					 * ColaDeDescarga.add(((ActivityLector)
					 * getActivity()).capitulo);
					 * ColaDeDescarga.iniciarCola(getActivity()); }/
					 */
                }
                super.onPostExecute(result);
            }

        }

    }

    public static class UltimaPaginaFragment extends Fragment {

        Button b1, b2;
        Chapter c1 = null, c2 = null;
        ActivityLector l;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rView = inflater.inflate(R.layout.fragment_pagina_final, container, false);
            b1 = (Button) rView.findViewById(R.id.button1);
            b2 = (Button) rView.findViewById(R.id.button2);
            return rView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            l = (ActivityLector) getActivity();
            int cid = l.chapter.getId();
            ArrayList<Chapter> caps = l.manga.getChapters();
            for (int i = 0; i < caps.size(); i++) {
                if (caps.get(i).getId() == cid) {
                    if (i > 0) {
                        c1 = caps.get(i - 1);
                    }
                    if (i < caps.size() - 1) {
                        c2 = caps.get(i + 1);
                    }
                }
            }

            if (c1 == null) {
                b1.setVisibility(Button.INVISIBLE);
            } else {
                b1.setText(c1.getTitle() + ">");
            }

            if (c2 == null) {
                b2.setVisibility(Button.INVISIBLE);
            } else {
                b2.setText(c2.getTitle() + ">");
            }

            b1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    new GetPaginas().execute(c1);
                }
            });

            b2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    new GetPaginas().execute(c2);
                }
            });

            super.onActivityCreated(savedInstanceState);
        }

        public class GetPaginas extends AsyncTask<Chapter, Void, Chapter> {
            ProgressDialog asyncdialog = new ProgressDialog(getActivity());
            String error = "";

            @Override
            protected void onPreExecute() {
                asyncdialog.setMessage(getResources().getString(R.string.iniciando));
                asyncdialog.show();
                super.onPreExecute();
            }

            @Override
            protected Chapter doInBackground(Chapter... arg0) {
                Chapter c = arg0[0];
                ServerBase s = ServerBase.getServer(l.manga.getServerId());
                try {
                    if (c.getPages() < 1)
                        s.chapterInit(c);
                } catch (Exception e) {
                    error = e.getMessage();
                    e.printStackTrace();
                } finally {
                    onProgressUpdate();
                }
                return c;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                asyncdialog.dismiss();
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Chapter result) {
                if (error.length() > 1) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                } else {
                    asyncdialog.dismiss();
                    Database.updateChapter(getActivity(), result);
                    DownloadPoolService.agregarDescarga(getActivity(), result, true);
                    Intent intent = new Intent(getActivity(), ActivityLector.class);
                    intent.putExtra(ActivityManga.CAPITULO_ID, result.getId());
                    getActivity().startActivity(intent);
                    Database.updateChapter(l, l.chapter);
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

        ArrayList<PlaceholderFragment> fragments = new ArrayList<>(6);
        int[] pos = {-1, -1, -1, -1, -1, -1};
        int idx = 0;
        FragmentManager fm = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
            for (int i = 0; i < pos.length; i++) {
                fragments.add(new PlaceholderFragment());
            }
        }

        private int getNextPos() {
            int np = idx % pos.length;
            idx++;
            return np;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment rsta;
            if (direccion == Direccion.R2L || direccion == Direccion.VERTICAL)
                if (position == chapter.getPages())
                    rsta = ultimaPaginaFragment;
                else {
                    rsta = getFragmentIn(position);
                }
            else {
                if (position == 0)
                    rsta = ultimaPaginaFragment;
                else {
                    int pos = (chapter.getPages() - position);
                    rsta = getFragmentIn(pos);
                }
            }
            return rsta;
        }

        public Fragment getFragmentIn(int position) {
            PlaceholderFragment f = null;
            for (int i = 0; i < pos.length; i++) {
                if (pos[i] == position) {
                    f = fragments.get(i);
                    break;
                }
            }
            if (f == null) {
                String ruta = DownloadPoolService.generarRutaBase(s, manga, chapter, getApplicationContext()) + "/" + (position + 1) + ".jpg";
                int idx;
                do {
                    idx = getNextPos();
                    if (pos[idx] == -1)
                        break;
                } while (pos[idx] + 1 > getCurrentItem() && pos[idx] - 1 < getCurrentItem());
                pos[idx] = position;
                Fragment old = fragments.get(idx);
                fm.beginTransaction().remove(old).commit();
                old = null;
                fragments.set(idx, PlaceholderFragment.newInstance(ruta, ActivityLector.this));
                f = fragments.get(idx);
                f.setTapListener(ActivityLector.this);
            }
            return f;
        }

        @Override
        public int getCount() {
            return chapter.getPages() + 1;
        }

        public void actualizarDisplayTipe() {
            for (PlaceholderFragment iterable_element : fragments) {
                if (iterable_element != null) {
                    iterable_element.setDisplayType(AJUSTE_PAGINA);
                }
            }
        }

        public Fragment getCurrentFragment() {
            return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + getCurrentItem());
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

    }
}
