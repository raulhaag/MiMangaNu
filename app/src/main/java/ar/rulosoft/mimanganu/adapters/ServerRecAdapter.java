package ar.rulosoft.mimanganu.adapters;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class ServerRecAdapter extends RecyclerView.Adapter<ServerRecAdapter.ServerViewHolder> implements ActionMode.Callback {
    private SharedPreferences pm;
    private FragmentActivity mActivity;
    private OnServerClickListener onServerClickListener;
    private ServerBase[] servers;
    private List<Integer> unused_servers;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    public ActionMode actionMode;
    private OnEndActionModeListener endActionModeListener;

    public ServerRecAdapter(ServerBase[] serverBases, SharedPreferences pm, FragmentActivity mActivity) {
        this.servers = serverBases;
        this.pm = pm;
        this.mActivity = mActivity;
        unused_servers = stringToIntList(pm.getString("unused_servers", ""));
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
    public void onBindViewHolder(ServerViewHolder sHolder, final int pos) {
        final ServerBase server = servers[pos];
        sHolder.flag.setImageResource(server.getFlag());
        sHolder.icon.setImageResource(server.getIcon());
        sHolder.title.setText(server.getServerName());
        sHolder.v.setOnClickListener(new OnClickListener() {
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
        if (!unused_servers.contains(server.getServerID()) || actionMode != null) {
            param.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            param.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            sHolder.v.setVisibility(View.VISIBLE);
            if (actionMode != null && selectedItems.get(pos)) {
                sHolder.title.setAlpha(0.3f);
                sHolder.icon.setAlpha(0.3f);
                sHolder.flag.setAlpha(0.3f);
            } else {
                sHolder.title.setAlpha(1f);
                sHolder.icon.setAlpha(1f);
                sHolder.flag.setAlpha(1f);
            }
        } else {
            sHolder.v.setVisibility(View.GONE);
            param.height = 0;
            param.width = 0;
        }
        sHolder.v.setLayoutParams(param);
        sHolder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return startActionMode();
            }
        });
    }

    public boolean startActionMode(){
        if (actionMode != null) {
            return false;
        }
        actionMode = mActivity.startActionMode(ServerRecAdapter.this);
        return false;
    }


    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_server, viewGroup, false);
        return new ServerViewHolder(v);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.listitem_server_menu_cab, menu);
        for (Integer sid : unused_servers) {
            for (int i = 0; i < servers.length; i++) {
                if (servers[i].getServerID() == sid) {
                    toggleSelection(i);
                    break;
                }
            }
        }
        mode.setTitle(mActivity.getString(R.string.edit_server_list));
        notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            unused_servers = new ArrayList<>();
            for (Integer i : getSelectedItems()) {
                unused_servers.add(servers[i].getServerID());
            }
            clearSelections();
            actionMode.finish();
            actionMode = null;
            pm.edit().putString("unused_servers", intListToString(unused_servers)).apply();
            notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        clearSelections();
        if(endActionModeListener != null){
            endActionModeListener.onEndActionMode();
        }
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

    public void setEndActionModeListener(OnEndActionModeListener endActionModeListener) {
        this.endActionModeListener = endActionModeListener;
    }

    public interface OnServerClickListener {
        void onServerClick(ServerBase server);
    }

    public class ServerViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView flag;
        ImageView icon;
        View v;

        public ServerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.server_nombre);
            flag = (ImageView) itemView.findViewById(R.id.server_bandera);
            icon = (ImageView) itemView.findViewById(R.id.server_imagen);
            v = itemView;
        }
    }

    public interface OnEndActionModeListener{
        void onEndActionMode();
    }

}
