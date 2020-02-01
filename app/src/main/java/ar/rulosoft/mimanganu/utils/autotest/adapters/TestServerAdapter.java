package ar.rulosoft.mimanganu.utils.autotest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class TestServerAdapter extends RecyclerView.Adapter<TestServerAdapter.ServerViewHolder> {
    private TestServerAdapter.OnServerClickListener onServerClickListener;
    private ServerBase[] servers;
    private int[] status;

    public TestServerAdapter(ServerBase[] serverBases) {
        this.servers = serverBases;
        this.status = new int[serverBases.length];
    }

    public void setOnServerClickListener(TestServerAdapter.OnServerClickListener onServerClickListener) {
        this.onServerClickListener = onServerClickListener;
    }

    public ServerBase getItem(int pos) {
        return servers[pos];
    }

    public int setStatus(ServerBase sb, int nStatus) {
        int idx = Arrays.asList(servers).indexOf(sb);
        if (idx >= 0) {
            status[idx] = nStatus;
            notifyItemChanged(idx);
        }
        return idx;
    }

    @Override
    public int getItemCount() {
        return servers.length;
    }

    @Override
    public void onBindViewHolder(ServerViewHolder sHolder, final int pos) {
        final ServerBase server = servers[pos];
        switch (status[pos]) {
            case 0:
                sHolder.status.setImageResource(R.drawable.serveruc);
                break;
            case 1:
                sHolder.status.setImageResource(R.drawable.serverwn);
                break;
            case 2:
                sHolder.status.setImageResource(R.drawable.serverko);
                break;
            case 3:
                sHolder.status.setImageResource(R.drawable.serverok);
                break;
        }
        sHolder.icon.setImageResource(server.getIcon());
        sHolder.title.setText(server.getServerName());
        sHolder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onServerClickListener != null) {
                    onServerClickListener.onServerClick(pos);
                }
            }
        });

        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) sHolder.v.getLayoutParams();
        param.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        param.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        sHolder.v.setVisibility(View.VISIBLE);
        sHolder.v.setLayoutParams(param);
        sHolder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onServerClickListener != null) {
                    onServerClickListener.onServerLongClick(pos);
                }
                return false;
            }
        });
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_server, viewGroup, false);
        return new ServerViewHolder(v);
    }

    public interface OnServerClickListener {
        void onServerClick(int pos);

        void onServerLongClick(int pos);
    }

    public class ServerViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView status;
        ImageView icon;
        View v;

        public ServerViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.server_nombre);
            status = itemView.findViewById(R.id.server_bandera);
            icon = itemView.findViewById(R.id.server_imagen);
            v = itemView;
        }
    }
}


