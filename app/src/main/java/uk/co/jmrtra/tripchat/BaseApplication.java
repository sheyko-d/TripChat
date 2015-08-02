package uk.co.jmrtra.tripchat;

import android.app.Application;
import android.content.Context;

import com.flurry.android.FlurryAgent;


public class BaseApplication extends Application {

    private static Context sContext;

    public static Context getAppContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        FlurryAgent.init(sContext, getString(R.string.flurry_key));
    }

}
