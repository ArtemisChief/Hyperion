package Teacher.ServerClient;

import javafx.scene.control.TableView;

import java.io.*;
import java.net.*;


public class DedicatedServer {

	private String serverIP = "";
	private static final int serverPort = 20076;
	private Socket socket;

	private static TableView tableView;

	// 得到Controller实例
	public static void setTableView(TableView tv) {
		tableView = tv;
	}

	/**
	 * 所有服务器返回第一个数字的结果
	 * 0 - 连接成功
	 * 1 - 签到已开启
	 * 2 - 收到新的签到信息
	 * 3 - 签到结束
	 * 4 - 连接即将断开
	 *
	 * 所有向服务器发送的信息，第一个数字含义
	 * 0 - 尝试连接
	 * 1 - 开启签到
	 * 2 - 关闭签到
	 * 3 - 断开连接
	 */

	//点击连接按钮，尝试和服务器连接
	public String connectToServer(String serverIP){
		if(serverIP == null)
			return "Please Input Server IP!";

		this.serverIP = serverIP;
		try{
			// 发送
			socket = new Socket(serverIP, serverPort);
			OutputStream outputStream = socket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream);
			printWriter.write("0\n");
			printWriter.flush();

			// 接收
			InputStream inputStream = socket.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String receivedString = bufferedReader.readLine();

			if(receivedString.substring(0,1).equals("0"))
				return "Connected!";

		}catch(Exception ex){
			//Todo:返回异常信息
		}

		return "Connection Failed!";
	}

	// 开启签到，保持监听
	public static void startCheckIn(){
		//Todo：
//		OutputStream outputStream = socket.getOutputStream();
//		PrintWriter printWriter = new PrintWriter(outputStream);
//		printWriter.write("0\n");
//		printWriter.flush();
	}

	// 关闭服务器
	public static void Close() {
		//Todo：
		//datagramSocket.close();
	}


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
