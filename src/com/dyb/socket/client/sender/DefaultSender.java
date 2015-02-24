package com.dyb.socket.client.sender;

import java.io.IOException;

import com.dyb.socket.SocketWrapper;
import com.dyb.socket.client.exceptions.ExitException;

import static com.dyb.socket.Commons.HELP_STR;
import static com.dyb.socket.Commons.HELP_SHOW;
import static com.dyb.socket.Commons.println;
import static com.dyb.socket.Commons.EXIT_STR;
import static com.dyb.socket.Commons.ERROR_MESSAGE_FORMAT;

public class DefaultSender implements Sendable{
	
	public DefaultSender(String[] tokens){
		String firstToken = tokens[0];
		if(HELP_STR.equalsIgnoreCase(firstToken)){//帮助
			println(HELP_SHOW);
		} else if(EXIT_STR.equalsIgnoreCase(firstToken)){//退出
			//System.exit(0);该方法直接关闭进程，也可以使用自定义的ExitException外部会做socket回收处理
			throw new ExitException();
		} else {
			throw new RuntimeException(ERROR_MESSAGE_FORMAT);
		}
	}

	@Override
	public byte getSendType() {
		return 0;
	}

	@Override
	public void sendContent(SocketWrapper socketWrapper) throws IOException {
		/*不发送任何信息*/
	}

	
}
