package DedicatedServer.Component;

import DedicatedServer.Entity.Class;
import DedicatedServer.Entity.Student;

import java.io.*;
import java.net.Socket;

class StudentController {

    private static Socket StudTCPSocket;

    public StudentController(Socket tCPSocket) {
        super();
        StudTCPSocket = tCPSocket;
    }

    //	public Socket getTCPSocket() {
//		return TCPSocket;
//	}

    public void run() {
        processMessage(StudTCPSocket);
    }

    public static void processMessage(Socket tcpSocket) {
        StudTCPSocket = tcpSocket;
        try {
            PrintStream printStream = (PrintStream) StudTCPSocket.getOutputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(StudTCPSocket.getInputStream()));
            String line = bufferedReader.readLine();

            String content = "";
            while(line!=null&&line!="") {
                content += line + "\n";

                printStream.println("Hello student, your message received.");
                printStream.flush();
                System.out.println("From student:"+line);
            }
            //TODO:处理结果已返回到result，需要显示在某处的话
            String result = ProcessContent(content);
            //向学生回复签到状态信息
            sendToStudent(result);

            printStream.close();
            bufferedReader.close();
            StudTCPSocket.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }//end processMessage

    /**
     * 来自学生端的信息
     * 签到请求，格式：“学号\n本地MAC地址\n默认网关MAC地址”
     *
     * 回复学生端的信息
     * 0 - 签到成功
     * 1 - 签到失败，学号与MAC地址不匹配
     * 2 - 重复签到
     * 3 - 本轮签到已关闭
     */
    private static String ProcessContent(String content){

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
                //Todo：签到成功后关于教师端的处理

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
                        //Todo：签到成功后关于教师端的处理

                        return "0";
                    } else if (student.getCheckList().get(count - 1).equals("\\")) {
                        // 该学生当前次漏签，补签成功
                        student.getCheckList().set(count - 1, countStr);
                        //Todo：签到成功后关于教师端的处理

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

    }//end processContent

    //使用当前的Socket向学生发送信息
    public static void sendToStudent(String message){
        try{
            OutputStream outputStream = StudTCPSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.write(message);
            printWriter.flush();

        }catch (Exception ex){
            //Todo:处理异常
        }

    }//end sendToStudent

}
