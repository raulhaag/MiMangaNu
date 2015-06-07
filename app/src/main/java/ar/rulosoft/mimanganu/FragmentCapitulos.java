package ar.rulosoft.mimanganu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import ar.rulosoft.mimanganu.ActivityCapitulos.SetCapitulos;
import ar.rulosoft.mimanganu.adapters.ChapterAdapter;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;

public class FragmentCapitulos extends Fragment implements SetCapitulos {

    ListView lista;
    ChapterAdapter capitulosAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_capitulos, container, false);
        lista = (ListView) rView.findViewById(R.id.lista);
        return rView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        lista.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chapter c = (Chapter) lista.getAdapter().getItem(position);
                new GetPaginas().execute(c);
            }
        });

        lista.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lista.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                capitulosAdapter.clearSelection();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.listitem_capitulo_menu_cab, menu);
                final ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
                try {
                    final Field contextView = actionBar.getClass().getDeclaredField("mContextView");
                    ((View) contextView.get(actionBar)).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FEFEFE")));
                } catch (final Exception ignored) {
                    // Nothing to do
                }

                return true;

            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                SparseBooleanArray selection = capitulosAdapter.getSelection();
                Manga manga = ((ActivityCapitulos) getActivity()).manga;
                ServerBase s = ServerBase.getServer(manga.getServerId());

                switch (item.getItemId()) {
                    case R.id.seleccionar_todo:
                        capitulosAdapter.selectAll();
                        return true;
                    case R.id.seleccionar_nada:
                        capitulosAdapter.clearSelection();
                        return true;
                    case R.id.borrar_imagenes:
                        for (int i = 0; i < selection.size(); i++) {
                            Chapter c = capitulosAdapter.getItem(selection.keyAt(i));
                            c.freeSpace(getActivity(), manga, s);
                        }
                        break;
                    case R.id.borrar:
                        int[] selecionados = new int[selection.size()];
                        for (int j = 0; j < selection.size(); j++) {
                            selecionados[j] = selection.keyAt(j);
                        }
                        Arrays.sort(selecionados);
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter c = capitulosAdapter.getItem(selection.keyAt(i));
                            c.delete(getActivity(), manga, s);
                            capitulosAdapter.remove(c);

                        }
                        break;
                    case R.id.reset:
                        for (int i = 0; i < selection.size(); i++) {
                            Chapter c = capitulosAdapter.getItem(selection.keyAt(i));
                            c.reset(getActivity(), manga, s);
                        }
                        break;
                    case R.id.marcar_leido:
                        for (int i = selection.size() - 1; i >= 0; i--) {
                            Chapter c = capitulosAdapter.getItem(selection.keyAt(i));
                            c.markRead(getActivity());
                        }
                        break;
                }
                capitulosAdapter.notifyDataSetChanged();
                mode.finish();
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    capitulosAdapter.setNewSelection(position, true);
                } else {
                    capitulosAdapter.removeSelection(position);
                }
            }
        });


        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        lista.setAdapter(capitulosAdapter);
        lista.setSelection(((ActivityCapitulos) getActivity()).manga.getLastIndex());
    }

    @Override
    public void onPause() {
        int first = lista.getFirstVisiblePosition();
        Database.updateMangaLastIndex(getActivity(), ((ActivityCapitulos) getActivity()).manga.getId(), first);
        super.onPause();
    }

    @Override
    public void onCalpitulosCargados(Activity c, ArrayList<Chapter> chapters) {
        capitulosAdapter = new ChapterAdapter(c, chapters);
        if (lista != null) {
            lista.setAdapter(capitulosAdapter);
            lista.setSelection(((ActivityCapitulos) getActivity()).manga.getLastIndex());
        }
    }

    private class GetPaginas extends AsyncTask<Chapter, Void, Chapter> {
        ProgressDialog asyncdialog = new ProgressDialog(getActivity());
        String error = "";

        @Override
        protected void onPreExecute() {
            try {
                asyncdialog.setMessage(getResources().getString(R.string.iniciando));
                asyncdialog.show();
            } catch (Exception e) {
                //prevent dialog error
            }
        }

        @Override
        protected Chapter doInBackground(Chapter... arg0) {
            Chapter c = arg0[0];
            ServerBase s = ServerBase.getServer(((ActivityCapitulos) getActivity()).manga.getServerId());
            try {
                if (c.getPaginas() < 1)
                    s.iniciarCapitulo(c);
            } catch (Exception e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                onProgressUpdate();
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            asyncdialog.dismiss();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Chapter result) {
            if (error != null && error.length() > 1) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            } else {
                asyncdialog.dismiss();
                Database.updateChapter(getActivity(), result);
                ServicioColaDeDescarga.agregarDescarga(getActivity(), result, true);
                int first = lista.getFirstVisiblePosition();
                Database.updateMangaLastIndex(getActivity(), ((ActivityCapitulos) getActivity()).manga.getId(), first);
                Intent intent = new Intent(getActivity(), ActivityLector.class);
                intent.putExtra(ActivityCapitulos.CAPITULO_ID, result.getId());
                getActivity().startActivity(intent);
            }
            super.onPostExecute(result);
        }
    }
}
