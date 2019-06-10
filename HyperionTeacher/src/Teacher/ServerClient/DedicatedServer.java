package Teacher.ServerClient;

import javafx.scene.control.TableView;

import java.io.*;
import java.net.*;


public class DedicatedServer {

	private String serverIP = "";
	private static final int serverPort = 20076;

	private static DatagramSocket datagramSocket;
	private static TableView tableView;

	// 得到Controller实例
	public static void setTableView(TableView tv) {
		tableView = tv;
	}

	//点击连接按钮，尝试和服务器连接
	public String connectToServer(String serverIP){
		this.serverIP = serverIP;
		try{
			String receiveString = sendTCP(serverIP, serverPort, "TryConnecting");
			return receiveString;
		}catch(Exception ex){
			//Todo:异常显示
		}

		return "";
	}

//	//点击开启签到按钮，向服务器发送开启签到请求，并保持监听
//	// 开启服务器
//	public static void launch() {
//		//readCheckInData();
//
//		//Todo:向服务器发送开启签到信息
//
//
//		//Todo:开启监听来自服务器的签到信息
//		Thread thread = new Thread(() -> {
//			try {
//				datagramSocket = new DatagramSocket(serverPort);
//				DatagramPacket receivedDatagramPacket;
//
//				System.out.println("Local server launched, port = " + serverPort + ", waiting for receive packet...");
//
//				while (true) {
//					receivedDatagramPacket = new DatagramPacket(new byte[1024], 1024);
//					datagramSocket.receive(receivedDatagramPacket);
//					ProcessDatagramPacket(receivedDatagramPacket);
//				}
//			} catch (SocketException e) {
//				writeCheckInData();
//				System.out.println("Local server closed");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		});
//		thread.start();
//	}
//
//	// 关闭服务器
//	public static void Close() {
//		datagramSocket.close();
//	}


	// 利用TCP协议发送并接收信息
	private String sendTCP(String ip, int port, String message) throws IOException {
		// 发送
		Socket socket = new Socket(ip, port);
		OutputStream outputStream = socket.getOutputStream();
		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.write(message);
		printWriter.flush();
		socket.shutdownOutput();

		// 接收
		InputStream inputStream = socket.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String receivedString = bufferedReader.readLine();

		// 关闭连接
		bufferedReader.close();
		inputStream.close();
		printWriter.close();
		outputStream.close();
		socket.close();

		return receivedString;
	}
}
