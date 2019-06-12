package teacher.network.client;

import teacher.component.CheckInManager;
import teacher.entity.Class;
import teacher.entity.Student;
import javafx.scene.control.TableView;

import java.io.*;
import java.net.*;

public class DedicatedServer {

	/**
	 * 教师端凭证：1（单独一行）
	 *
	 * 向服务器发送的信息
	 * 信息同步 - "0"
	 * 开启签到 - "1 设置的MAC地址 班级ID 签到次数"
	 * 关闭签到 - "2"
	 * 断开连接 - "3"
	 *
	 * 从服务器接收的信息
	 * 签到成功信息 - "学号 学生MAC地址"
	 * 确认签到停止 - "."
	 * 连接成功 - 0
	 * 连接失败 - 1
	 */

	// 单例
	private static DedicatedServer ourInstance = new DedicatedServer();

	// 得到单例
	public static DedicatedServer getInstance() {
		return ourInstance;
	}

	// 服务器端口号
	private static final int SERVER_PORT = 20076;

	// TCP连接客户端Socket
	private Socket socket;

	// 是否连接
	private boolean isConnected;

	// 发送
	private PrintWriter printWriter;

	// 接收
	private BufferedReader bufferedReader;

	// GUI表格
	private TableView tableView;

	// 构造函数
	private DedicatedServer() {
		socket = null;
		isConnected = false;
		printWriter = null;
		bufferedReader = null;
		tableView = null;
	}

	// 得到Controller实例
	public void setTableView(TableView tableView) {
		this.tableView = tableView;
	}

	// 得到连接状态
	public boolean getIsConnected(){
		return isConnected;
	}

	// 连接服务器
	public boolean connectToServer(String serverIP) throws IOException {
		System.out.println("Dedicated server connecting...");

		socket = new Socket(serverIP, SERVER_PORT);

		// Socket空闲不断开
		socket.setKeepAlive(true);

		// IO数据流Timeout时间为无限
		socket.setSoTimeout(0);

		printWriter = new PrintWriter(socket.getOutputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// 教师端凭证
		printWriter.println("1");
		printWriter.flush();

		// 判断连接成功与否
		if(!bufferedReader.readLine().equals("0")){
			socket.close();
			System.out.println("Dedicated server connect fail, there is already a teacher connecting");
			return false;
		}

		System.out.println("Dedicated server connected, port = " + SERVER_PORT + ", waiting for receiving message...");


		// 同步班级数据
		printWriter.println("0");
		printWriter.flush();
		CheckInManager.getInstance().readClassesFromString(bufferedReader);

		isConnected = true;
		System.out.println("Sync data complete");
		return true;
	}

	// 发送开启签到请求
	public void startCheckIn(String mac, String classId, int count) {
		Thread thread = new Thread(() -> {
			try {
				printWriter.println("1 " + mac + " " + classId + " " + count);
				printWriter.flush();
				System.out.println("Start Check-in request sent, start receiving check-in message...");

				String receivedString;
				Class currentClass = CheckInManager.getInstance().getCurrentClass();

				while (!(receivedString = bufferedReader.readLine()).equals(".")) {
					// 接收学号，处理签到信息
					String stuId = receivedString.substring(0, receivedString.indexOf(" "));
					String stuMac = receivedString.substring(receivedString.indexOf(" ") + 1);

					Student student = currentClass.getStudentsInClass().get(stuId);

					if (student == null) {
						// 接收到的学生是第一次签到成功的
						student = new Student(stuId, stuMac);
						for (int i = 1; i < count; i++)
							student.getCheckList().add("\\");
						student.getCheckList().add(Integer.toString(count));
						currentClass.getStudentsInClass().put(stuId, student);
						tableView.getItems().add(student);
						tableView.refresh();
					} else {
						// 接收到的学生是已经存在的
						if (student.getCheckList().size() < count) {
							// 该学生本次还未签到，签到成功
							for (int i = student.getCheckList().size() + 1; i < count; i++)
								student.getCheckList().add("\\");
							student.getCheckList().add(Integer.toString(count));
							tableView.refresh();
						} else if (student.getCheckList().get(count - 1).equals("\\")) {
							// 该学生当前次漏签，补签成功
							student.getCheckList().set(count - 1, Integer.toString(count));
							tableView.refresh();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

	// 发送结束签到请求
	public void stopCheckIn() {
		printWriter.println("2");
		printWriter.flush();
		System.out.println("Stop Check-in request sent, start receiving check-in message...");
	}

	// 关闭连接
	public void close() {
		try {
			printWriter.println("3");
			printWriter.flush();
			socket.close();
			isConnected = false;
			System.out.println("Dedicated server connection closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
