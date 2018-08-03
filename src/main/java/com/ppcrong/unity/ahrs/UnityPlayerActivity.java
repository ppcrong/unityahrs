package com.ppcrong.unity.ahrs;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
}
