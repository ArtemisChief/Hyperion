package teacher.network.server;

import teacher.component.CheckInManager;
import teacher.entity.Class;
import teacher.entity.Student;
import javafx.scene.control.TableView;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class LocalServer {

    /**
     * 来自学生端的信息
     * 签到请求，格式：“学号 学生MAC地址”

     * 回复学生端的信息
     * 0 - 签到成功
     * 1 - 签到失败，学号与MAC地址不匹配
     * 2 - 重复签到
     * 3 - 本轮签到已关闭
     */

    // 单例
    private static LocalServer ourInstance = new LocalServer();

    // 得到单例
    public static LocalServer getInstance() {
        return ourInstance;
    }

    // 服务器端口号
    private static final int SERVER_PORT = 20076;

    // UDP数据报Socket
    private DatagramSocket datagramSocket;

    // 表格
    private TableView tableView;

    private LocalServer() {
        datagramSocket = null;
        tableView = null;
    }

    // 得到Controller实例
    public void setTableView(TableView tableView) {
        this.tableView = tableView;
    }

    // 开启服务器
    public void launch() {
        readCheckInData();

        Thread thread = new Thread(() -> {
            try {
                datagramSocket = new DatagramSocket(SERVER_PORT);
                DatagramPacket receivedDatagramPacket;

                System.out.println("Local server launched, port = " + SERVER_PORT + ", waiting for receiving packet...");

                while (true) {
                    receivedDatagramPacket = new DatagramPacket(new byte[1024], 1024);
                    datagramSocket.receive(receivedDatagramPacket);
                    processDatagramPacket(receivedDatagramPacket);
                }
            } catch (SocketException e) {
                writeCheckInData();
                System.out.println("Local server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // 关闭服务器
    public void close() {
        datagramSocket.close();
    }

    // 处理数据包
    private void processDatagramPacket(DatagramPacket receivedDatagramPacket) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Received from " + receivedDatagramPacket.getAddress().getHostAddress());

                String receivedString = new String(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength());

                byte[] type = processContent(receivedString).getBytes();
                DatagramPacket sentDatagramPacket = new DatagramPacket(type, type.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                datagramSocket.send(sentDatagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // 处理签到内容
    private String processContent(String content) {
        String id = content.substring(0, content.indexOf(" "));
        String mac = content.substring(content.indexOf(" ") + 1);

        Class currentClass= CheckInManager.getInstance().getCurrentClass();

        if (currentClass == null)
            // 当前签到的班级为空，未开启签到
            return "3";
        else {
            int count = currentClass.getCheckInCount();
            String countStr = Integer.toString(count);

            Student student = currentClass.getStudentsInClass().get(id);

            if (student == null) {
                // 学生第一次参与这个班级签到，但是MAC地址已经被其他学号的用过了，企图代签
            	for (Student stu : currentClass.getStudentsInClass().values())
                    if (mac.equals(stu.getMac()))
                        return "1";

                // 学生第一次参与这个班级签到，绑定学号与MAC地址，签到成功
                student = new Student(id, mac);
                for (int i = 1; i < count; i++)
                    student.getCheckList().add("\\");
                student.getCheckList().add(countStr);
                currentClass.getStudentsInClass().put(id, student);
                tableView.getItems().add(student);
                tableView.refresh();
                return "0";
            } else {
                // 该班级已经存在当前学生
                if (student.getMac().equals(mac)) {
                    // 检测MAC地址与记录相同，即学生在自己电脑进行操作，不是代签
                    if (student.getCheckList().size() < count) {
                        // 该学生本次还未签到，签到成功
                        for (int i = student.getCheckList().size() + 1; i < count; i++)
                            student.getCheckList().add("\\");
                        student.getCheckList().add(countStr);
                        tableView.refresh();
                        return "0";
                    } else if (student.getCheckList().get(count - 1).equals("\\")) {
                        // 该学生当前次漏签，补签成功
                        student.getCheckList().set(count - 1, countStr);
                        tableView.refresh();
                        return "0";
                    } else
                        // 该学生本次已经签到，提示重复
                        return "2";
                } else {
                    // 检测MAC地址与记录不同，不在常用机进行操作，是代签
                    return "1";
                }
            }
        }
    }

    // 读取签到表文件
    private void readCheckInData() {
        try {
            File file = new File("HyperionData");

            if (!file.exists())
                return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            CheckInManager.getInstance().readClassesFromString(reader);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 写入签到表文件
    private void writeCheckInData() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("HyperionData")));

            writer.write(CheckInManager.getInstance().writeClassToString());

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
