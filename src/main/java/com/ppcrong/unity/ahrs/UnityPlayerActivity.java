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
    @SuppressWarnings("deprecation") // ACTION_MULTIPLE is deprecated since API29 (Android Q)
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

    @Receive("BleEvents.NotifyAhrsRotateQuaternionEvent")
    public void onNotifyAhrsRotateQuaternionEvent(BleEvents.NotifyAhrsRotateQuaternionEvent event) {

        KLog.i("AhrsRotateQuaternion: " + event.toString() + ",0,0,0");
//        setRotation(event, new BleEvents.NotifyAhrsRotateEulerEvent());
    }

    @Receive("BleEvents.NotifyAhrsRotateEulerEvent")
    public void onNotifyAhrsRotateEulerEvent(BleEvents.NotifyAhrsRotateEulerEvent event) {

        KLog.i("AhrsRotateEuler: " + "0,0,0,0," + event.toString());
        setRotation(new BleEvents.NotifyAhrsRotateQuaternionEvent(), event);
    }
    // endregion [Apollo]

    // region [Private Function]
    private void setRotation(BleEvents.NotifyAhrsRotateQuaternionEvent eventQ,
                             BleEvents.NotifyAhrsRotateEulerEvent eventE) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetRotation",
                eventQ.toString() + "," + eventE.toString());
    }

    private void setMovement(BleEvents.NotifyAhrsMoveEvent event) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetMovement", event.toString());
    }

    private void setRotateInterpolant(float interpolant) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetRotateInterpolant", Float.toString(interpolant));
    }

    private void setMoveInterpolant(float interpolant) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetMoveInterpolant", Float.toString(interpolant));
    }

    /*
     * Rotate type
     * 0: Direct Quaternion
     * 1: Smooth Quaternion
     * 2: Direct Euler
     * 3: Smooth Euler
     */
    private void setRotateType(int type) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetRotateType", Integer.toString(type));
    }

    /*
     * Move type
     * 0: Direct
     * 1: Smooth
     */
    private void setMoveType(int type) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetMoveType", Integer.toString(type));
    }

    private void setMoveScale(float scale) {
        UnityPlayer.UnitySendMessage(PLAYER_NAME, "SetMoveScale", Float.toString(scale));
    }

    private void setIsFollowPlayer(boolean b) {
        UnityPlayer.UnitySendMessage(MAIN_CAMERA, "SetIsFollowPlayer", b ? "True" : "False");
    }

    private void setCamMoveSmoothTime(float time) {
        UnityPlayer.UnitySendMessage(MAIN_CAMERA, "SetMoveSmoothTime", Float.toString(time));
    }

    private void setCamMoveInterpolant(float interpolant) {
        UnityPlayer.UnitySendMessage(MAIN_CAMERA, "SetMoveInterpolant", Float.toString(interpolant));
    }

    private void initUnityPlayer() {

        // Set interpolant
        setRotateInterpolant(0.8f);
        setMoveInterpolant(0.1f);

        // Set type
        setRotateType(2);
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
            {0, 0, 0, 1f, 0, 0, 0},
            {0.03f, 0, 0, 1f, 12f, 12f, 12f},
            {0.06f, 0, 0, 1f, 24f, 24f, 24f},
            {0.09f, 0, 0, 1f, 36f, 36f, 36f},
            {0.12f, 0, 0, 1f, 48f, 48f, 48f},
            {0.15f, 0, 0, 1f, 60f, 60f, 60f},
            {0.18f, 0, 0, 1f, 72f, 72f, 72f},
            {0.21f, 0, 0, 1f, 84f, 84f, 84f},
            {0.24f, 0, 0, 1f, 96f, 96f, 96f},
            {0.27f, 0, 0, 1f, 108f, 108f, 108f},
            {0.30f, 0, 0, 1f, 120f, 120f, 120f},
            {0.33f, 0, 0, 1f, 132f, 132f, 132f},
            {0.36f, 0, 0, 1f, 144f, 144f, 144f},
            {0.39f, 0, 0, 1f, 156f, 156f, 156f},
            {0.42f, 0, 0, 1f, 168f, 168f, 168f},
            {0.45f, 0, 0, 1f, 180f, 180f, 180f},
            {0.48f, 0, 0, 1f, 192f, 192f, 192f},
            {0.51f, 0, 0, 1f, 204f, 204f, 204f},
            {0.54f, 0, 0, 1f, 216f, 216f, 216f},
            {0.57f, 0, 0, 1f, 228f, 228f, 228f},
            {0.60f, 0, 0, 1f, 240f, 240f, 240f},
            {0.63f, 0, 0, 1f, 252f, 252f, 252f},
            {0.66f, 0, 0, 1f, 264f, 264f, 264f},
            {0.69f, 0, 0, 1f, 276f, 276f, 276f},
            {0.72f, 0, 0, 1f, 288f, 288f, 288f},
            {0.75f, 0, 0, 1f, 300f, 300f, 300f},
            {0.78f, 0, 0, 1f, 312f, 312f, 312f},
            {0.81f, 0, 0, 1f, 324f, 324f, 324f},
            {0.84f, 0, 0, 1f, 336f, 336f, 336f},
            {0.87f, 0, 0, 1f, 348f, 348f, 348f},
            {0.90f, 0, 0, 1f, 360f, 360f, 360f},
            {0.87f, 0, 0, 1f, 348f, 348f, 348f},
            {0.84f, 0, 0, 1f, 336f, 336f, 336f},
            {0.81f, 0, 0, 1f, 324f, 324f, 324f},
            {0.78f, 0, 0, 1f, 312f, 312f, 312f},
            {0.75f, 0, 0, 1f, 300f, 300f, 300f},
            {0.72f, 0, 0, 1f, 288f, 288f, 288f},
            {0.69f, 0, 0, 1f, 276f, 276f, 276f},
            {0.66f, 0, 0, 1f, 264f, 264f, 264f},
            {0.63f, 0, 0, 1f, 252f, 252f, 252f},
            {0.60f, 0, 0, 1f, 240f, 240f, 240f},
            {0.57f, 0, 0, 1f, 228f, 228f, 228f},
            {0.54f, 0, 0, 1f, 216f, 216f, 216f},
            {0.51f, 0, 0, 1f, 204f, 204f, 204f},
            {0.48f, 0, 0, 1f, 192f, 192f, 192f},
            {0.45f, 0, 0, 1f, 180f, 180f, 180f},
            {0.42f, 0, 0, 1f, 168f, 168f, 168f},
            {0.39f, 0, 0, 1f, 156f, 156f, 156f},
            {0.36f, 0, 0, 1f, 144f, 144f, 144f},
            {0.33f, 0, 0, 1f, 132f, 132f, 132f},
            {0.30f, 0, 0, 1f, 120f, 120f, 120f},
            {0.27f, 0, 0, 1f, 108f, 108f, 108f},
            {0.24f, 0, 0, 1f, 96f, 96f, 96f},
            {0.21f, 0, 0, 1f, 84f, 84f, 84f},
            {0.18f, 0, 0, 1f, 72f, 72f, 72f},
            {0.15f, 0, 0, 1f, 60f, 60f, 60f},
            {0.12f, 0, 0, 1f, 48f, 48f, 48f},
            {0.9f, 0, 0, 1f, 36f, 36f, 36f},
            {0.6f, 0, 0, 1f, 24f, 24f, 24f},
            {0.3f, 0, 0, 1f, 12f, 12f, 12f},
            {0, 0, 0, 1f, 0, 0, 0}
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
                        new BleEvents.NotifyAhrsRotateEulerEvent(mRotations[i][4], mRotations[i][5], mRotations[i][6]));
                KLog.i("qx: " + mRotations[i][0] + ", qy: " + mRotations[i][1] + ", qz: " + mRotations[i][2] + ", qw: " + mRotations[i][3] +
                        ", ex: " + mRotations[i][4] + ", ey: " + mRotations[i][5] + ", ez: " + mRotations[i][6]);

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
