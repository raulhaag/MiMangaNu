package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ar.rulosoft.mimanganu.utils.UpdateUtil;

public class MessageActivity extends AppCompatActivity {
    public static final String MESSAGE_VALUE = "message_value";
    public static final String MESSAGE_UPDATE = "message_update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (getIntent().getStringExtra(MESSAGE_VALUE).equals(MESSAGE_UPDATE)) {
            UpdateUtil.generateUpdateDialog(MessageActivity.this);
        } else {
            onBackPressed();
        }
    }
}