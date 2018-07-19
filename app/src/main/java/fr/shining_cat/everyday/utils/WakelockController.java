package fr.shining_cat.everyday.utils;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public abstract class WakelockController {

    private static final String WAKELOCK_TAG = "EveryDay wakelock";

    private static PowerManager.WakeLock mWakeLock;

    public static void acquire(Context context, long timeout) {
        Log.d("LOGGING::", "WakelockController::acquire");
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Log.d("LOGGING::", "WakelockController::acquire::Releasing old wakelock first...");
            mWakeLock.release();
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            mWakeLock.acquire(timeout);
            Log.d("LOGGING::", "WakelockController::acquire::WAKELOCK acquired, will expire in : " + timeout/1000 + "s");
        }else{
            Log.e("LOGGING::", "WakelockController::acquire::POWERMANAGER IS NULL!!");
        }
    }

    public static void release() {
        if (mWakeLock != null) {
            if(mWakeLock.isHeld()) {
                mWakeLock.release();
                Log.d("LOGGING::", "WakelockController::release::wakelock RELEASED");
            }else{
                Log.d("LOGGING::", "WakelockController::release::wakelock was NOT HELD");
            }
        }else{
            Log.d("LOGGING::", "WakelockController::release::NO wakelock to release");
        }
        mWakeLock = null;
    }


}
