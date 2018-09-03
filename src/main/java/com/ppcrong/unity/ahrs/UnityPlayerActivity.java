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

    private static final String GAME_OBJECT_HELICOPTER = "Helicopter";
    private static final String GAME_OBJECT_BAT = "BatNewCenter";
    private static final String PLAYER_NAME = GAME_OBJECT_BAT;
    private static final String MAIN_CAMERA = "MainCamera";
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

        initUnityPlayer();
        initUnityCamera();

        // TestOnly
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
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        mUnityPlayer.pause();
        if (mBinder != null) {
            mBinder.unbind();
        }
        onBackPressed();
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
//        setMovement(event);
    }

    @Receive("BleEvents.NotifyAhrsRotateQuaternionEvent")
    public void onNotifyAhrsRotateQuaternionEvent(BleEvents.NotifyAhrsRotateQuaternionEvent event) {

        KLog.i("AhrsRotateQuaternion: " + event.toString() + ",0,0,0");
//        setRotation(event, new BleEvents.NotifyAhrsRotateEularEvent());
    }

    @Receive("BleEvents.NotifyAhrsRotateEularEvent")
    public void onNotifyAhrsRotateEularEvent(BleEvents.NotifyAhrsRotateEularEvent event) {

        KLog.i("AhrsRotateEular: " + "0,0,0,0," + event.toString());
        setRotation(new BleEvents.NotifyAhrsRotateQuaternionEvent(), event);
    }
    // endregion [Apollo]

    // region [Private Function]
    private void setRotation(BleEvents.NotifyAhrsRotateQuaternionEvent eventQ,
                             BleEvents.NotifyAhrsRotateEularEvent eventE) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setRotation",
                eventQ.toString() + "," + eventE.toString());
    }

    private void setMovement(BleEvents.NotifyAhrsMoveEvent event) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setMovement", event.toString());
    }

    private void setRotateInterpolant(float interpolant) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setRotateInterpolant", Float.toString(interpolant));
    }

    private void setMoveInterpolant(float interpolant) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setMoveInterpolant", Float.toString(interpolant));
    }

    /*
     * Rotate type
     * 0: Direct Quaternion
     * 1: Smooth Quaternion
     * 2: Direct Eular
     * 3: Smooth Eular
     */
    private void setRotateType(int type) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setRotateType", Integer.toString(type));
    }

    /*
     * Move type
     * 0: Direct
     * 1: Smooth
     */
    private void setMoveType(int type) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setMoveType", Integer.toString(type));
    }

    private void setMoveScale(float scale) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "setMoveScale", Float.toString(scale));
    }

    private void setIsFollowPlayer(boolean b) {
        UnityPlayer.UnitySendMessage(MAIN_CAMERA, "setIsFollowPlayer", b ? "True" : "False");
    }

    private void setCamMoveSmoothTime(float time) {
        UnityPlayer.UnitySendMessage(MAIN_CAMERA, "setMoveSmoothTime", Float.toString(time));
    }

    private void setCamMoveInterpolant(float interpolant) {
        UnityPlayer.UnitySendMessage(MAIN_CAMERA, "setMoveInterpolant", Float.toString(interpolant));
    }

    private void initUnityPlayer() {

        // Set interpolant
        setRotateInterpolant(0.8f);
        setMoveInterpolant(0.1f);

        // Set type
        setRotateType(0);
        setMoveType(1);

        // Set movement scale
        setMoveScale(1f);
    }

    private void initUnityCamera() {

        setIsFollowPlayer(false);
        setCamMoveSmoothTime(0.05f);
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
            {11, 11, 11},
            {12, 12, 12},
            {13, 13, 13},
            {14, 14, 14},
            {15, 15, 15},
            {16, 16, 16},
            {17, 17, 17},
            {18, 18, 18},
            {19, 19, 19},
            {20, 20, 20},
            {21, 21, 21},
            {22, 22, 22},
            {23, 23, 23},
            {24, 24, 24},
            {25, 25, 25},
            {26, 26, 26},
            {27, 27, 27},
            {28, 28, 28},
            {29, 29, 29},
            {30, 30, 30},
            {31, 31, 31},
            {32, 32, 32},
            {33, 33, 33},
            {34, 34, 34},
            {33, 33, 33},
            {32, 32, 32},
            {31, 31, 31},
            {30, 30, 30},
            {29, 29, 29},
            {28, 28, 28},
            {27, 27, 27},
            {26, 26, 26},
            {25, 25, 25},
            {24, 24, 24},
            {23, 23, 23},
            {22, 22, 22},
            {21, 21, 21},
            {20, 20, 20},
            {19, 19, 19},
            {18, 18, 18},
            {17, 17, 17},
            {16, 16, 16},
            {15, 15, 15},
            {14, 14, 14},
            {13, 13, 13},
            {12, 12, 12},
            {11, 11, 11},
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

                setRotation(new BleEvents.NotifyAhrsRotateQuaternionEvent(mRotations[i][0], mRotations[i][1], mRotations[i][2], mRotations[i][3]),
                        new BleEvents.NotifyAhrsRotateEularEvent());
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
