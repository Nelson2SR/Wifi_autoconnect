package com.appliedmesh.merchantapp.network;

public interface RequestCallback<T> {

	void onRequestSuccess(T value);
	
	void onRequestFailed(String errorMessage);
	
}
