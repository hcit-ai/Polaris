package com.polaris.http.listener;

public interface ServerListener {

	default void starting() {
		return;
	}
	default void started(){
		return;
	}
	default void failure(){
		return;
	}
	default void stopping() {
		return;
	}
	default void stopped() {
		return;
	}
	
}
