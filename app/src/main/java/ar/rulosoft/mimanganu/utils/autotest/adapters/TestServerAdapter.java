package ar.rulosoft.mimanganu.utils.autotest.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class TestServerAdapter extends RecyclerView.Adapter<TestServerAdapter.ServerViewHolder> {
    public ActionMode actionMode;
    private TestServerAdapter.OnServerClickListener onServerClickListener;
    private ServerBase[] servers;
    private int[] status;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public TestServerAdapter(ServerBase[] serverBases) {
        this.servers = serverBases;
        this.status = new int[serverBases.length];
    }

    public void setOnServerClickListener(TestServerAdapter.OnServerClickListener onServerClickListener) {
        this.onServerClickListener = onServerClickListener;
    }

    public ServerBase getItem(int pos) {
        return servers[0];
    }

    public void setStatus(int idx, int nStatus) {
        status[idx] = nStatus;
    }

    @Override
    public int getItemCount() {
        return servers.length;
    }

    @Override
    public void onBindViewHolder(ServerViewHolder sHolder, final int pos) {
        final ServerBase server = servers[pos];
        sHolder.status.setImageResource(R.drawable.serveruc);
        sHolder.icon.setImageResource(server.getIcon());
        sHolder.title.setText(server.getServerName());
        sHolder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionMode == null) {
                    if (onServerClickListener != null)
                        onServerClickListener.onServerClick(server);
                } else {
                    toggleSelection(pos);
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
                //TODO
                return false;
            }
        });
    }


    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_server, viewGroup, false);
        return new ServerViewHolder(v);
    }

    public List<Integer> stringToIntList(String sl) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] parts = sl.split("\\|");
        for (String s : parts) {
            if (s.trim().length() > 0 && s.matches("\\d+")) {
                result.add(Integer.parseInt(s));
            }
        }
        return result;
    }

    public String intListToString(List<Integer> il) {
        String result = "";
        for (Integer integer : il) {
            result = result + "|" + integer;
        }
        return result;
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public interface OnServerClickListener {
        void onServerClick(ServerBase server);
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


