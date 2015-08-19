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

public class ActivityDetails extends ActionBarActivity {

    public static final String TITLE = "titulo_m";
    public static final String PATH = "path_m";

    ImageLoader imageLoader;
    ControlInfo data;
    SwipeRefreshLayout str;

    ServerBase s;
    Manga m;
    FloatingActionButton button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles);
        data = (ControlInfo) findViewById(R.id.datos);
        str = (SwipeRefreshLayout) findViewById(R.id.str);
        int[] colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        str.setColorSchemeColors(colors[0], colors[1]);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        button_add = (FloatingActionButton) findViewById(R.id.button_add);
        button_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddMangaTask().execute(m);
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
        button_add.attachToScrollView(data);
        data.setColor(colors[0]);
        String title = getIntent().getExtras().getString(TITLE);
        getSupportActionBar().setTitle(getResources().getString(R.string.datosde) + " " + title);
        String path = getIntent().getExtras().getString(PATH);
        int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        m = new Manga(id, title, path, false);
        s = ServerBase.getServer(id);
        imageLoader = new ImageLoader(this.getApplicationContext());
        str.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadDetailsTask().execute();
            }
        });
        str.post(new Runnable() {
            @Override
            public void run() {
                str.setRefreshing(true);
            }
        });
        new LoadDetailsTask().execute();
    }

    private class LoadDetailsTask extends AsyncTask<Void, Void, Void> {

        String error = ".";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                s.loadMangaInformation(m, true);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String infoExtra = "";
            if (error.length() < 2) {
                if (m.isFinished()) {
                    infoExtra = infoExtra + getResources().getString(R.string.finalizado);
                } else {
                    infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
                }
                data.setStatus(infoExtra);
                data.setSynopsis(m.getSynopsis());
                data.setServer(s.getServerName());
                if (m.getAuthor() != null && m.getAuthor().length() > 1) {
                    data.setAuthor(m.getAuthor());
                } else {
                    data.setAuthor(getResources().getString(R.string.nodisponible));
                }
                imageLoader.DisplayImage(m.getImages(), data);
                if (error != null && error.length() > 2) {
                    Toast.makeText(ActivityDetails.this, error, Toast.LENGTH_LONG).show();
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
                Toast.makeText(ActivityDetails.this, error, Toast.LENGTH_LONG).show();
            }
            str.setRefreshing(false);
        }

    }

    public class AddMangaTask extends AsyncTask<Manga, Integer, Void> {
        ProgressDialog adding = new ProgressDialog(ActivityDetails.this);
        String error = ".";
        int total = 0;

        @Override
        protected void onPreExecute() {
            adding.setMessage(getResources().getString(R.string.agregando));
            adding.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Manga... params) {
            try {
                s.loadChapters(m, false);
            } catch (Exception e) {
                error = e.getMessage();
            }
            total = params[0].getChapters().size();
            int mid = Database.addManga(getBaseContext(), params[0]);
            long initTime = System.currentTimeMillis();
            for (int i = 0; i < params[0].getChapters().size(); i++) {
                if (System.currentTimeMillis() - initTime > 500) {
                    publishProgress(i);
                    initTime = System.currentTimeMillis();
                }
                Database.addChapter(ActivityDetails.this, params[0].getChapter(i), mid);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adding != null) {
                        adding.setMessage(getResources().getString(R.string.agregando) + " " + values[0] + "/" + total);
                    }
                }
            });

        }

        @Override
        protected void onPostExecute(Void result) {
            adding.dismiss();
            Toast.makeText(ActivityDetails.this, getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
            if (error != null && error.length() > 2) {
                Toast.makeText(ActivityDetails.this, error, Toast.LENGTH_LONG).show();
            }
            try {
                onBackPressed();
            } catch (Exception e) {

            }
            super.onPostExecute(result);
        }
    }
}
