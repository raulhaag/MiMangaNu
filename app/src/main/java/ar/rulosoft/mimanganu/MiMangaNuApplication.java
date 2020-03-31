package ar.rulosoft.mimanganu;

import android.app.Application;
import android.content.Context;

import com.evernote.android.job.JobManager;

import ar.rulosoft.mimanganu.services.UpdateJobCreator;
import ar.rulosoft.mimanganu.utils.CustomExceptionHandler;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by jtx on 28.05.2016.
 */

/*@ReportsCrashes(
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUri = "https://collector.tracepot.com/3928c326"
)*/
public class MiMangaNuApplication extends Application {
    public MiMangaNuApplication() {
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //   ACRA.init(this);
        CustomExceptionHandler.Attach(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // don't delete this, this is used to detect memory leaks ~xtj9182
        //LeakCanary.install(this);
        JobManager.create(this).addJobCreator(new UpdateJobCreator());
        if (JobManager.instance().getAllJobRequestsForTag(UpdateJobCreator.UPDATE_TAG).isEmpty()) {
            UpdateJobCreator.UpdateJob.scheduleJob(getApplicationContext());
        }
        Util.createNotificationChannels(getApplicationContext());
    }

}
