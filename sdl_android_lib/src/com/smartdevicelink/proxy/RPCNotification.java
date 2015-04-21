package com.smartdevicelink.proxy;

import java.util.Hashtable;

public class RPCNotification extends RPCMessage {

	public RPCNotification(String functionName) {
		super(functionName, "notification");
	}

	public RPCNotification(Hashtable<String, Object> hash) {
		super(hash);
	}

	public RPCNotification(RPCMessage rpcMsg) {
		super(rpcMsg);
	}
}