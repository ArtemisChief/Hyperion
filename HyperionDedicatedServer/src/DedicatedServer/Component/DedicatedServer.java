package DedicatedServer.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class DedicatedServer {

    private static final int STU_PORT = 20076;

    private static final int PROF_PORT = 20077;

    private static final int THREAD_POOL_SIZE = 10;

    private static ServerSocket stu_ServerSocket;

    private static ServerSocket prof_ServerSocket;

    public static String currentMAC = "";

    public static void launchServerPool() throws IOException{
        prof_ServerSocket = new ServerSocket(PROF_PORT);
        stu_ServerSocket = new ServerSocket(STU_PORT);

        Thread thread_prof = new Thread() {
            public void run() {
                try {
                    //等待教授客户端的连接
                    Socket prof = prof_ServerSocket.accept();
                    //Todo：返回已有签到信息
                    TeacherController.processMessage(prof);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        };
        thread_prof.start();
        //学生端采取线程池的处理方式：池内线程数：10
        for(int i = 0;i< THREAD_POOL_SIZE;i++) {
            Thread thread_stu = new Thread(){
                public void run(){
                    while(true){
                        try {
                            //等待学生客户端的连接
                            Socket student = stu_ServerSocket.accept();
                            StudentController.processMessage(student);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread_stu.start();
        }
    }

    // 关闭服务器
    public static void Close() throws IOException {
        stu_ServerSocket.close();
        prof_ServerSocket.close();
    }

}
