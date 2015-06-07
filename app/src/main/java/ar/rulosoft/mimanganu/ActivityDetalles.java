package ar.rulosoft.mimanganu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.fedorvlasov.lazylist.ImageLoader;
import com.melnykov.fab.FloatingActionButton;

import ar.rulosoft.mimanganu.componentes.ControlInfo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityDetalles extends ActionBarActivity {

    public static final String TITULO = "titulo_m";
    public static final String PATH = "path_m";

    ImageLoader imageLoader;
    ControlInfo datos;
    SwipeRefreshLayout str;

    ServerBase s;
    Manga m;
    FloatingActionButton button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles);
        datos = (ControlInfo) findViewById(R.id.datos);
        str = (SwipeRefreshLayout) findViewById(R.id.str);
        int[] colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        str.setColorSchemeColors(colors[0], colors[1]);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        button_add = (FloatingActionButton) findViewById(R.id.button_add);
        button_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AgregaManga().execute(m);
                AnimatorSet set = new AnimatorSet();
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(button_add, "alpha", 1.0f, 0.0f);
                anim1.setDuration(0);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(button_add, "y", displayMetrics.heightPixels);
                anim2.setDuration(500);
                set.playSequentially(anim2, anim1);
                set.start();
            }
        });
        button_add.setColorNormal(colors[1]);
        button_add.setColorPressed(colors[3]);
        button_add.setColorRipple(colors[0]);
        button_add.attachToScrollView(datos);
        datos.setColor(colors[0]);
        String titulo = getIntent().getExtras().getString(TITULO);
        getSupportActionBar().setTitle(getResources().getString(R.string.datosde) + " " + titulo);
        String path = getIntent().getExtras().getString(PATH);
        int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        m = new Manga(id, titulo, path, false);
        s = ServerBase.getServer(id);
        imageLoader = new ImageLoader(this.getApplicationContext());
        str.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new CargarDetalles().execute();
            }
        });
        str.post(new Runnable() {
            @Override
            public void run() {
                str.setRefreshing(true);
            }
        });
        new CargarDetalles().execute();
    }

    private class CargarDetalles extends AsyncTask<Void, Void, Void> {

        String error = ".";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                s.cargarPortada(m, true);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String infoExtra = "";
            if (error.length() < 2) {
                if (m.isFinalizado()) {
                    infoExtra = infoExtra + getResources().getString(R.string.finalizado);
                } else {
                    infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
                }
                datos.setEstado(infoExtra);
                datos.setSinopsis(m.getSinopsis());
                datos.setServidor(s.getServerName());
                if (m.getAutor() != null && m.getAutor().length() > 1) {
                    datos.setAutor(m.getAutor());
                } else {
                    datos.setAutor(getResources().getString(R.string.nodisponible));
                }
                imageLoader.DisplayImage(m.getImages(), datos);
                if (error != null && error.length() > 2) {
                    Toast.makeText(ActivityDetalles.this, error, Toast.LENGTH_LONG).show();
                } else {
                    AnimatorSet set = new AnimatorSet();
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(button_add, "alpha", 0.0f, 1.0f);
                    anim1.setDuration(0);
                    float y = button_add.getY();
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    ObjectAnimator anim2 = ObjectAnimator.ofFloat(button_add, "y", displayMetrics.heightPixels);
                    anim2.setDuration(0);
                    ObjectAnimator anim3 = ObjectAnimator.ofFloat(button_add, "y", y);
                    anim3.setInterpolator(new AccelerateDecelerateInterpolator());
                    anim3.setDuration(500);
                    set.playSequentially(anim2, anim1, anim3);
                    set.start();
                }
            } else {
                Toast.makeText(ActivityDetalles.this, error, Toast.LENGTH_LONG).show();
            }
            str.setRefreshing(false);
        }

    }

    public class AgregaManga extends AsyncTask<Manga, Integer, Void> {
        ProgressDialog agregando = new ProgressDialog(ActivityDetalles.this);
        String error = ".";
        int total = 0;

        @Override
        protected void onPreExecute() {
            agregando.setMessage(getResources().getString(R.string.agregando));
            agregando.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Manga... params) {
            try {
                s.cargarCapitulos(m, false);
            } catch (Exception e) {
                error = e.getMessage();
            }
            total = params[0].getChapters().size();
            int mid = Database.addManga(getBaseContext(), params[0]);
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < params[0].getChapters().size(); i++) {
                if (System.currentTimeMillis() - initTime > 500) {
                    onProgressUpdate(i);
                    initTime = System.currentTimeMillis();
                }
                Database.addChapter(ActivityDetalles.this, params[0].getCapitulo(i), mid);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (agregando != null) {
                        agregando.setMessage(getResources().getString(R.string.agregando) + " " + values[0] + "/" + total);
                    }
                }
            });

        }

        @Override
        protected void onPostExecute(Void result) {
            agregando.dismiss();
            Toast.makeText(ActivityDetalles.this, getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
            if (error != null && error.length() > 2) {
                Toast.makeText(ActivityDetalles.this, error, Toast.LENGTH_LONG).show();
            }
            try {
                onBackPressed();
            } catch (Exception e) {

            }
            super.onPostExecute(result);
        }
    }
}
