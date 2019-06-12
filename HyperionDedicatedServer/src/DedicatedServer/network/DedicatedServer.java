package dedicatedserver.network;

import dedicatedserver.component.CheckInManager;
import dedicatedserver.entity.Class;
import dedicatedserver.entity.Student;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DedicatedServer {

    /**
     * 区分消息来源
     * 学生端 - 0
     * 教师端 - 1
     * <p>
     * 来自学生端的消息
     * 签到请求 - "学号 学生MAC地址 默认网关MAC地址"
     * <p>
     * 回复学生端的消息
     * 签到成功 - 0
     * 签到失败，学号与MAC地址不匹配 - 1
     * 重复签到 - 2
     * 本轮签到已关闭 - 3
     * 签到失败，不在指定位置签到 - 4
     * <p>
     * 来自教师端消息
     * 信息同步 - "0"
     * 开启签到 - "1 设置的MAC地址 班级ID 签到次数"
     * 关闭签到 - "2"
     * 断开连接 - "3"
     * <p>
     * 回复教师端的消息
     * 签到成功信息 - "学号 学生MAC地址"
     * 确认签到停止 - "."
     */

    // 单例
    private static DedicatedServer ourInstance = new DedicatedServer();

    // 得到单例
    public static DedicatedServer getInstance() {
        return ourInstance;
    }

    // 服务器端口号
    private static final int SERVER_PORT = 20076;

    // TCP连接服务器端Socket
    private ServerSocket serverSocket;

    // TCP连接老师端Socket
    private Socket teacherSocket;

    // 接收老师端消息
    private BufferedReader teacherBufferedReader;

    // 发送老师端消息
    private PrintWriter teacherPrintWriter;

    // 构造函数
    private DedicatedServer() {
        serverSocket = null;
        teacherSocket = null;
        teacherPrintWriter = null;
        teacherBufferedReader = null;
    }

    // 开启服务器
    public void launch() {
        readCheckInData();

        Thread thread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                Socket socket;
                System.out.println("Dedicated server launched, port = " + SERVER_PORT + ", waiting for client connection...");

                while (true) {
                    socket = serverSocket.accept();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String sender = bufferedReader.readLine();

                    if (sender.equals("0"))
                        processStudentConnection(socket,bufferedReader);
                    else if (sender.equals("1")) {
                        processTeacherConnection(socket,bufferedReader);
                    } else
                        socket.close();
                }
            } catch (IOException e) {
                writeCheckInData();
                System.out.println("Dedicated server closed");
            }
        });
        thread.start();
    }

    // 关闭服务器
    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 处理学生连接(短链接)
    private void processStudentConnection(Socket socket, BufferedReader bufferedReader) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Student connected from " + socket.getInetAddress().getHostAddress());

                // 接收消息
                String receivedString = bufferedReader.readLine();

                // 处理消息
                String returnString = processStudentContent(receivedString);

                // 发送消息
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println(returnString);
                printWriter.flush();

                if (returnString.equals("0")) {
                    String stuId = receivedString.substring(0, receivedString.indexOf(" "));
                    String stdMac = receivedString.substring(receivedString.indexOf(" ") + 1, receivedString.lastIndexOf(" "));
                    teacherPrintWriter.println(stuId + " " + stdMac);
                    teacherPrintWriter.flush();
                }

                // 关闭连接
                if (socket != null) {
                    socket.close();
                    System.out.println("Student disconnected from " + socket.getInetAddress().getHostAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // 处理学生内容
    private String processStudentContent(String str) {
        String stuId = str.substring(0, str.indexOf(" "));
        String stdMac = str.substring(str.indexOf(" ") + 1, str.lastIndexOf(" "));
        String routerMac = str.substring(str.lastIndexOf(" ") + 1);

        Class currentClass = CheckInManager.getInstance().getCurrentClass();

        if (currentClass == null)
            // 当前签到的班级为空，未开启签到
            return "3";
        else {
            if (!routerMac.equals(CheckInManager.getInstance().getCurrentMAC())) {
                // 目标路由器MAC匹配失败，怕是在宿舍签到
                return "4";
            } else {
                // 目标路由器MAC匹配成功，是在指定位置签到
                int count = currentClass.getCheckInCount();
                String countStr = Integer.toString(count);

                Student student = currentClass.getStudentsInClass().get(stuId);

                if (student == null) {
                    // 学生第一次参与这个班级签到，但是MAC地址已经被其他学号的用过了，企图代签
                    for (Student stu : currentClass.getStudentsInClass().values())
                        if (stdMac.equals(stu.getMac()))
                            return "1";

                    // 学生第一次参与这个班级签到，绑定学号与MAC地址，签到成功
                    student = new Student(stuId, stdMac);
                    for (int i = 1; i < count; i++)
                        student.getCheckList().add("\\");
                    student.getCheckList().add(countStr);
                    currentClass.getStudentsInClass().put(stuId, student);
                    return "0";
                } else {
                    // 该班级已经存在当前学生
                    if (student.getMac().equals(stdMac)) {
                        // 检测MAC地址与记录相同，即学生在自己电脑进行操作，不是代签
                        if (student.getCheckList().size() < count) {
                            // 该学生本次还未签到，签到成功
                            for (int i = student.getCheckList().size() + 1; i < count; i++)
                                student.getCheckList().add("\\");
                            student.getCheckList().add(countStr);
                            return "0";
                        } else if (student.getCheckList().get(count - 1).equals("\\")) {
                            // 该学生当前次漏签，补签成功
                            student.getCheckList().set(count - 1, countStr);
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
    }

    // 处理老师连接(长连接)
    private void processTeacherConnection(Socket socket,BufferedReader bufferedReader) {
        if (teacherSocket == null) {
            try {
                teacherSocket = socket;
                teacherBufferedReader = bufferedReader;
                teacherPrintWriter = new PrintWriter(teacherSocket.getOutputStream());

                teacherPrintWriter.println("0");
                teacherPrintWriter.flush();
                System.out.println("Teacher connected from " + teacherSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread thread = new Thread(() -> {
                try {
                    String receivedString;

                    while (!(receivedString = teacherBufferedReader.readLine()).equals("3")) {
                        String returnString = processTeacherContent(receivedString);

                        if (returnString != null) {
                            teacherPrintWriter.println(returnString);
                            teacherPrintWriter.flush();
                        }
                    }

                    if (teacherSocket != null) {
                        teacherSocket.close();
                        teacherSocket = null;
                        System.out.println("Teacher disconnected from " + socket.getInetAddress().getHostAddress());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        } else {
            try {
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println("1");
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 处理老师内容
    private String processTeacherContent(String str) {
        int type;

        if (str.length() > 1)
            type = Integer.parseInt(str.substring(0, str.indexOf(" ")));
        else
            type = Integer.parseInt(str);

        CheckInManager checkInManager = CheckInManager.getInstance();

        switch (type) {
            case 0:
                // 信息同步
                System.out.println("Teacher sync data complete");

                return checkInManager.writeClassToString();
            case 1:
                // 开启签到
                System.out.println("Teacher start Check-in");

                String content = str.substring(str.indexOf(" ") + 1);

                String currentMAC = content.substring(0, content.indexOf(" "));
                String currentClassId = content.substring(content.indexOf(" ") + 1, content.lastIndexOf(" "));
                int checkInCount = Integer.parseInt(content.substring(content.lastIndexOf(" ") + 1));

                checkInManager.setCurrentMAC(currentMAC);
                checkInManager.setCurrentClass(currentClassId);

                if (checkInManager.getCurrentClass() == null)
                    checkInManager.getClasses().put(currentClassId, new Class(currentClassId, checkInCount));
                else
                    checkInManager.getCurrentClass().setCheckInCount(checkInCount);

                return null;
            case 2:
                // 关闭签到
                System.out.println("Teacher stop Check-in");

                checkInManager.setCurrentMAC(null);
                checkInManager.setCurrentClass(null);

                return ".";
            default:
                return null;

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
