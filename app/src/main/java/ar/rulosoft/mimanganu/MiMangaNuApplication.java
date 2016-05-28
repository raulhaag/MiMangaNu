package ar.rulosoft.mimanganu;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by jtx on 28.05.2016.
 */

//note: this is the information for Smileupps.com, we might switch to them later.
//formUri = "http://acra-a01afb.smileupps.com/acra-myapp-656d8e/_design/acra-storage/_update/report",
//formUriBasicAuthLogin = "mimanganu",
//formUriBasicAuthPassword = "hsZ!378AX_"

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

    public MiMangaNuApplication(){
    }

}
