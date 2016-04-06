package com.smartdevicelink.api;

import android.support.annotation.CallSuper;

import com.smartdevicelink.api.interfaces.SdlContext;

public abstract class SdlActivity extends SdlContextAbsImpl {

    enum SdlActivityState{
        created,
        started,
        foreground,
        background,
        stopped,
        restarted,
        destroyed
    }

    SdlActivityState mActivityState;

    SdlActivity(SdlContext sdlApplicationContext){
        super(sdlApplicationContext);
    }

    @CallSuper
    public void onCreate(){

    }

    @CallSuper
    public void onRestart(){

    }

    @CallSuper
    public void onStart(){

    }

    @CallSuper
    public void onForeground(){

    }

    @CallSuper
    public void onBackground(){

    }

    @CallSuper
    public void onStop(){

    }

    @CallSuper
    public void onDestroy(){

    }

    final void performCreate(){

    }

    final  void performRestart(){

    }

    final void performStart(){

    }

    final void performForeground(){

    }

    final void performBackground(){

    }

    final void performStop(){

    }

    final void performDestroy(){

    }

    @Override
    public final void startSdlActivity(Class<? extends SdlActivity> activity, int flags) {
        getSdlApplicationContext().startSdlActivity(activity, flags);
    }
}
