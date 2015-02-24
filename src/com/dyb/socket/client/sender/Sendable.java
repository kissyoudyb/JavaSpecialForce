package com.dyb.socket.client.sender;

import java.io.IOException;

import com.dyb.socket.SocketWrapper;

public interface Sendable {

	public byte getSendType();
	
	public void sendContent(SocketWrapper socketWrapper) throws IOException;
}
