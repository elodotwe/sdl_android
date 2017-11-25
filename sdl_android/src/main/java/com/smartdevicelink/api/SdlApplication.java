package com.smartdevicelink.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.SparseArray;

import com.smartdevicelink.api.diagnostics.DiagnosticManager;
import com.smartdevicelink.api.file.SdlFileManager;
import com.smartdevicelink.api.lockscreen.LockScreenStatusListener;
import com.smartdevicelink.api.menu.SdlMenuManager;
import com.smartdevicelink.api.menu.SdlMenuTransaction;
import com.smartdevicelink.api.menu.SelectListener;
import com.smartdevicelink.api.permission.SdlPermissionManager;
import com.smartdevicelink.api.speak.SdlTextToSpeak;
import com.smartdevicelink.api.view.SdlAudioPassThruDialog;
import com.smartdevicelink.api.view.SdlButton;
import com.smartdevicelink.api.view.SdlChoiceSetManager;
import com.smartdevicelink.api.vehicledata.SdlVehicleDataManager;
import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCNotification;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistration;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.DisplayCapabilities;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetWayPointsResponse;
import com.smartdevicelink.proxy.rpc.HMICapabilities;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.OnWayPointChange;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SdlMsgVersion;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.TTSChunk;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.VehicleType;
import com.smartdevicelink.proxy.rpc.enums.DriverDistractionState;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.LockScreenStatus;
import com.smartdevicelink.proxy.rpc.enums.Language;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.TriggerSource;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SdlApplication extends SdlContextAbsImpl {

    private static final String TAG = SdlApplication.class.getSimpleName();

    private static final String THREAD_NAME_BASE = "SDL_THREAD_";

    public static final int BACK_BUTTON_ID = 0;

    private static final int CHANGE_REGISTRATION_DELAY = 3000;

    public enum Status {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        RECONNECTING
    }

    private HandlerThread mExecutionThread;
    private Handler mExecutionHandler;

    private int mAutoCoorId = 100;
    private int mAutoButtonId = BACK_BUTTON_ID + 1;

    private SdlApplicationConfig mApplicationConfig;
    private LockScreenStatusListener mLockScreenStatusListener;
    @VisibleForTesting
    SdlActivityManager mSdlActivityManager;
    private SdlPermissionManager mSdlPermissionManager;
    private SdlChoiceSetManager mSdlChoiceSetManager;
    private SdlFileManager mSdlFileManager;
    private SdlMenuManager mSdlMenuManager;
    private DiagnosticManager mDiagnosticManager;
    private SdlVehicleDataManager mSdlVehicleDataManager;
    private SdlProxyALM mSdlProxyALM;

    private final ArrayList<LifecycleListener> mLifecycleListeners = new ArrayList<>();
    private final SparseArray<CopyOnWriteArrayList<RPCNotificationListenerRecord>> mNotificationListeners = new SparseArray<>();

    private ConnectionStatusListener mApplicationStatusListener;
    private Status mConnectionStatus;
    private Language mConnectedLanguage;

    private boolean isFirstHmiReceived = false;
    private boolean isFirstHmiNotNoneReceived = false;

    private SparseArray<SelectListener> mMenuListenerRegistry = new SparseArray<>();
    private SparseArray<SdlButton.OnPressListener> mButtonListenerRegistry = new SparseArray<>();
    private SdlAudioPassThruDialog.ReceiveDataListener mAudioPassThruListener;

    private DriverDistractionState mDriverDistractionState = DriverDistractionState.DD_ON;

    void initialize(final SdlConnectionService service,
                    final SdlApplicationConfig config,
                    final ConnectionStatusListener listener,
                    final LockScreenStatusListener lockScreenActivityManager) {
        mApplicationConfig = config;
        initializeExecutionThread(service.getApplicationContext());
        mExecutionHandler.post(new Runnable() {
            @Override
            public void run() {
                mSdlProxyALM = mApplicationConfig.buildProxy(service, null, mIProxyListenerALM);
                if (mSdlProxyALM == null) {
                    listener.onStatusChange(mApplicationConfig.getAppId(), Status.DISCONNECTED);
                    return;
                }
                mApplicationStatusListener = listener;
                mSdlActivityManager = new SdlActivityManager();
                mLockScreenStatusListener = lockScreenActivityManager;
                mSdlMenuManager = new SdlMenuManager();

                mSdlPermissionManager = new SdlPermissionManager();
                mLifecycleListeners.add(mSdlActivityManager);
                mSdlFileManager = new SdlFileManager(SdlApplication.this, mApplicationConfig);
                mSdlVehicleDataManager = new SdlVehicleDataManager(SdlApplication.this);
                mLifecycleListeners.add(mSdlFileManager);
                mDiagnosticManager = new DiagnosticManager(SdlApplication.this);
                createItemManagers();
                if (mSdlProxyALM != null) {
                    mConnectionStatus = Status.CONNECTING;
                    listener.onStatusChange(mApplicationConfig.getAppId(), Status.CONNECTING);
                } else {
                    onCreate();
                }
            }
        });
    }

    //TODO: have it so that we are not recreating the managers
    private void createItemManagers() {
        mSdlMenuManager = new SdlMenuManager();
        mSdlChoiceSetManager = new SdlChoiceSetManager(SdlApplication.this);
    }

    // Methods to be overridden by developer.
    protected void onConnect() {
    }

    protected void onCreate() {
    }

    protected void onDisconnect() {
    }

    // Executed on main to set up execution thread
    final void initializeExecutionThread(Context androidContext) {
        if (!isInitialized()) {
            setAndroidContext(androidContext);
            setSdlApplicationContext(this);
            /* Handler thread is spawned under the default priority to ensure that it is lower
             * priority than the main thread. */
            mExecutionThread = new HandlerThread(
                    THREAD_NAME_BASE + mApplicationConfig.getAppName().replace(' ', '_'),
                    Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE);
            mExecutionThread.start();
            mExecutionHandler = new Handler(mExecutionThread.getLooper());
            setInitialized(true);
        } else {
            Log.w(TAG, "Attempting to initialize SdlContext that is already initialized. Class " +
                    this.getClass().getCanonicalName());
        }
    }

    public final String getName() {
        return mApplicationConfig.getAppName();
    }

    final public String getId() {
        return mApplicationConfig.getAppId();
    }

    final SdlActivityManager getSdlActivityManager() {
        return mSdlActivityManager;
    }

    final void closeConnection(boolean notifyStatusListener, boolean destroyProxy, boolean destroyThread) {
        if (mConnectionStatus != Status.DISCONNECTED) {
            for (LifecycleListener listener : mLifecycleListeners) {
                listener.onSdlDisconnect();
            }
            mConnectionStatus = Status.DISCONNECTED;
            if (notifyStatusListener) {
                mLockScreenStatusListener.onLockScreenStatus(getId(), LockScreenStatus.OFF);
                mApplicationStatusListener.onStatusChange(mApplicationConfig.getAppId(), Status.DISCONNECTED);
            }
            onDisconnect();
            if (destroyProxy) {
                try {
                    mSdlProxyALM.dispose();
                } catch (SdlException e) {
                    e.printStackTrace();
                }
                mSdlProxyALM = null;

            }
            if (destroyThread) {
                mExecutionHandler.removeCallbacksAndMessages(null);
                mExecutionHandler = null;
                mExecutionThread.quit();
                mExecutionThread = null;
            }
        }
    }

    @Override
    public final String toString() {
        return String.format("SdlApplication: %s-%s",
                mApplicationConfig.getAppName(), mApplicationConfig.getAppId());
    }

    /****************************
     * SdlContext interface methods
     ****************************/

    @Override
    public final void startSdlActivity(final Class<? extends SdlActivity> activity, final Bundle bundle, final int flags) {
        mExecutionHandler.post(new Runnable() {
            @Override
            public void run() {
                mSdlActivityManager.startSdlActivity(SdlApplication.this, activity, bundle, flags);
            }
        });
    }

    @Override
    public SdlPermissionManager getSdlPermissionManager() {
        return mSdlPermissionManager;
    }

    @Override
    public final void startSdlActivity(Class<? extends SdlActivity> activity, int flags) {
        startSdlActivity(activity, null, flags);
    }

    @Override
    public final SdlFileManager getSdlFileManager() {
        return mSdlFileManager;
    }

    @Override
    public final SdlMenuManager getSdlMenuManager() {
        return mSdlMenuManager;
    }

    @Override
    public final SdlChoiceSetManager getSdlChoiceSetManager() {
        return mSdlChoiceSetManager;
    }

    @Override
    public DiagnosticManager getDiagnosticManager() {
        return mDiagnosticManager;
    }

    @Override
    public final int registerButtonCallback(SdlButton.OnPressListener listener) {
        int buttonId = mAutoButtonId++;
        mButtonListenerRegistry.append(buttonId, listener);
        return buttonId;
    }

    @Override
    public final void unregisterButtonCallback(int id) {
        mButtonListenerRegistry.remove(id);
    }

    @Override
    public final void registerMenuCallback(int id, SelectListener listener) {
        mMenuListenerRegistry.append(id, listener);
    }

    @Override
    public final void registerRpcNotificationListener(FunctionID functionID, OnRPCNotificationListener rpcNotificationListener) {
        synchronized (mNotificationListeners) {
            final int id = functionID.getId();
            CopyOnWriteArrayList<RPCNotificationListenerRecord> listenerList = mNotificationListeners.get(id);
            if (listenerList == null) {
                listenerList = new CopyOnWriteArrayList<>();
                mNotificationListeners.append(id, listenerList);
                listenerList.add(new RPCNotificationListenerRecord(rpcNotificationListener));

                OnRPCNotificationListener dispatchingListener = new OnRPCNotificationListener() {

                    @Override
                    public void onNotified(RPCNotification notification) {
                        synchronized (mNotificationListeners) {
                            List<RPCNotificationListenerRecord> listenerSet = mNotificationListeners.get(id);
                            for (RPCNotificationListenerRecord record : listenerSet) {
                                if (record.isValid)
                                    record.notificationListener.onNotified(notification);
                            }
                        }
                    }
                };
                if (mSdlProxyALM != null) {
                    mSdlProxyALM.addOnRPCNotificationListener(functionID, dispatchingListener);
                }
            } else {
                // This will match any ListenerRecord previously created with the given listener
                if (!listenerList.contains(new RPCNotificationListenerRecord(rpcNotificationListener))) {
                    listenerList.add(new RPCNotificationListenerRecord(rpcNotificationListener));
                }
            }
        }
    }

    @Override
    public final void unregisterRpcNotificationListener(FunctionID functionID, OnRPCNotificationListener rpcNotificationListener) {
        synchronized (mNotificationListeners) {
            int id = functionID.getId();
            List<RPCNotificationListenerRecord> listenerRecordList = mNotificationListeners.get(id);
            if (listenerRecordList != null) {
                // This will match any record created previously with this listener.
                int listenerIndex = listenerRecordList.indexOf(new RPCNotificationListenerRecord(rpcNotificationListener));
                if (listenerIndex >= 0) listenerRecordList.remove(listenerIndex).isValid = false;
                if (listenerRecordList.isEmpty() && mSdlProxyALM != null) {
                    mSdlProxyALM.removeOnRPCNotificationListener(functionID);
                    mNotificationListeners.put(id, null);
                }
            }
        }
    }

    @Override
    public final HMICapabilities getHmiCapabilities() {
        if (mSdlProxyALM == null) return null;
        try {
            return mSdlProxyALM.getHmiCapabilities();
        } catch (SdlException e) {
            Log.e(TAG, "Unable to retrieve HMICapabilities");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public final DisplayCapabilities getDisplayCapabilities() {
        if (mSdlProxyALM == null) return null;
        try {
            return mSdlProxyALM.getDisplayCapabilities();
        } catch (SdlException e) {
            Log.e(TAG, "Unable to retrieve DisplayCapabilities");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public final VehicleType getVehicleType() {
        if (mSdlProxyALM == null) return null;
        try {
            return mSdlProxyALM.getVehicleType();
        } catch (SdlException e) {
            Log.e(TAG, "Unable to retrieve VehicleType");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public final SdlMsgVersion getSdlMessageVersion() {
        if (mSdlProxyALM == null) return null;
        try {
            return mSdlProxyALM.getSdlMsgVersion();
        } catch (SdlException e) {
            Log.e(TAG, "Unable to retrieve SdlMessageVersion");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getModuleVersion() {
        if (mSdlProxyALM == null) return null;
        try {
            return mSdlProxyALM.getSystemSoftwareVersion();
        } catch (SdlException e) {
            Log.e(TAG, "Unable to retrieve SystemSoftwareVersion");
            return null;
        }
    }

    @Override
    public Language getConnectedLanguage() {
        return mConnectedLanguage;
    }

    @Override
    public final SdlMenuTransaction beginGlobalMenuTransaction() {
        return new SdlMenuTransaction(this, null);
    }

    @Override
    public final void unregisterMenuCallback(int id) {
        mMenuListenerRegistry.remove(id);
    }

    @Override
    public final void registerAudioPassThruListener(SdlAudioPassThruDialog.ReceiveDataListener listener) {
        mAudioPassThruListener = listener;
    }

    @Override
    public final void unregisterAudioPassThruListener(SdlAudioPassThruDialog.ReceiveDataListener listener) {
        if (mAudioPassThruListener == listener) {
            mAudioPassThruListener = null;
        }
    }

    @Override
    public final boolean sendRpc(final RPCRequest request) {
        if (Thread.currentThread() != mExecutionThread) {
            Log.e(TAG, "RPC Sent from thread: " + +Thread.currentThread().getId() + " - " +
                    Thread.currentThread().getName());
            throw new RuntimeException("RPCs may only be sent from the SdlApplication's execution " +
                    "thread. Use SdlContext#getExecutionHandler() to obtain a reference to the " +
                    "execution handler");
        }
        if (mSdlProxyALM != null) {
            try {
                request.setCorrelationID(mAutoCoorId++);
                Log.v(TAG, request.serializeJSON().toString(3));
                final OnRPCResponseListener listener = request.getOnRPCResponseListener();
                OnRPCResponseListener newListener = new OnRPCResponseListener() {
                    @Override
                    public void onResponse(final int correlationId, final RPCResponse response) {
                        request.setOnRPCResponseListener(listener);

                        mExecutionHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String jsonString = response.serializeJSON().toString(3);
                                    switch (response.getResultCode()) {

                                        case SUCCESS:
                                            Log.v(TAG, jsonString);
                                            break;
                                        case ABORTED:
                                        case IGNORED:
                                        case UNSUPPORTED_REQUEST:
                                        case WARNINGS:
                                        case USER_DISALLOWED:
                                            Log.w(TAG, jsonString);
                                            break;
                                        case INVALID_DATA:
                                        case OUT_OF_MEMORY:
                                        case TOO_MANY_PENDING_REQUESTS:
                                        case INVALID_ID:
                                        case DUPLICATE_NAME:
                                        case TOO_MANY_APPLICATIONS:
                                        case APPLICATION_REGISTERED_ALREADY:
                                        case WRONG_LANGUAGE:
                                        case APPLICATION_NOT_REGISTERED:
                                        case IN_USE:
                                        case VEHICLE_DATA_NOT_ALLOWED:
                                        case VEHICLE_DATA_NOT_AVAILABLE:
                                        case REJECTED:
                                        case UNSUPPORTED_RESOURCE:
                                        case FILE_NOT_FOUND:
                                        case GENERIC_ERROR:
                                        case DISALLOWED:
                                        case TIMED_OUT:
                                        case CANCEL_ROUTE:
                                        case TRUNCATED_DATA:
                                        case RETRY:
                                        case SAVED:
                                        case INVALID_CERT:
                                        case EXPIRED_CERT:
                                        case RESUME_FAILED:
                                            Log.e(TAG, jsonString);
                                            break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (listener != null) listener.onResponse(correlationId, response);
                            }
                        });
                    }

                    @Override
                    public void onError(final int correlationId, final Result resultCode, final String info) {
                        request.setOnRPCResponseListener(listener);
                        mExecutionHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.w(TAG, "RPC Error for correlation ID " + correlationId + " Result: " +
                                        resultCode + " - " + info);
                                if (listener != null)
                                    listener.onError(correlationId, resultCode, info);
                            }
                        });
                    }
                };
                request.setOnRPCResponseListener(newListener);
                mSdlProxyALM.sendRPCRequest(request);
            } catch (SdlException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public final Handler getExecutionHandler() {
        return mExecutionHandler;
    }

    @Override
    public boolean sendTextToSpeak(String text) {
        SdlTextToSpeak tts = new SdlTextToSpeak.Builder()
                .addSpokenChunk(text)
                .build();
        return tts.send(this);
    }

    @Override
    public boolean sendTextToSpeak(TTSChunk chunk) {
        SdlTextToSpeak tts = new SdlTextToSpeak.Builder()
                .addSpokenChunk(chunk)
                .build();
        return tts.send(this);
    }

    @Override
    public final Looper getSdlExecutionLooper() {
        return mExecutionThread != null ? mExecutionThread.getLooper() : null;
    }

    @Override
    public DriverDistractionState getCurrentDDState() {
        return mDriverDistractionState;
    }

    @Override
    public SdlVehicleDataManager getVehicleDataManager() {
        return mSdlVehicleDataManager;
    }

    /***********************************
     * IProxyListenerALM interface methods
     * All notification and response handling
     * should be offloaded onto the handler thread.
     ***********************************/

    private IProxyListenerALM mIProxyListenerALM = new IProxyListenerALM() {

        @Override
        public final void onOnHMIStatus(final OnHMIStatus notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    HMILevel hmiLevel = notification.getHmiLevel();
                    mSdlPermissionManager.onHmi(hmiLevel);

                    if (notification == null || notification.getHmiLevel() == null) {
                        Log.w(TAG, "INVALID OnHMIStatus Notification Received!");
                        return;
                    }

                    Log.i(TAG, toString() + " Received HMILevel: " + hmiLevel.name());

                    if (!isFirstHmiReceived) {
                        isFirstHmiReceived = true;
                        try {
                            if (mApplicationConfig.languageIsSupported(mSdlProxyALM.getSdlLanguage())) {
                                mConnectedLanguage = mSdlProxyALM.getSdlLanguage();
                                Log.v(TAG, "Config indicates the app supports the lang the module requested");
                            } else {
                                mConnectedLanguage = mApplicationConfig.getDefaultLanguage();
                                Log.v(TAG, "Config indicates the app does not support the lang the module requested, going to default");

                            }
                            Log.v(TAG, "Connected Lang: " + mConnectedLanguage + " " + "Module Lang: " + mSdlProxyALM.getSdlLanguage());
                        } catch (SdlException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Language could not be grabbed from proxy object");
                            mConnectedLanguage = mApplicationConfig.getDefaultLanguage();
                        }
                        if (mConnectedLanguage != mApplicationConfig.getDefaultLanguage()) {
                            changeRegistrationTask().run();
                        }
                        mConnectionStatus = Status.CONNECTED;
                        onConnect();
                        mApplicationStatusListener.onStatusChange(mApplicationConfig.getAppId(), Status.CONNECTED);

                        for (LifecycleListener listener : mLifecycleListeners) {
                            listener.onSdlConnect();
                        }
                    }

                    if (!isFirstHmiNotNoneReceived && hmiLevel != HMILevel.HMI_NONE) {
                        isFirstHmiNotNoneReceived = true;
                        Log.i(TAG, toString() + " is launching entry point activity: " + mApplicationConfig.getMainSdlActivityClass().getCanonicalName());
                        // TODO: Add check for resume
                        onCreate();
                        mSdlActivityManager.onSdlAppLaunch(SdlApplication.this, mApplicationConfig.getMainSdlActivityClass());
                    }

                    switch (hmiLevel) {

                        case HMI_FULL:
                            for (LifecycleListener listener : mLifecycleListeners) {
                                listener.onForeground();
                            }
                            break;
                        case HMI_LIMITED:
                        case HMI_BACKGROUND:
                            for (LifecycleListener listener : mLifecycleListeners) {
                                listener.onBackground();
                            }
                            break;
                        case HMI_NONE:
                            if (isFirstHmiNotNoneReceived) {
                                isFirstHmiNotNoneReceived = false;
                                for (LifecycleListener listener : mLifecycleListeners) {
                                    listener.onExit();
                                }
                            }
                            break;
                    }

                }
            });

        }

        @Override
        public final void onProxyClosed(String info, Exception e, final SdlDisconnectedReason reason) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (reason != SdlDisconnectedReason.LANGUAGE_CHANGE) {
                        closeConnection(true, true, true);
                    } else {
                        closeConnection(false, false, false);
                        isFirstHmiReceived = false;
                        isFirstHmiNotNoneReceived = false;
                        createItemManagers();
                        mConnectionStatus = Status.RECONNECTING;
                        mApplicationStatusListener.onStatusChange(mApplicationConfig.getAppId(), Status.RECONNECTING);
                    }
                }
            });
        }

        @Override
        public final void onServiceEnded(OnServiceEnded serviceEnded) {

        }

        @Override
        public final void onServiceNACKed(OnServiceNACKed serviceNACKed) {

        }

        @Override
        public final void onOnStreamRPC(OnStreamRPC notification) {

        }

        @Override
        public final void onStreamRPCResponse(StreamRPCResponse response) {

        }

        @Override
        public final void onError(String info, Exception e) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeConnection(true, true, true);
                }
            });
        }

        @Override
        public final void onGenericResponse(GenericResponse response) {

        }

        @Override
        public final void onOnCommand(final OnCommand notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (notification != null && notification.getCmdID() != null) {
                        SelectListener listener = mMenuListenerRegistry.get(notification.getCmdID());
                        if (listener != null) {
                            TriggerSource triggerSource = notification.getTriggerSource();
                            listener.onSelect(triggerSource != null ? triggerSource : TriggerSource.TS_MENU);
                        }
                    }
                }
            });
        }

        @Override
        public final void onAddCommandResponse(AddCommandResponse response) {

        }

        @Override
        public final void onAddSubMenuResponse(AddSubMenuResponse response) {

        }

        @Override
        public final void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {

        }

        @Override
        public final void onAlertResponse(AlertResponse response) {

        }

        @Override
        public final void onDeleteCommandResponse(DeleteCommandResponse response) {

        }

        @Override
        public final void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {

        }

        @Override
        public final void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {

        }

        @Override
        public final void onPerformInteractionResponse(PerformInteractionResponse response) {

        }

        @Override
        public final void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse response) {

        }

        @Override
        public final void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {

        }

        @Override
        public final void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {

        }

        @Override
        public final void onShowResponse(ShowResponse response) {

        }

        @Override
        public final void onSpeakResponse(SpeakResponse response) {

        }

        @Override
        public final void onOnButtonEvent(OnButtonEvent notification) {

        }

        @Override
        public final void onOnButtonPress(final OnButtonPress notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "onOnButtonPress");
                    if (notification != null && notification.getCustomButtonName() != null) {
                        int buttonId = notification.getCustomButtonName();
                        if (buttonId == BACK_BUTTON_ID) {
                            mSdlActivityManager.back();
                        } else {
                            SdlButton.OnPressListener listener = mButtonListenerRegistry.get(buttonId);
                            if (listener != null) {
                                listener.onButtonPress();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public final void onSubscribeButtonResponse(SubscribeButtonResponse response) {

        }

        @Override
        public final void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {

        }

        @Override
        public final void onOnPermissionsChange(final OnPermissionsChange notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSdlPermissionManager.onPermissionChange(notification);
                }
            });
        }

        @Override
        public final void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {

        }

        @Override
        public final void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse response) {

        }

        @Override
        public final void onGetVehicleDataResponse(final GetVehicleDataResponse response) {

        }

        @Override
        public final void onOnVehicleData(final OnVehicleData notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSdlVehicleDataManager.OnVehicleData(notification);
                }
            });
        }

        @Override
        public final void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {

        }

        @Override
        public final void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {

        }

        @Override
        public final void onOnAudioPassThru(final OnAudioPassThru notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Sending bytes back to the listener");
                    if (mAudioPassThruListener != null)
                        mAudioPassThruListener.receiveData(notification.getAPTData());
                }
            });

        }

        @Override
        public final void onPutFileResponse(PutFileResponse response) {

        }

        @Override
        public final void onDeleteFileResponse(DeleteFileResponse response) {

        }

        @Override
        public final void onListFilesResponse(ListFilesResponse response) {

        }

        @Override
        public final void onSetAppIconResponse(SetAppIconResponse response) {

        }

        @Override
        public final void onScrollableMessageResponse(ScrollableMessageResponse response) {

        }

        @Override
        public final void onChangeRegistrationResponse(ChangeRegistrationResponse response) {

        }

        @Override
        public final void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {

        }

        @Override
        public final void onOnLanguageChange(OnLanguageChange notification) {

        }

        @Override
        public final void onOnHashChange(OnHashChange notification) {

        }

        @Override
        public final void onSliderResponse(SliderResponse response) {

        }

        @Override
        public final void onOnDriverDistraction(final OnDriverDistraction notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDriverDistractionState = notification.getState();
                }
            });
        }

        @Override
        public final void onOnTBTClientState(OnTBTClientState notification) {

        }

        @Override
        public final void onOnSystemRequest(OnSystemRequest notification) {

        }

        @Override
        public final void onSystemRequestResponse(SystemRequestResponse response) {

        }

        @Override
        public final void onOnKeyboardInput(OnKeyboardInput notification) {

        }

        @Override
        public final void onOnTouchEvent(OnTouchEvent notification) {

        }

        @Override
        public final void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {

        }

        @Override
        public final void onReadDIDResponse(ReadDIDResponse response) {

        }

        @Override
        public final void onGetDTCsResponse(GetDTCsResponse response) {

        }

        @Override
        public final void onOnLockScreenNotification(final OnLockScreenStatus notification) {
            mExecutionHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "OnLockScreenStatus received.");
                    if (notification != null && notification.getShowLockScreen() != null) {
                        Log.i(TAG, "LockScreenStatus: " + notification.getShowLockScreen().name());
                        mLockScreenStatusListener.onLockScreenStatus(getId(), notification.getShowLockScreen());
                    }
                }
            });
        }

        @Override
        public final void onDialNumberResponse(DialNumberResponse response) {

        }

        @Override
        public final void onSendLocationResponse(SendLocationResponse response) {

        }

        @Override
        public final void onShowConstantTbtResponse(ShowConstantTbtResponse response) {

        }

        @Override
        public final void onAlertManeuverResponse(AlertManeuverResponse response) {

        }

        @Override
        public final void onUpdateTurnListResponse(UpdateTurnListResponse response) {

        }

        @Override
        public void onServiceDataACK(int dataSize) {

        }

        @Override
        public void onGetWayPointsResponse(GetWayPointsResponse response) {

        }

        @Override
        public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse response) {

        }

        @Override
        public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse response) {

        }

        @Override
        public void onOnWayPointChange(OnWayPointChange notification) {

        }
    };

    interface ConnectionStatusListener {

        void onStatusChange(String appId, SdlApplication.Status status);

    }

    public interface LifecycleListener {

        void onSdlConnect();

        void onBackground();

        void onForeground();

        void onExit();

        void onSdlDisconnect();

    }

    private Runnable changeRegistrationTask() {
        return new Runnable() {
            boolean hasExecutedOnce = false;

            @Override
            public void run() {
                Language connectedLang = getConnectedLanguage();
                ChangeRegistration reRegister = new ChangeRegistration();
                reRegister.setLanguage(connectedLang);
                reRegister.setHmiDisplayLanguage(connectedLang);
                final Runnable reuseRunnable = this;
                reRegister.setOnRPCResponseListener(new OnRPCResponseListener() {
                    @Override
                    public void onResponse(int correlationId, final RPCResponse response) {
                    }

                    @Override
                    public void onError(int correlationId, Result resultCode, String info) {
                        super.onError(correlationId, resultCode, info);
                        if (resultCode != Result.SUCCESS && !hasExecutedOnce) {
                            hasExecutedOnce = true;
                            mExecutionHandler.postDelayed(reuseRunnable, CHANGE_REGISTRATION_DELAY);
                        }
                    }
                });
                sendRpc(reRegister);
            }
        };
    }

    private final class RPCNotificationListenerRecord {
        volatile boolean isValid = true;
        final OnRPCNotificationListener notificationListener;

        RPCNotificationListenerRecord(OnRPCNotificationListener listener) {
            notificationListener = listener;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RPCNotificationListenerRecord that = (RPCNotificationListenerRecord) o;

            return notificationListener.equals(that.notificationListener);

        }

        @Override
        public int hashCode() {
            return notificationListener.hashCode();
        }
    }

}