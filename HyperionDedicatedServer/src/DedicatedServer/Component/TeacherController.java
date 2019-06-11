package DedicatedServer.Component;

import DedicatedServer.Entity.Class;
import DedicatedServer.Entity.Student;

import java.io.*;
import java.net.Socket;

class TeacherController {

    private static Socket ProfTCPSocket;


    public TeacherController(Socket profTCPSocket) {
        super();
        ProfTCPSocket = profTCPSocket;
    }


    public void run() {
        processMessage(ProfTCPSocket);
    }

    /**
     * 向教师端返回第一个数字的结果
     * 0 - 收到新的签到信息（后接具体信息）
     * . - 确认签到停止
     *
     * 来自教师端信息的第一个数字含义
     * 0 - 开启签到（后接开启签到班级信息）
     * 1 - 关闭签到
     */
    public static void processMessage(Socket tcpSocket) {
        ProfTCPSocket = tcpSocket;
        Thread thread = new Thread(() -> {
            try {
                PrintStream printStream = (PrintStream) ProfTCPSocket.getOutputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ProfTCPSocket.getInputStream()));
                String line = bufferedReader.readLine();

                String content = "";
                while(line!=null&&line!="") {
                    content += line + "\n";

                    printStream.println("Hello prof, your message received.");
                    printStream.flush();
                    System.out.println("From prof:"+line);
                }
                //TODO:处理结果已返回到result，需要显示在某处的话
                String result = ProcessContent(content);

                printStream.close();
                bufferedReader.close();
                ProfTCPSocket.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        });
        thread.start();

    }//end processMessage

    private static String ProcessContent(String content){

        if(content.substring(0,1).equals("0")){
            //服务器开启该班级签到
            content = content.substring(content.indexOf("\n") + 1);
            Class.CurrentClassId = content.substring(0, content.indexOf("\n"));
            DedicatedServer.currentMAC = content.substring(content.indexOf("\n") + 1);

            return "CheckIn Started! CurrentClassId = " + Class.CurrentClassId + " ; CurrentMac = " + DedicatedServer.currentMAC + "\n";
        }

        if(content.substring(0,1).equals("1")){
            //关闭签到
            Class.CurrentClassId = null;
            DedicatedServer.currentMAC = null;

            //向教师端回复确认结束信息
            sendToTeacher(".");


            return "CheckIn Ended!\n";
        }

        return "Invalid Request! Request = \" " + content.replace("\n","\\n") + " \" ";

    }//end processContent

    //使用当前的Socket向教师发送信息
    public static void sendToTeacher(String message){
        try{
            OutputStream outputStream = ProfTCPSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.write(message);
            printWriter.flush();

        }catch (Exception ex){
            //Todo:处理异常
        }

    }//end sendToTeacher

}
