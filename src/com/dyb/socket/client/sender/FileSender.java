package com.dyb.socket.client.sender;

import java.io.IOException;

import com.dyb.socket.Commons;
import com.dyb.socket.SocketWrapper;

import static com.dyb.socket.Commons.CHARSET_START;
import static com.dyb.socket.Commons.SEND_FILE;

public class FileSender extends BFileSender{
	
	private byte charsetByte;
	
	protected int minLength = 3;

	public FileSender(String[] tokens) throws IOException {
		super(tokens);
		this.charsetByte = Commons.getCharsetByteByName(getCharset(tokens[2]));
	}
	
	private String getCharset(String token){
		token = token.toLowerCase();
		if(token.startsWith(CHARSET_START)){
			return token.substring(CHARSET_START.length());
		} else{
			throw new RuntimeException("字符集部分不符合规范。");
		}
	}
	
	protected void sendCharset(SocketWrapper socketWrapper) throws IOException{
		socketWrapper.write(charsetByte); //字符集
	}

	public byte getSendType(){
		return SEND_FILE;
	}
}
