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
import ar.rulosoft.mimanganu.servers.DeNineMangaCom;
import ar.rulosoft.mimanganu.servers.EsMangaHere;
import ar.rulosoft.mimanganu.servers.EsNineMangaCom;
import ar.rulosoft.mimanganu.servers.HeavenMangaCom;
import ar.rulosoft.mimanganu.servers.ItNineMangaCom;
import ar.rulosoft.mimanganu.servers.KissManga;
import ar.rulosoft.mimanganu.servers.LectureEnLigne;
import ar.rulosoft.mimanganu.servers.MangaEdenIt;
import ar.rulosoft.mimanganu.servers.MangaFox;
import ar.rulosoft.mimanganu.servers.MangaHere;
import ar.rulosoft.mimanganu.servers.MangaPanda;
import ar.rulosoft.mimanganu.servers.MangaReader;
import ar.rulosoft.mimanganu.servers.Manga_Tube;
import ar.rulosoft.mimanganu.servers.MyMangaIo;
import ar.rulosoft.mimanganu.servers.RuNineMangaCom;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.servers.SubManga;
import ar.rulosoft.mimanganu.servers.TusMangasOnlineCom;

public class FragmentAddManga extends Fragment implements OnServerClickListener {
    private RecyclerView lista_server;
    private ServerRecAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_add_manga, container, false);
        lista_server = (RecyclerView) rView.findViewById(R.id.lista_de_servers);
        lista_server.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (((ActivityMisMangas) getActivity()).darkTheme) {
            ((CardView) rView.findViewById(R.id.cardview_server_container))
                    .setCardBackgroundColor(getResources()
                            .getColor(R.color.background_floating_material_dark));
        }
        return rView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        adapter = new ServerRecAdapter(new ServerBase[]{
                new HeavenMangaCom(),
                new SubManga(),
                new EsNineMangaCom(),
                new EsMangaHere(),
                new TusMangasOnlineCom(),
                new MangaPanda(),
                new MangaHere(),
                new MangaFox(),
                new MangaReader(),
                new KissManga(),
                new RuNineMangaCom(),
                new LectureEnLigne(),
                new MyMangaIo(),
                new ItNineMangaCom(),
                new MangaEdenIt(),
                new DeNineMangaCom(),
                new Manga_Tube()
        });
        lista_server.setAdapter(adapter);
        adapter.setOnServerClickListener(FragmentAddManga.this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onServerClick(ServerBase server) {
        Intent intent;
        if (server.hasFilteredNavigation())
            intent = new Intent(getActivity(), ActivityServerFilteredNavigation.class);
        else
            intent = new Intent(getActivity(), ActivityServerMangaList.class);
        intent.putExtra(ActivityMisMangas.SERVER_ID, server.getServerID());
        getActivity().startActivity(intent);
    }
}
