package Teacher.ServerClient;

import Teacher.Component.CheckInManager;
import Teacher.Entity.Class;
import Teacher.Entity.Student;
import javafx.scene.control.TableView;

import java.io.*;
import java.net.*;

public class DedicatedServer {

	/**
	 * 向服务器发送的信息
	 * 0 - 开启签到（后接开启签到班级信息）
	 * 1 - 关闭签到
	 * <p>
	 * 从服务器接收的信息
	 * stuId stuMac - 学生签到成功信息
	 * . - 确认关闭签到
	 */

	// 单例
	private static DedicatedServer ourInstance = new DedicatedServer();

	// 得到单例
	public static DedicatedServer getInstance() {
		return ourInstance;
	}

	// 服务器端口号
	private static final int serverPort = 20076;

	// TCP连接客户端Socket
	private Socket socket;

	// 是否连接
	private boolean isConnected;

	private PrintWriter printWriter;

	private BufferedReader bufferedReader;

	private TableView tableView;

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
	public void connectToServer(String serverIP) throws IOException {
		socket = new Socket(serverIP, serverPort);

		// Socket空闲不断开
		socket.setKeepAlive(true);

		// IO数据流Timeout时间为无限
		socket.setSoTimeout(0);

		printWriter = new PrintWriter(socket.getOutputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// 同步班级数据
		CheckInManager.getInstance().readClassesFromString(bufferedReader);

		isConnected = true;
	}

	// 发送开启签到请求
	public void startCheckIn(String mac, String classId, int count) {
		Thread thread = new Thread(() -> {
			try {
				printWriter.write("0\n" + mac + "\n" + classId + "\n" + count);
				printWriter.flush();

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
						for (int i = student.getCheckList().size() + 1; i < count; i++)
							student.getCheckList().add("\\");
						student.getCheckList().add(Integer.toString(count));
						tableView.refresh();
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
		printWriter.write("1");
		printWriter.flush();
	}

	// 关闭连接
	public void close() {
		try {
			socket.close();
			isConnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
