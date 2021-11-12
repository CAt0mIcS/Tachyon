package com.de.mucify;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;

public class MucifyApplication extends Application {
    private static boolean sActivityVisible;

    private static final ArrayList<ActivityVisibilityChangedListener> mActivityVisibilityChangedListeners = new ArrayList<>();

    public interface ActivityVisibilityChangedListener {
        void onVisibilityChanged(Activity activity, boolean becameVisible);
    }

    public static boolean isActivityVisible() {
        return sActivityVisible;
    }

    public static void activityResumed(Activity activity) {
        sActivityVisible = true;
        for(ActivityVisibilityChangedListener listener : mActivityVisibilityChangedListeners)
            listener.onVisibilityChanged(activity, sActivityVisible);
    }

    public static void activityPaused(Activity activity) {
        sActivityVisible = false;
        for(ActivityVisibilityChangedListener listener : mActivityVisibilityChangedListeners)
            listener.onVisibilityChanged(activity, sActivityVisible);
    }

    public static void addOnActivityVisibilityChangedListener(ActivityVisibilityChangedListener listener) {
        mActivityVisibilityChangedListeners.add(listener);
    }
}
