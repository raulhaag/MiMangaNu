package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class ServerAdapter extends ArrayAdapter<ServerBase> {

    static final int resource = R.layout.listitem_server;
    private LayoutInflater li;


    public ServerAdapter(Context context, ServerBase[] objects) {
        super(context, resource, objects);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = li.inflate(resource, null);
        TextView titulo = (TextView) itemView.findViewById(R.id.server_nombre);
        ImageView bandera = (ImageView) itemView.findViewById(R.id.server_bandera);
        ImageView icono = (ImageView) itemView.findViewById(R.id.server_imagen);

        ServerBase item = getItem(position);
        titulo.setText(item.getServerName());
        bandera.setImageResource(item.getBandera());
        icono.setImageResource(item.getIcon());

        return itemView;
    }

}
