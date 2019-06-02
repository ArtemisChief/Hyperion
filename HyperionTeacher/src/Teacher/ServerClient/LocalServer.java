package Teacher.ServerClient;

import Teacher.Entity.Class;
import Teacher.Entity.Student;
import javafx.scene.control.TableView;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalServer {

    /**
     * 服务器状态：
     * 0 - 未开启签到
     * 1 - 已开启签到
     */

    private static AtomicInteger status = new AtomicInteger(0);

    private static final int serverPort = 20076;

    private static DatagramSocket datagramSocket;

    private static TableView tableView;

    // 得到Controller实例
    public static void setTableView(TableView tv) {
        tableView = tv;
    }

    // 开启服务器
    public static void launch() {
        readCheckInData();

        Thread thread = new Thread(() -> {
            try {
                datagramSocket = new DatagramSocket(serverPort);
                DatagramPacket receivedDatagramPacket;

                System.out.println("Local server launched, port = " + serverPort + ", waiting for receive packet...");

                while (true) {
                    receivedDatagramPacket = new DatagramPacket(new byte[1024], 1024);
                    datagramSocket.receive(receivedDatagramPacket);
                    ProcessDatagramPacket(receivedDatagramPacket);
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
    public static void Close() {
        datagramSocket.close();
    }

    // 处理连接
    private static void ProcessDatagramPacket(DatagramPacket receivedDatagramPacket) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Received from " + receivedDatagramPacket.getAddress().getHostAddress());

                String receivedString = new String(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength());

                byte[] type = ProcessContent(receivedString).getBytes();
                DatagramPacket sentDatagramPacket = new DatagramPacket(type, type.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                datagramSocket.send(sentDatagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * 返回结果
     * 0 - 签到成功
     * 1 - 签到失败，学号与MAC地址不匹配
     * 2 - 重复签到
     * 3 - 本轮签到已关闭
     */

    private static String ProcessContent(String content) {
        status.get();

        String id = content.substring(0, content.indexOf("\n"));
        String mac = content.substring(content.indexOf("\n") + 1);

        if (Class.CurrentClassId == null)
            // 当前签到的班级为空，未开启签到
            return "3";
        else {
            int count = Class.GetCurrentClass().getCheckInCount();
            String countStr = Integer.toString(count);

            Student student = Class.GetCurrentClass().getStudentsInClass().get(id);

            if (student == null) {
                // 学生第一次参与这个班级签到，但是MAC地址已经被其他学号的用过了，企图代签
                for (Student stu : Class.GetCurrentClass().getStudentsInClass().values())
                    if (mac.equals(stu.getMac()))
                        return "1";

                // 学生第一次参与这个班级签到，绑定学号与MAC地址，签到成功
                student = new Student(id, mac);
                for (int i = 1; i < count; i++)
                    student.getCheckList().add("\\");
                student.getCheckList().add(countStr);
                Class.GetCurrentClass().getStudentsInClass().put(id, student);
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
    private static void readCheckInData() {
        try {
            File file = new File("HyperionData");

            if (!file.exists())
                return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String classId;
            int checkInCount;
            ConcurrentHashMap<String, Student> studentsInClass;

            String studentInfo;
            String studentId, studentMac;
            Vector<String> checkVector;

            while ((classId = reader.readLine()) != null) {
                checkInCount = Integer.parseInt(reader.readLine());
                studentsInClass = new ConcurrentHashMap<>();

                while ((studentInfo = reader.readLine()) != null) {
                    if (studentInfo.equals(""))
                        break;

                    String[] strings = studentInfo.split("\\s");

                    studentId = strings[0];
                    studentMac = strings[1];

                    checkVector = new Vector<>(Arrays.asList(strings).subList(2, strings.length));

                    Student student = new Student(studentId, studentMac, checkVector);
                    studentsInClass.put(studentId, student);
                }

                Class theClass = new Class(classId, checkInCount, studentsInClass);
                Class.Classes.put(classId, theClass);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 写入签到表文件
    private static void writeCheckInData() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("HyperionData")));

            StringBuilder content = new StringBuilder();

            for (Class theClass : Class.Classes.values()) {
                content.append(theClass.getId()).append("\n").append(theClass.getCheckInCount()).append("\n");

                for (Student student : theClass.getStudentsInClass().values()) {
                    content.append(student.getId()).append(" ").append(student.getMac());

                    Vector<String> checkList = student.getCheckList();

                    if (Class.CurrentClassId != null && theClass == Class.GetCurrentClass())
                        for (int i = checkList.size(); i < Class.GetCurrentClass().getCheckInCount(); i++)
                            checkList.add("\\");

                    for (String checkIn : checkList)
                        content.append(" ").append(checkIn);

                    content.append("\n");
                }
                content.append("\n");
            }

            writer.write(content.toString());

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
