package ar.rulosoft.mimanganu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class SearchResultsFragment extends Fragment {
    public static final String TERM = "termino_busqueda";
    private String search_term = "";
    private int serverId;
    private ProgressBar loading;
    private ListView list;
    private PerformSearchTask performSearchTask = new PerformSearchTask();
    private boolean searchPerformed;
    private ArrayList<Manga> mangasFromSearch = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_search_result,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onStart();
        serverId = getArguments().getInt(MainFragment.SERVER_ID);
        search_term = getArguments().getString(TERM);
        list = (ListView) getView().findViewById(R.id.result);
        loading = (ProgressBar) getView().findViewById(R.id.loading);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manga manga = (Manga) list.getAdapter().getItem(position);
                Bundle bundle = new Bundle();
                bundle.putInt(MainFragment.SERVER_ID, serverId);
                bundle.putString(DetailsFragment.TITLE, manga.getTitle());
                bundle.putString(DetailsFragment.PATH, manga.getPath());
                DetailsFragment detailsFragment = new DetailsFragment();
                detailsFragment.setArguments(bundle);
                ((MainActivity) getActivity()).replaceFragment(detailsFragment, "DetailsFragment");
                searchPerformed = true;
            }
        });
        if (searchPerformed) {
            if (list != null)
                list.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mangasFromSearch));
        } else
            performSearchTask = (PerformSearchTask) new PerformSearchTask().execute();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().onBackPressed();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchPerformed) {
            loading.setVisibility(ProgressBar.INVISIBLE);
            ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.search_result, search_term) + " " + ServerBase.getServer(serverId).getServerName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        performSearchTask.cancel(true);
        mangasFromSearch.clear();
    }

    public class PerformSearchTask extends AsyncTask<Void, Void, ArrayList<Manga>> {
        public String error = "";

        @Override
        protected void onPreExecute() {
            if (isAdded()) {
                loading.setVisibility(ProgressBar.VISIBLE);
                ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.searching, search_term) + " " + ServerBase.getServer(serverId).getServerName());
            }
        }

        @Override
        protected ArrayList<Manga> doInBackground(Void... params) {
            ArrayList<Manga> mangas = new ArrayList<>();
            if(isAdded()) {
                ServerBase serverBase = ServerBase.getServer(serverId);
                try {
                    mangas = serverBase.search(search_term);
                } catch (Exception e) {
                    error = e.getMessage();
                }
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(ArrayList<Manga> result) {
            if (isAdded()) {
                loading.setVisibility(ProgressBar.INVISIBLE);
                ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.search_result, search_term) + " " + ServerBase.getServer(serverId).getServerName());
                if (error != null) {
                    if (error.length() < 2) {
                        if (result != null && !result.isEmpty() && list != null) {
                            list.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, result));
                            if (!mangasFromSearch.isEmpty())
                                mangasFromSearch.clear();
                            mangasFromSearch.addAll(result);
                        } else if (result == null || result.isEmpty()) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.busquedanores), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                    }
                }
            }
            super.onPostExecute(result);
        }
    }
}
