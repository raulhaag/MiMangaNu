package ar.rulosoft.mimanganu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.fedorvlasov.lazylist.ImageLoader;

import java.util.List;

import ar.rulosoft.mimanganu.componentes.ControlInfo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class DetailsFragment extends Fragment {

    public static final String TITLE = "titulo_m";
    public static final String PATH = "path_m";
    private static final String TAG = "DetailFragment";
    private String title, path;
    private int id;
    private ImageLoader imageLoader;
    private ControlInfo data;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ServerBase serverBase;
    private Manga manga;
    private FloatingActionButton floatingActionButton_add;
    private LoadDetailsTask loadDetailsTask = new LoadDetailsTask();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        title = getArguments().getString(TITLE);
        path = getArguments().getString(PATH);
        id = getArguments().getInt(MainFragment.SERVER_ID);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        data = (ControlInfo) getView().findViewById(R.id.datos);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.str);
        ActionBar mActBar = getActivity().getActionBar();
        if (mActBar != null) {
            mActBar.setDisplayHomeAsUpEnabled(true);
        }
        floatingActionButton_add = (FloatingActionButton) getView().findViewById(R.id.floatingActionButton_add);
        floatingActionButton_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Manga> mangas = Database.getMangas(getContext(), null, true);
                boolean onDb = false;
                for (Manga m : mangas) {
                    if (m.getPath().contains(manga.getPath()))
                        onDb = true;
                }
                if (!onDb) {
                    new AddMangaTask().execute(manga);
                    AnimatorSet set = new AnimatorSet();
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(floatingActionButton_add, "alpha", 1.0f, 0.0f);
                    anim1.setDuration(0);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    ObjectAnimator anim2 = ObjectAnimator.ofFloat(floatingActionButton_add, "y", displayMetrics.heightPixels);
                    anim2.setDuration(500);
                    set.playSequentially(anim2, anim1);
                    set.start();
                }else{
                    Toast.makeText(getContext(),getString(R.string.already_on_db),Toast.LENGTH_LONG).show();
                }
            }
        });
        floatingActionButton_add.setBackgroundTintList(ColorStateList.valueOf(MainActivity.colors[1]));
        swipeRefreshLayout.setColorSchemeColors(MainActivity.colors[0], MainActivity.colors[1]);
        data.setColor(MainActivity.darkTheme, MainActivity.colors[0]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setNavigationBarColor(MainActivity.colors[0]);
            window.setStatusBarColor(MainActivity.colors[4]);
        }
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.datosde) + " " + title);
        }
        manga = new Manga(id, title, path, false);
        serverBase = ServerBase.getServer(id);
        imageLoader = new ImageLoader(getContext());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDetailsTask = (LoadDetailsTask) new LoadDetailsTask().execute();
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        loadDetailsTask = (LoadDetailsTask) new LoadDetailsTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).enableHomeButton(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loadDetailsTask.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadDetailsTask extends AsyncTask<Void, Void, Void> {
        String error;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                serverBase.loadMangaInformation(manga, true);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    error = e.getMessage();
                else
                    error = e.getLocalizedMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String infoExtra = "";
            if(isAdded()) {
                if (error == null || error.length() < 2) {
                    if (manga.isFinished()) {
                        infoExtra = infoExtra + getResources().getString(R.string.finalizado);
                    } else {
                        infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
                    }
                    data.setStatus(infoExtra);
                    data.setServer(serverBase.getServerName());
                    if (manga.getAuthor() != null && manga.getAuthor().length() > 1) {
                        data.setAuthor(manga.getAuthor());
                    } else {
                        data.setAuthor(getResources().getString(R.string.nodisponible));
                    }
                    if (manga.getGenre() != null && manga.getGenre().length() > 1) {
                        data.setGenre(manga.getGenre());
                    } else {
                        data.setGenre(getResources().getString(R.string.nodisponible));
                    }
                    if (manga.getSynopsis() != null && manga.getSynopsis().length() > 1) {
                        data.setSynopsis(manga.getSynopsis());
                    } else {
                        data.setSynopsis(getResources().getString(R.string.nodisponible));
                    }
                    imageLoader.displayImg(manga.getImages(), data);
                    if (error != null && error.length() > 2) {
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                    } else {
                        AnimatorSet set = new AnimatorSet();
                        ObjectAnimator anim1 = ObjectAnimator.ofFloat(floatingActionButton_add, "alpha", 0.0f, 1.0f);
                        anim1.setDuration(0);
                        float y = floatingActionButton_add.getY();
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(floatingActionButton_add, "y", displayMetrics.heightPixels);
                        anim2.setDuration(0);
                        ObjectAnimator anim3 = ObjectAnimator.ofFloat(floatingActionButton_add, "y", y);
                        anim3.setInterpolator(new AccelerateDecelerateInterpolator());
                        anim3.setDuration(500);
                        set.playSequentially(anim2, anim1, anim3);
                        set.start();
                    }
                } else {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class AddMangaTask extends AsyncTask<Manga, Integer, Void> {
        ProgressDialog adding = new ProgressDialog(getActivity());
        String error = ".";
        int total = 0;
        boolean errorWhileAddingManga;

        @Override
        protected void onPreExecute() {
            adding.setMessage(getResources().getString(R.string.adding_to_db));
            adding.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Manga... params) {
            try {
                serverBase.loadChapters(manga, false);
            } catch (Exception e) {
                error = e.getMessage();
                Log.e(TAG, "Chapter load error", e);
            }
            total = params[0].getChapters().size();
            int mid = Database.addManga(getActivity(), params[0]);
            if (mid > -1) {
                long initTime = System.currentTimeMillis();
                for (int i = 0; i < params[0].getChapters().size(); i++) {
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                    Database.addChapter(getActivity(), params[0].getChapter(i), mid);
                }
            } else {
                errorWhileAddingManga = true;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (isAdded()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adding != null) {
                            adding.setMessage(getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + total);
                        }
                    }
                });
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if(isAdded()) {
                adding.dismiss();
                if(!errorWhileAddingManga)
                    Toast.makeText(getActivity(), getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
                if (error != null && error.length() > 2) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
                getActivity().onBackPressed();
            }
            super.onPostExecute(result);
        }
    }
}
