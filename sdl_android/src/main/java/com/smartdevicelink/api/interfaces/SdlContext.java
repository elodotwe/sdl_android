package com.smartdevicelink.api.interfaces;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;

import com.smartdevicelink.api.SdlActivity;
import com.smartdevicelink.api.diagnostics.DiagnosticManager;
import com.smartdevicelink.api.file.SdlFileManager;
import com.smartdevicelink.api.menu.SdlMenuManager;
import com.smartdevicelink.api.menu.SdlMenuTransaction;
import com.smartdevicelink.api.menu.SelectListener;
import com.smartdevicelink.api.permission.SdlPermissionManager;
import com.smartdevicelink.api.view.SdlAudioPassThruDialog;
import com.smartdevicelink.api.view.SdlButton;
import com.smartdevicelink.api.view.SdlChoiceSetManager;
import com.smartdevicelink.api.vehicledata.SdlVehicleDataManager;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.rpc.DisplayCapabilities;
import com.smartdevicelink.proxy.rpc.HMICapabilities;
import com.smartdevicelink.proxy.rpc.SdlMsgVersion;
import com.smartdevicelink.proxy.rpc.TTSChunk;
import com.smartdevicelink.proxy.rpc.VehicleType;
import com.smartdevicelink.proxy.rpc.enums.ButtonName;
import com.smartdevicelink.proxy.rpc.enums.DriverDistractionState;
import com.smartdevicelink.proxy.rpc.enums.Language;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;

public interface SdlContext {

    void startSdlActivity(Class<? extends SdlActivity> activity, Bundle bundle, int flags);

    void startSdlActivity(Class<? extends SdlActivity> activity, int flags);

    SdlContext getSdlApplicationContext();

    Context getAndroidApplicationContext();

    SdlFileManager getSdlFileManager();

    int registerButtonCallback(SdlButton.OnPressListener listener);

    void registerNamedButtonCallback(SdlButton.OnPressListener listener, ButtonName buttonName);

    void unregisterNamedButtonCallback(ButtonName buttonName);

    SdlMenuManager getSdlMenuManager();

    DiagnosticManager getDiagnosticManager();

    void unregisterButtonCallback(int id);

    void registerMenuCallback(int id, SelectListener listener);

    void unregisterMenuCallback(int id);

    void registerAudioPassThruListener(SdlAudioPassThruDialog.ReceiveDataListener listener);

    void unregisterAudioPassThruListener(SdlAudioPassThruDialog.ReceiveDataListener listener);

    boolean sendRpc(RPCRequest request);

    boolean sendTextToSpeak(String text);

    SdlChoiceSetManager getSdlChoiceSetManager();

    boolean sendTextToSpeak(TTSChunk chunk);

    SdlPermissionManager getSdlPermissionManager();

    Looper getSdlExecutionLooper();

    SdlMenuTransaction beginGlobalMenuTransaction();

    void registerRpcNotificationListener(FunctionID functionID, OnRPCNotificationListener rpcNotificationListener);

    void unregisterRpcNotificationListener(FunctionID functionID, OnRPCNotificationListener rpcNotificationListener);

    HMICapabilities getHmiCapabilities();

    DisplayCapabilities getDisplayCapabilities();

    VehicleType getVehicleType();

    SdlMsgVersion getSdlMessageVersion();

    String getModuleVersion();

    Language getConnectedLanguage();

    DriverDistractionState getCurrentDDState();

    SdlVehicleDataManager getVehicleDataManager();

}
