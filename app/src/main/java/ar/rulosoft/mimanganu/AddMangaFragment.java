package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ar.rulosoft.mimanganu.adapters.ServerRecAdapter;
import ar.rulosoft.mimanganu.adapters.ServerRecAdapter.OnServerClickListener;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class AddMangaFragment extends Fragment implements OnServerClickListener {
    private RecyclerView server_list;
    private ServerRecAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_manga, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        server_list = (RecyclerView) getView().findViewById(R.id.lista_de_servers);
        server_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (((MainActivity) getActivity()).darkTheme) {
            ((CardView) getView().findViewById(R.id.cardview_server_container))
                    .setCardBackgroundColor(getResources()
                            .getColor(R.color.background_floating_material_dark));
        }
        adapter = new ServerRecAdapter(ServerBase.getServers());
        server_list.setAdapter(adapter);
        adapter.setOnServerClickListener(AddMangaFragment.this);
    }

    @Override
    public void onServerClick(ServerBase server) {
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

    }
}
