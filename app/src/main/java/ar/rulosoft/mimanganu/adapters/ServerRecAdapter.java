package ar.rulosoft.mimanganu.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class ServerRecAdapter extends RecyclerView.Adapter<ServerRecAdapter.ServerViewHolder> {

    OnServerClickListener onServerClickListener;
    ServerBase[] servers;

    public ServerRecAdapter(ServerBase[] serverBases) {
        this.servers = serverBases;
    }

    public void setOnServerClickListener(OnServerClickListener onServerClickListener) {
        this.onServerClickListener = onServerClickListener;
    }

    public ServerBase getItem(int pos) {
        return servers[0];
    }

    @Override
    public int getItemCount() {
        return servers.length;
    }

    @Override
    public void onBindViewHolder(ServerViewHolder sHolder, int pos) {
        final ServerBase server = servers[pos];
        sHolder.bandera.setImageResource(server.getBandera());
        sHolder.icono.setImageResource(server.getIcon());
        sHolder.titulo.setText(server.getServerName());
        sHolder.v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onServerClickListener != null)
                    onServerClickListener.onServerClick(server);
            }
        });
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_server, viewGroup, false);
        return new ServerViewHolder(v);
    }

    public interface OnServerClickListener {
        void onServerClick(ServerBase server);
    }

    public class ServerViewHolder extends RecyclerView.ViewHolder {
        TextView titulo;
        ImageView bandera;
        ImageView icono;
        View v;

        public ServerViewHolder(View itemView) {
            super(itemView);
            titulo = (TextView) itemView.findViewById(R.id.server_nombre);
            bandera = (ImageView) itemView.findViewById(R.id.server_bandera);
            icono = (ImageView) itemView.findViewById(R.id.server_imagen);
            v = itemView;
        }
    }

}
