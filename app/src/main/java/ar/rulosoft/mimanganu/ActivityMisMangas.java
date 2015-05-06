package ar.rulosoft.mimanganu;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;


import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.MasMangasPageTransformer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityMisMangas extends ActionBarActivity implements OnClickListener {

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    public static final String MOSTRAR_EN_GALERIA = "mostrarengaleria";

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    public int[] colors;
    FragmentMisMangas fragmentMisMangas;
    FragmentAddManga fragmentAddManga;
    SharedPreferences pm;
    FloatingActionButton button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mis_mangas);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        fragmentAddManga = new FragmentAddManga();
        fragmentMisMangas = new FragmentMisMangas();

        fragmentAddManga.setRetainInstance(true);

        mSectionsPagerAdapter.add(fragmentMisMangas);
        mSectionsPagerAdapter.add(fragmentAddManga);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new MasMangasPageTransformer());

        button_add = (FloatingActionButton) findViewById(R.id.button_add);
        button_add.setOnClickListener(this);
        pm = PreferenceManager.getDefaultSharedPreferences(ActivityMisMangas.this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mis_mangas, menu);
        MenuItem menuEsconderSinLectura = menu.findItem(R.id.action_esconder_leidos);
        boolean checkedLeidos = pm.getInt(FragmentMisMangas.SELECTOR_MODO, FragmentMisMangas.MODO_ULTIMA_LECTURA_Y_NUEVOS) > 0;
        menuEsconderSinLectura.setChecked(checkedLeidos);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_buscarnuevos) {
            if (!BuscarNuevo.running)
                new BuscarNuevo().setActivity(ActivityMisMangas.this).execute();
            return true;
        } else if (id == R.id.licencia) {
            Intent intent = new Intent(this, ActivityLicencia.class);
            startActivity(intent);
        } else if (id == R.id.action_esconder_leidos) {
            if (item.isChecked()) {
                item.setChecked(false);
                pm.edit().putInt(FragmentMisMangas.SELECTOR_MODO, FragmentMisMangas.MODO_ULTIMA_LECTURA_Y_NUEVOS).commit();
            } else {
                item.setChecked(true);
                pm.edit().putInt(FragmentMisMangas.SELECTOR_MODO, FragmentMisMangas.MODO_SIN_LEER).commit();
            }
            try {
                fragmentMisMangas.cargarMangas();
            } catch (Exception e) {
                e.printStackTrace();
                // TODO
            }
        } else if (id == R.id.action_configurar) {
            startActivity(new Intent(ActivityMisMangas.this, OpcionesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            onClick(button_add);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        BuscarNuevo.onActivityPaused();
        super.onPause();
    }

    @Override
    protected void onResume() {
        colors = ThemeColors.getColors(pm, getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        button_add.setColorNormal(colors[1]);
        button_add.setColorPressed(colors[3]);
        button_add.setColorRipple(colors[0]);
        BuscarNuevo.onActivityResumed(ActivityMisMangas.this);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (mViewPager.getCurrentItem() == 0) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", 360.0f, 315.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(1);
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
        }
    }

    public static class BuscarNuevo extends AsyncTask<Void, String, Integer> {

        static boolean running = false;
        static BuscarNuevo actual = null;
        Activity activity;
        ProgressDialog progreso;
        String msg;

        public static void onActivityPaused() {
            if (running && actual.progreso != null)
                actual.progreso.dismiss();
        }

        public static void onActivityResumed(Activity actvt) {
            if (running && actual != null) {
                actual.progreso = new ProgressDialog(actvt);
                actual.progreso.setCancelable(false);
                actual.progreso.setMessage(actual.msg);
                actual.progreso.show();
            }
        }

        public BuscarNuevo setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        @Override
        protected void onPreExecute() {
            running = true;
            actual = this;
            progreso = new ProgressDialog(activity);
            progreso.setCancelable(false);
            msg = activity.getResources().getString(R.string.buscandonuevo);
            progreso.setTitle(msg);
            progreso.show();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            final String s = values[0];
            msg = s;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progreso.setMessage(s);
                }
            });
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            ArrayList<Manga> mangas = Database.getMangasForUpdates(activity);
            int result = 0;
            Database.removerCapitulosHuerfanos(activity);
            for (int i = 0; i < mangas.size(); i++) {
                Manga manga = mangas.get(i);
                ServerBase s = ServerBase.getServer(manga.getServerId());
                try {
                    onProgressUpdate(manga.getTitulo());
                    s.cargarCapitulos(manga, false);
                    int diff = s.buscarNuevosCapitulos(manga, activity);
                    result += diff;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                if (((ActivityMisMangas) activity).fragmentMisMangas != null && result > 0)
                    ((ActivityMisMangas) activity).fragmentMisMangas.cargarMangas();
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
            if (progreso != null && progreso.isShowing()) {
                try {
                    progreso.dismiss();
                } catch (Exception e) {
                }
            }
            running = false;
            actual = null;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragments;
        private String tabs[] = new String[]{getResources().getString(R.string.mismangas), getResources().getString(R.string.masmangas)};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<Fragment>();
        }

        public void add(Fragment f) {
            fragments.add(f);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public float getPageWidth(int position) {
            return 1f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }

}
