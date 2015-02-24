package com.dyb.socket.client.sender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.dyb.socket.SocketWrapper;
import static com.dyb.socket.Commons.DEFAULT_MESSAGE_CHARSET;
import static com.dyb.socket.Commons.SEND_MESSAGE;
import static com.dyb.socket.Commons.println;

public class MessageSender implements Sendable {
	
	private String message; //普通的message消息
	
	private byte[] messageBytes; //消息发送时使用
	
	private int length = 0;
	
	public MessageSender(String[] tokens) throws UnsupportedEncodingException {
		if(tokens.length >= 2){
			message = tokens[1];
			this.messageBytes = message.getBytes(DEFAULT_MESSAGE_CHARSET);
			this.length = messageBytes.length;
		} else {
			throw new RuntimeException("请在sendMsg后面添加内容。");
		}
	}

	@Override
	public byte getSendType() {
		return SEND_MESSAGE;
	}

	@Override
	public void sendContent(SocketWrapper socketWrapper) throws IOException {
		println("我此时向服务器端发送消息：" + message);
		socketWrapper.write(length);
		socketWrapper.write(messageBytes);
		println("发送消息完毕。");
		
	}

}
