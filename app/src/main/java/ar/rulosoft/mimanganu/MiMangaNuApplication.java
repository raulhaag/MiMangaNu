package ar.rulosoft.mimanganu;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by jtx on 28.05.2016.
 */

@ReportsCrashes(
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUri = "https://collector.tracepot.com/3928c326"
)
public class MiMangaNuApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }

    @Override public void onCreate() {
        super.onCreate();
        // don't delete this, this is used to detect memory leaks ~xtj9182
        //LeakCanary.install(this);
    }

    public MiMangaNuApplication(){
    }

}
