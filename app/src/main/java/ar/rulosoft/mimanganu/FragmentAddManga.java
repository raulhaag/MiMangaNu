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

public class FragmentAddManga extends Fragment implements OnServerClickListener {
    private RecyclerView lista_server;
    private ServerRecAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_manga, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        lista_server = (RecyclerView) getView().findViewById(R.id.lista_de_servers);
        lista_server.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (((MainActivity) getActivity()).darkTheme) {
            ((CardView) getView().findViewById(R.id.cardview_server_container))
                    .setCardBackgroundColor(getResources()
                            .getColor(R.color.background_floating_material_dark));
        }
        adapter = new ServerRecAdapter(ServerBase.getServers());
        lista_server.setAdapter(adapter);
        adapter.setOnServerClickListener(FragmentAddManga.this);
    }

    @Override
    public void onServerClick(ServerBase server) {
        Intent intent;
        if (server.hasFilteredNavigation()) {
            FragmentServerFilteredNavigation fragment = new FragmentServerFilteredNavigation();
            Bundle b = new Bundle();
            b.putInt(FragmentMainMisMangas.SERVER_ID,server.getServerID());
            fragment.setArguments(b);
            ((MainActivity) getActivity()).replaceFragment(fragment,"FilteredNavegation");
            //intent = new Intent(getActivity(), FragmentServerFilteredNavigation.class);
        }else
            intent = new Intent(getActivity(), ActivityServerMangaList.class);
        //intent.putExtra(FragmentMainMisMangas.SERVER_ID, server.getServerID());
        //getActivity().startActivity(intent);
    }
}
