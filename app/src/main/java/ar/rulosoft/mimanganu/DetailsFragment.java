package ar.rulosoft.mimanganu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fedorvlasov.lazylist.ImageLoader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ar.rulosoft.mimanganu.componentes.ControlInfo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.AsyncAddManga;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

public class DetailsFragment extends Fragment {

    public static final String TITLE = "titulo_m";
    public static final String PATH = "path_m";
    public static final String IMG = "img";
    private static final String TAG = "DetailsFragment";
    private String title, path, img;
    private int id;
    private ImageLoader imageLoader;
    private ControlInfo data;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ServerBase serverBase;
    private Manga manga;
    private FloatingActionButton floatingActionButton_add;
    private LoadDetailsTask loadDetailsTask = new LoadDetailsTask();
    private boolean mangaAlreadyAdded;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        title = getArguments().getString(TITLE);
        path = getArguments().getString(PATH);
        img = getArguments().getString(IMG);
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

        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Manga> mangas = Database.getMangas(getContext(), null, true);
                    for (Manga m : mangas) {
                        if (m.getPath().equals(manga.getPath()) && (m.getServerId() == manga.getServerId())) {
                            mangaAlreadyAdded = true;
                            if (floatingActionButton_add != null)
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        floatingActionButton_add.hide();
                                    }
                                });
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                    Util.getInstance().toast(getContext(), Log.getStackTraceString(e));
                }
            }
        });
        t0.start();

        data = getView().findViewById(R.id.datos);
        swipeRefreshLayout = getView().findViewById(R.id.str);
        ActionBar mActBar = getActivity().getActionBar();
        if (mActBar != null) {
            mActBar.setDisplayHomeAsUpEnabled(true);
        }
        floatingActionButton_add = getView().findViewById(R.id.floatingActionButton_add);
        floatingActionButton_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mangaAlreadyAdded) {
                    AsyncAddManga nAsyncAddManga = new AsyncAddManga(manga, getActivity(), getView(), false, false, true);
                    nAsyncAddManga.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    AnimatorSet set = new AnimatorSet();
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(floatingActionButton_add, "alpha", 1.0f, 0.0f);
                    anim1.setDuration(0);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    ObjectAnimator anim2 = ObjectAnimator.ofFloat(floatingActionButton_add, "y", displayMetrics.heightPixels);
                    anim2.setDuration(500);
                    set.playSequentially(anim2, anim1);
                    set.start();
                } else {
                    Util.getInstance().showFastSnackBar(getString(R.string.already_on_db), getView(), getContext());
                }
            }
        });
        int[] colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getActivity()));
        floatingActionButton_add.setBackgroundTintList(ColorStateList.valueOf(colors[1]));
        swipeRefreshLayout.setColorSchemeColors(colors[0], colors[1]);
        data.setColor(MainActivity.darkTheme, colors[0]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.datosde) + " " + title);
        }
        manga = new Manga(id, title, path, false);
        manga.setImages(img);
        data.enableTitleCopy(getActivity(), manga.getTitle());
        serverBase = ServerBase.getServer(id, getContext());
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
        ((MainActivity) getActivity()).enableHomeButton(true);
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
        String error = "";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                manga.getChapters().clear();
                serverBase.loadMangaInformation(manga, true);
            } catch (Exception e) {
                e.printStackTrace();
                error = Log.getStackTraceString(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String infoExtra = "";
            if (isAdded()) {
                if (error.isEmpty()) {
                    if (manga.isFinished()) {
                        infoExtra = infoExtra + getResources().getString(R.string.finalizado);
                    } else {
                        infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
                    }
                    if (mangaAlreadyAdded)
                        data.setStatus(infoExtra + " (" + getString(R.string.already_on_db) + ")");
                    else
                        data.setStatus(infoExtra);
                    String chapters = "";
                    if (manga.getChapters().size() > 0) {
                        chapters = " (" + manga.getChapters().size() + " " + getString(R.string.chapters) + ")";
                    }
                    data.setServer(serverBase.getServerName() + chapters);
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
                    if (!error.isEmpty()) {
                        Util.getInstance().showFastSnackBar(error, getView(), getContext());
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
                    Util.getInstance().showFastSnackBar(error, getView(), getContext());
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

}
