package com.ppcrong.unity.ahrs;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

import com.lsxiao.apollo.core.Apollo;
import com.lsxiao.apollo.core.annotations.Receive;
import com.lsxiao.apollo.core.contract.ApolloBinder;
import com.ppcrong.unity.ahrs.apollo.BleEvents;
import com.socks.library.KLog;
import com.unity3d.player.UnityPlayer;

public class UnityPlayerActivity extends Activity {

    private ApolloBinder mBinder;
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        mBinder = Apollo.bind(this);

        // TestOnly
//        setMovement(new BleEvents.NotifyAhrsMoveEvent(10, 0, 0));
//        setRotation(new BleEvents.NotifyAhrsRotateEvent(0, 1, 0, 0));
//        doFakeDataTest();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override
    protected void onDestroy() {
        if (mBinder != null) {
            mBinder.unbind();
        }
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override
    protected void onResume() {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    /*API12*/
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    // region [Apollo]
    @Receive("BleEvents.NotifyAhrsMoveEvent")
    public void onNotifyAhrsMoveEvent(BleEvents.NotifyAhrsMoveEvent event) {

        KLog.i("AhrsMove: " + event.toString());
        setMovement(event);
    }

    @Receive("BleEvents.NotifyAhrsRotateEvent")
    public void onNotifyAhrsRotateEvent(BleEvents.NotifyAhrsRotateEvent event) {

        KLog.i("AhrsRotate: " + event.toString());
        setRotation(event);
    }
    // endregion [Apollo]

    // region [Private Function]
    private void setMovement(BleEvents.NotifyAhrsMoveEvent event) {
        UnityPlayer.UnitySendMessage("Helicopter", "setMovement", event.toString());
    }

    private void setRotation(BleEvents.NotifyAhrsRotateEvent event) {
        UnityPlayer.UnitySendMessage("Helicopter", "setRotation", event.toString());
    }
    // endregion [Private Function]

    // region [Task]
    private void doFakeDataTest() {

        KLog.i("start FakeData test");
        mFakeDataTask = new FakeDataTask();
        mFakeDataTask.execute();
    }

    private FakeDataTask mFakeDataTask;
    private float[][] mRotations = new float[][]{
            {0, 0, 0, 1},
            {0.1f, 0, 0, 1},
            {0.2f, 0, 0, 1},
            {0.3f, 0, 0, 1},
            {0.4f, 0, 0, 1},
            {0.5f, 0, 0, 1},
            {0.6f, 0, 0, 1},
            {0.7f, 0, 0, 1},
            {0.8f, 0, 0, 1},
            {0.9f, 0, 0, 1},
            {1, 0, 0, 1},
            {0.9f, 0, 0, 1},
            {0.8f, 0, 0, 1},
            {0.7f, 0, 0, 1},
            {0.6f, 0, 0, 1},
            {0.5f, 0, 0, 1},
            {0.4f, 0, 0, 1},
            {0.3f, 0, 0, 1},
            {0.2f, 0, 0, 1},
            {0.1f, 0, 0, 1},
            {0, 0, 0, 1}
    };
    private float[][] mMovements = new float[][]{
            {0, 0, 0},
            {1, 1, 1},
            {2, 2, 2},
            {3, 3, 3},
            {4, 4, 4},
            {5, 5, 5},
            {6, 6, 6},
            {7, 7, 7},
            {8, 8, 8},
            {9, 9, 9},
            {10, 10, 10},
            {9, 9, 9},
            {8, 8, 8},
            {7, 7, 7},
            {6, 6, 6},
            {5, 5, 5},
            {4, 4, 4},
            {3, 3, 3},
            {2, 2, 2},
            {1, 1, 1},
            {0, 0, 0}
    };

    private class FakeDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            // Wait
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                KLog.i(Log.getStackTraceString(e));
            }

            // Rotation
            for (int i = 0; i < mRotations.length; i++) {

                setRotation(new BleEvents.NotifyAhrsRotateEvent(mRotations[i][0], mRotations[i][1], mRotations[i][2], mRotations[i][3]));
                KLog.i("qx: " + mRotations[i][0] + ", qy: " + mRotations[i][1] + ", qz: " + mRotations[i][2] + ", qw: " + mRotations[i][3]);

                // Wait
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    KLog.i(Log.getStackTraceString(e));
                }
            }

            // Wait
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                KLog.i(Log.getStackTraceString(e));
            }

            // Movement
            for (int i = 0; i < mMovements.length; i++) {

                setMovement(new BleEvents.NotifyAhrsMoveEvent(mMovements[i][0], mMovements[i][1], mMovements[i][2]));
                KLog.i("x: " + mMovements[i][0] + ", y: " + mMovements[i][1] + ", z: " + mMovements[i][2]);

                // Wait
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    KLog.i(Log.getStackTraceString(e));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            KLog.i("FakeData is stopped...");
            super.onPostExecute(aVoid);
        }
    }
}
