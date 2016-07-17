package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 17/07/2016.
 */
public class AsyncAddManga extends AsyncTask<Manga,Void,Void> {
    Context mContext;
    String error;
    boolean allOk = true;
    Manga m;


    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Manga... mangas) {
        m = mangas[0];
        ServerBase serverBase = ServerBase.getServer(m.getServerId());
        try {
            serverBase.loadMangaInformation(m, false);
            serverBase.loadChapters(m, false);
            int mid = Database.addManga(mContext, m);
            for (int i = 0; i < m.getChapters().size(); i++) {
                 Database.addChapter(mContext, m.getChapter(i), mid);
            }
        }catch (Exception e){
            allOk = false;
            if(e.getMessage() != null){
                error = e.getMessage();
            }else{
                error = "NullPointerException";
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(mContext!=null){
            if(!allOk){
                Toast.makeText(mContext,error,Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mContext,mContext.getString(R.string.agregado) + " " + m.getTitle(),Toast.LENGTH_SHORT).show();
            }
        }
        super.onPostExecute(aVoid);
    }
}
