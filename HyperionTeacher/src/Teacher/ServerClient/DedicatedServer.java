package Teacher.ServerClient;

import javafx.scene.control.TableView;

import java.io.*;
import java.net.*;


public class DedicatedServer {

	private String serverIP = "";
	private static final int serverPort = 20076;
	private Socket socket = null;

	private static TableView tableView;

	// 得到Controller实例
	public static void setTableView(TableView tv) {
		tableView = tv;
	}

	/**
	 * 所有服务器返回第一个数字的结果
	 * 0 - 连接成功
	 * 1 - 签到已开启
	 * 2 - 收到新的签到信息（后接具体信息）
	 * 3 - 签到结束
	 *
	 * 所有向服务器发送的信息，第一个数字含义
	 * 0 - 尝试连接
	 * 1 - 开启签到（后接开启签到班级信息）
	 * 2 - 关闭签到
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
	public void startCheckIn(){
		if(socket == null)
			return;

		Thread thread = new Thread(() -> {
			try{
				OutputStream outputStream = socket.getOutputStream();
				PrintWriter printWriter = new PrintWriter(outputStream);
				//Todo：获取班级、MAC信息
				printWriter.write("1\n" + "班级");
				printWriter.flush();

				while(true){
					// 接收
					InputStream inputStream = socket.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					String receivedString = bufferedReader.readLine();
					if(receivedString.substring(0,1).equals("2")){
						//Todo:处理信息，将新的签到信息保存、展示
					}
				}
			}catch (Exception ex){

			}
		});
		thread.start();
}

	// 关闭服务器
	public void Close() {
		try{
			socket.close();
		}catch (Exception ex){
			//Todo:处理异常
		}

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
