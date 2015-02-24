package com.dyb.socket.server;

import static com.dyb.socket.Commons.DEFAULT_MESSAGE_CHARSET;
import static com.dyb.socket.Commons.SERVER_SAVE_BASE_PATH;
import static com.dyb.socket.Commons.logInfo;
import static com.dyb.socket.Commons.DEFAULT_BUFFER_LENGTH;
import static com.dyb.socket.Commons.print;
import static com.dyb.socket.Commons.closeStream;
import static com.dyb.socket.Commons.SEND_MESSAGE;
import static com.dyb.socket.Commons.SEND_FILE;
import static com.dyb.socket.Commons.SEND_B_FILE;
import static com.dyb.socket.Commons.GET_FILE;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.dyb.socket.Commons;
import com.dyb.socket.SocketWrapper;
import com.dyb.socket.client.exceptions.DownloadNotExistsFileException;
import com.dyb.socket.client.exceptions.SaveExistsFileException;

public class Worker extends Thread {

	private SocketWrapper socketWrapper;
	
	private String name;
	
	public Worker(SocketWrapper socketWrapper, String name){
		this.socketWrapper = socketWrapper;
		this.name = name;
		logInfo("我是线程: " + name + " , 开始启动接收客户端传来的数据......");
		this.start(); //启动
	}
	
	@Override
	public void run() {
		while(true){
			try{
				if(this.isInterrupted()) break;
				byte type = socketWrapper.readByte();
				switch (type) {
					case SEND_MESSAGE:
						processMessage();
						break;
					case SEND_FILE:
						processFile();
						break;
					case SEND_B_FILE:
						uploadFileContent(null);
						break;
					case GET_FILE:
						downloadFile();
						break;
					default:
						break;
					}
				} catch(SaveExistsFileException e){
					logInfo(e.getMessage());
				} catch(DownloadNotExistsFileException e){
					logInfo(e.getMessage());
				} catch(EOFException e){
					logInfo("客户端关闭socket, 线程：" + name + " 结束执行。");
					break; //对方socket已经断开
				} catch(Exception e){
					e.printStackTrace();
					logInfo("线程:" + name + "结束执行。");
					break;
				}
		}
		this.socketWrapper.close();
	}
	
	public void interrupt(){
		if(this.isAlive()){
			super.interrupt();
		}
	}
	
	private void processMessage() throws IOException{
		int length = socketWrapper.readInt();
		byte[] message = new byte[length];
		socketWrapper.read(message);
		logInfo("线程：" + name + " 接收到来自客户端传来message信息：" + new String(message, DEFAULT_MESSAGE_CHARSET));
	}
	
	private void processFile() throws IOException{
		//获取字符集
		String charset = Commons.getCharsetNameByCode(socketWrapper.readByte());
		logInfo("线程：" + name + "接收来自客户端发送文件,字符集为：" + charset);
		uploadFileContent(charset);
	}
	
	private void uploadFileContent(String charset) throws IOException, UnsupportedEncodingException, FileNotFoundException {
		FileOutputStream out = null;
		try{
			//获取文件名长度以及文件名
			short length = socketWrapper.readShort();
			byte[] bytes = new byte[length];
			socketWrapper.readFull(bytes);
			String fileName = new String(bytes, DEFAULT_MESSAGE_CHARSET);
			logInfo("线程：" + name + "接收来自客户端发送文件，来源文件名为：" + fileName);
			
			String path = SERVER_SAVE_BASE_PATH + fileName;
			File file = new File(path);
			if(file.exists()){
				throw new SaveExistsFileException(path);
			}
			socketWrapper.write(1); //表示文件可以创建
			out = new FileOutputStream(file);
			
			//获取文件内容，输出前面部分内容
			long fileLength = socketWrapper.readLong();
			logInfo("线程：" + name + "接收来自客户端发送文件，文件长度为：" + fileLength);
			bytes = new byte[DEFAULT_BUFFER_LENGTH];
			int allLength = 0, i = 0;
			while(allLength < fileLength){
				int len = socketWrapper.read(bytes);
				allLength += len;
				out.write(bytes, 0, len);
				if(charset != null){
					print(new String(bytes, charset));
				}
				if(i++ % 1024 == 0){
					print(".");
				}
			}
			logInfo("\n文件接收完毕并保存，向客户端发送确认信息， 实际接收内容长度为：" + fileLength);
			socketWrapper.write(1);
		} catch(SaveExistsFileException e){
			socketWrapper.write(-1);
			throw e;
		} catch(RuntimeException e){
			logInfo("\n文件接收失败，向客户端发送错误消息。");
			socketWrapper.write(-2);
			throw e;
		} finally{
			closeStream(out);
		}
	}
	
	private void downloadFile() throws IOException, UnsupportedEncodingException, FileNotFoundException {
		short fileNameLength = socketWrapper.readShort();
		byte[] fileNameBytes = new byte[fileNameLength];
		socketWrapper.read(fileNameBytes);
		String fileName = new String(fileNameBytes, DEFAULT_MESSAGE_CHARSET);
		logInfo("线程：" + name + "接受客户端下载文件，下载文件名为：" + fileName);
		
		String absolutePath = SERVER_SAVE_BASE_PATH + fileName;
		File file = new File(absolutePath);
		if(file.exists()){
			socketWrapper.write(1);
		} else {
			socketWrapper.write(-1);
			throw new DownloadNotExistsFileException(absolutePath);
		}
		socketWrapper.write(file.length()); //文件长度
		socketWrapper.sendFile(absolutePath); //发送文件
	}
}
