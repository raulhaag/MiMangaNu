package ar.rulosoft.mimanganu.componentes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.preference.PreferenceManager;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 17/01/2017.
 */

public class LoginDialog {

    private AlertDialog dialog;
    private EditText username;
    private EditText password;
    private ProgressBar progressBar;
    private Context context;

    public LoginDialog(final Context context, final ServerBase serverBase) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.dialog_login, null);
        username = rootView.findViewById(R.id.txtUsername);
        password = rootView.findViewById(R.id.txtPassword);
        progressBar = rootView.findViewById(R.id.progressBar);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(context.getString(R.string.login_in) + serverBase.getServerName());
        dialogBuilder.setView(rootView);
        dialogBuilder.setPositiveButton(R.string.login, null);
        dialogBuilder.setNegativeButton(context.getString(android.R.string.cancel), null);

        dialog = dialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button accept = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AsyncLogin(username.getText().toString(), password.getText().toString()).execute(serverBase);
                    }
                });
                Button cancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Util.getInstance().toast(context, "This server needs log in to show all chapters");
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void show() {
        dialog.show();
    }

    private class AsyncLogin extends AsyncTask<ServerBase, Void, Boolean> {
        String username;
        String password;
        String serverName;

        AsyncLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

        }

        @Override
        protected Boolean doInBackground(ServerBase... serverBases) {
            try {
                serverName = serverBases[0].getServerName();
                return serverBases[0].testLogin(username, password);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Handler handler = new Handler(Looper.getMainLooper());
            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = pm.edit();
            if (aBoolean) {
                Util.getInstance().toast(context, "Login OK");
                editor.putString("username_" + serverName, username);
                editor.putString("dwp_" + serverName, Util.xorEncode(password, serverName));
                editor.apply();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        dialog.dismiss();
                    }
                });
            } else {
                editor.putString("username_" + serverName, "");
                editor.putString("dwp_" + serverName, "");
                editor.apply();
                Util.getInstance().toast(context, "Login error.");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }
}
