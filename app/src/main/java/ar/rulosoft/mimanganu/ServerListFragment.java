package ar.rulosoft.mimanganu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import ar.rulosoft.mimanganu.adapters.MangaAdapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class ServerListFragment extends Fragment {

    private int id = -1;
    private ServerBase serverBase;
    private ListView list;
    private ProgressBar loading;
    private MangaAdapter adapter;
    private LoadMangasTask loadMangasTask = new LoadMangasTask();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        return inflater.inflate(R.layout.frament_server_plain_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onStart();
        if (id == -1)
            id = getArguments().getInt(MainFragment.SERVER_ID);
        serverBase = ServerBase.getServer(id);
        list = (ListView) getView().findViewById(R.id.lista_de_mangas);
        loading = (ProgressBar) getView().findViewById(R.id.loading);
        if (adapter == null) {
            loadMangasTask = (LoadMangasTask) new LoadMangasTask().execute();
        } else {
            list.setAdapter(adapter);
            loading.setVisibility(View.INVISIBLE);
        }
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manga m = (Manga) list.getAdapter().getItem(position);
                Bundle bundle = new Bundle();
                bundle.putInt(MainFragment.SERVER_ID, serverBase.getServerID());
                bundle.putString(DetailsFragment.TITLE, m.getTitle());
                bundle.putString(DetailsFragment.PATH, m.getPath());
                DetailsFragment detailsFragment = new DetailsFragment();
                detailsFragment.setArguments(bundle);
                ((MainActivity) getActivity()).replaceFragment(detailsFragment, "DetailsFragment");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.listaen) + " " + serverBase.getServerName());
        ((MainActivity)getActivity()).enableHomeButton(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manga_server, menu);
        MenuItem search;
        search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (adapter != null)
                    adapter.getFilter().filter(s);
                return false;
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loadMangasTask.cancel(true);
    }

    private class LoadMangasTask extends AsyncTask<Void, Void, List<Manga>> {

        String error = "";

        @Override
        protected void onPreExecute() {
            loading.setVisibility(ProgressBar.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected List<Manga> doInBackground(Void... params) {
            List<Manga> mangas = null;
            try {
                mangas = serverBase.getMangas();
            } catch (Exception e) {
                if (e.getMessage() != null)
                    error = e.getMessage();
                else
                    error = e.toString();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(List<Manga> result) {
            if (list != null && result != null && !result.isEmpty() && isAdded()) {
                adapter = new MangaAdapter(getContext(), result, MainActivity.darkTheme);
                list.setAdapter(adapter);
            }
            if (error != null && error.length() > 2 && isAdded()) {
                Toast.makeText(getActivity(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
            loading.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}
