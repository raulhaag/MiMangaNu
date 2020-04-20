package ar.rulosoft.mimanganu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ar.rulosoft.mimanganu.adapters.ServerRecAdapter;
import ar.rulosoft.mimanganu.componentes.MangaFolderSelect;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ServersSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServersSelectFragment extends Fragment implements MainActivity.OnBackListener, ServerRecAdapter.OnEndActionModeListener {

    private ServerRecAdapter serverRecAdapter;
    private RecyclerView server_list;

    public ServersSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ServersFragment.
     */
    public static ServersSelectFragment newInstance() {
        return new ServersSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setAllowEnterTransitionOverlap(true);
        setAllowReturnTransitionOverlap(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_add_manga, container, false);
        server_list = viewGroup.findViewById(R.id.lista_de_servers);
        server_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        serverRecAdapter = new ServerRecAdapter(ServerBase.getServers(getContext()), pm, getActivity());
        serverRecAdapter.setEndActionModeListener(this);
        server_list.setAdapter(serverRecAdapter);
        serverRecAdapter.setOnServerClickListener(new ServerRecAdapter.OnServerClickListener() {
            @Override
            public void onServerClick(final ServerBase server) {
                if (!(server instanceof FromFolder)) {
                    if (server.hasFilteredNavigation()) {
                        ServerFilteredNavigationFragment fragment = new ServerFilteredNavigationFragment();
                        Bundle b = new Bundle();
                        b.putInt(MainFragment.SERVER_ID, server.getServerID());
                        fragment.setArguments(b);
                        ((MainActivity) getActivity()).replaceFragment(fragment, "FilteredNavigation");
                    } else {
                        ServerListFragment fragment = new ServerListFragment();
                        Bundle b = new Bundle();
                        b.putInt(MainFragment.SERVER_ID, server.getServerID());
                        fragment.setArguments(b);
                        ((MainActivity) getActivity()).replaceFragment(fragment, "FilteredServerList");
                    }
                } else {
                    MangaFolderSelect mangaFolderSelect = new MangaFolderSelect();
                    mangaFolderSelect.show(getChildFragmentManager(), "fragment_find_folder");
                }
            }
        });
        return viewGroup;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_server_select, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_edit_server_list:
                serverRecAdapter.startActionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.enableHomeButton(true);
        activity.setTitle(getString(R.string.select_server));
    }

    @Override
    public void onEndActionMode() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }
}
