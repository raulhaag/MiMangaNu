package ar.rulosoft.mimanganu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityCapitulos extends ActionBarActivity {

    public static final String DIRECCION = "direcciondelectura";
    public static final String ORDEN = "ordendecapitulos";
    public static final String CAPITULO_ID = "cap_id";
    public static Orden cOrden;
    public Manga manga;
    public Direccion direccion;
    SectionsPagerAdapter mSectionsPagerAdapter;
    int[] colors;

    PagerTabStrip pagerStrip;
    FragmentCapitulos fragmentCapitulos;
    FragmentDetalles fragmentDetalles;
    FragmentDescarga fragmentDescarga;
    SharedPreferences pm;
    ViewPager mViewPager;
    MenuItem sentido, mOrden;
    SetCapitulos listenerCapitulos;
    int id;
    ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_capitulos);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        fragmentCapitulos = new FragmentCapitulos();
        fragmentCapitulos.setRetainInstance(true);
        listenerCapitulos = fragmentCapitulos;

        fragmentDetalles = new FragmentDetalles();
        fragmentDetalles.setRetainInstance(true);

        fragmentDescarga = new FragmentDescarga();

        mSectionsPagerAdapter.add(fragmentCapitulos);
        mSectionsPagerAdapter.add(fragmentDetalles);
        mSectionsPagerAdapter.add(fragmentDescarga);

        id = getIntent().getExtras().getInt(ActivityMisMangas.MANGA_ID, -1);
        if (id == -1) {
            onBackPressed();
            finish();
        }
        colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_strip);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(colors[0]);
        pagerTabStrip.setBackgroundColor(colors[1]);
        pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int orden = Integer.parseInt(pm.getString(ORDEN, "" + Orden.DSC.ordinal()));
        cOrden = Orden.values()[orden];
    }

    @Override
    protected void onResume() {
        super.onResume();
        manga = Database.getFullManga(getApplicationContext(), id, cOrden == Orden.ASD);
        listenerCapitulos.onCalpitulosCargados(this, manga.getChapters());
        fragmentDetalles.m = manga;
        Database.updateMangaRead(this, manga.getId());
        Database.updateNewMangas(ActivityCapitulos.this, manga, -100);
        BuscarNuevo.onActivityResumed(ActivityCapitulos.this);
    }

    @Override
    protected void onPause() {
        BuscarNuevo.onActivityPaused();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_capitulos, menu);
        sentido = menu.findItem(R.id.action_sentido);
        mOrden = menu.findItem(R.id.action_orden);
        int direccion;
        if (manga.getReadingDirection() != -1) {
            direccion = manga.getReadingDirection();
        } else {
            direccion = Integer.parseInt(pm.getString(DIRECCION, "" + Direccion.R2L.ordinal()));
        }

        if (direccion == Direccion.R2L.ordinal()) {
            this.direccion = Direccion.R2L;
            sentido.setIcon(R.drawable.ic_action_clasico);
        } else if (direccion == Direccion.L2R.ordinal()) {
            this.direccion = Direccion.L2R;
            sentido.setIcon(R.drawable.ic_action_inverso);
        } else {
            this.direccion = Direccion.VERTICAL;
            sentido.setIcon(R.drawable.ic_action_verical);
        }
        if (cOrden == Orden.DSC) {
            mOrden.setIcon(R.drawable.ic_action_9a1);
        } else {
            mOrden.setIcon(R.drawable.ic_action_1a9);
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_descargar_restantes) {
            ArrayList<Chapter> chapters = Database.getChapters(ActivityCapitulos.this, ActivityCapitulos.this.id, Database.COL_CAP_DESCARGADO + " != 1",
                    true);
            Chapter[] arr = new Chapter[chapters.size()];
            arr = chapters.toArray(arr);
            new DascargarDemas().execute(arr);
            // TODO mecanimos mostrar progreso
            return true;
        } else if (id == R.id.action_marcar_todo_leido) {
            Database.markAllChapters(ActivityCapitulos.this, this.id, true);
            manga = Database.getFullManga(getApplicationContext(), this.id, cOrden == Orden.ASD);
            listenerCapitulos.onCalpitulosCargados(this, manga.getChapters());
        } else if (id == R.id.action_marcar_todo_no_leido) {
            Database.markAllChapters(ActivityCapitulos.this, this.id, false );
            manga = Database.getFullManga(getApplicationContext(), this.id, cOrden == Orden.ASD);
            listenerCapitulos.onCalpitulosCargados(this, manga.getChapters());
        } else if (id == R.id.action_buscarnuevos) {
            new BuscarNuevo().setActivity(ActivityCapitulos.this).execute(manga);
        } else if (id == R.id.action_sentido) {
            // TODO check database
            int direccion;
            if (manga.getReadingDirection() != -1) {
                direccion = manga.getReadingDirection();
            } else {
                direccion = Integer.parseInt(pm.getString(DIRECCION, "" + Direccion.R2L.ordinal()));
            }
            if (direccion == Direccion.R2L.ordinal()) {
                sentido.setIcon(R.drawable.ic_action_inverso);
                this.direccion = Direccion.L2R;
            } else if (direccion == Direccion.L2R.ordinal()) {
                sentido.setIcon(R.drawable.ic_action_verical);
                this.direccion = Direccion.VERTICAL;
            } else {
                sentido.setIcon(R.drawable.ic_action_clasico);
                this.direccion = Direccion.R2L;
            }
            manga.setReadingDirection(this.direccion.ordinal());
            Database.updadeSentidoLectura(ActivityCapitulos.this, this.direccion.ordinal(), manga.getId());

        } else if (id == R.id.action_orden) {
            if (cOrden == Orden.DSC) {
                mOrden.setIcon(R.drawable.ic_action_1a9);
                cOrden = Orden.ASD;
            } else if (cOrden == Orden.ASD) {
                mOrden.setIcon(R.drawable.ic_action_9a1);
                cOrden = Orden.DSC;
            }
            manga = Database.getFullManga(getApplicationContext(), this.id, cOrden == Orden.ASD);
            listenerCapitulos.onCalpitulosCargados(this, manga.getChapters());
            pm.edit().putString(ORDEN, "" + cOrden.ordinal()).commit();
        } else if (id == R.id.action_descargar_no_leidos) {
            ArrayList<Chapter> chapters = Database.getChapters(ActivityCapitulos.this, ActivityCapitulos.this.id, Database.COL_CAP_STATE + " < 1", true);
            Chapter[] arr = new Chapter[chapters.size()];
            arr = chapters.toArray(arr);
            new DascargarDemas().execute(arr);
        }
        return super.onOptionsItemSelected(item);
    }

    public enum Direccion {
        L2R, R2L, VERTICAL
    }

    public enum Orden {
        ASD, DSC
    }

    public interface SetCapitulos {
        void onCalpitulosCargados(Activity c, ArrayList<Chapter> chapters);
    }

    public static class BuscarNuevo extends AsyncTask<Manga, String, Integer> {

        static boolean running = false;
        static BuscarNuevo actual = null;
        Activity activity;
        ProgressDialog progreso;
        int mangaId = 0;
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
        protected Integer doInBackground(Manga... params) {
            int result = 0;
            Database.removerCapitulosHuerfanos(activity);
            ServerBase s = ServerBase.getServer(params[0].getServerId());
            mangaId = params[0].getId();
            try {
                onProgressUpdate(params[0].getTitle());
                params[0].clearChapters();
                s.cargarCapitulos(params[0],true);
                int diff = s.buscarNuevosCapitulos(params[0].getId(), activity);
                result += diff;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (((ActivityCapitulos) activity).fragmentCapitulos != null && result > 0) {
                Manga manga = Database.getFullManga(activity, mangaId, cOrden == Orden.ASD);
                ((ActivityCapitulos) activity).fragmentCapitulos.onCalpitulosCargados(activity, manga.getChapters());
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
        private String tabs[] = new String[]{getResources().getString(R.string.capitulos), getResources().getString(R.string.info),
                getResources().getString(R.string.descargas)};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
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

        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

    }

    public class DascargarDemas extends AsyncTask<Chapter, Void, Void> {
        private ServerBase server;
        private Context context;

        @Override
        protected void onPreExecute() {
            server = ServerBase.getServer(ActivityCapitulos.this.manga.getServerId());
            context = getApplicationContext();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Chapter... chapters) {
            for (Chapter c : chapters) {
                try {
                    server.iniciarCapitulo(c);
                    Database.updateChapter(context, c);
                    ServicioColaDeDescarga.agregarDescarga(ActivityCapitulos.this, c, false);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }

    }
}
