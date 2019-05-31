package DedicatedServer.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class DedicatedServer {

    private static final int serverPort = 20076;

    private static AtomicInteger count = new AtomicInteger(0);

    // 启动服务器
    public static void Launch() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            Socket socket;
            System.out.println("Dedicated server launched, port = " + serverPort + ", waiting for connecting...");

            while (true) {
                socket = serverSocket.accept();
                ProcessConnection(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 处理连接
    private static void ProcessConnection(Socket socket) {
        Thread thread = new Thread(() -> {
            String ip = socket.getInetAddress().getHostAddress();
            System.out.println("Connection Count: " + count.incrementAndGet() + ", Connected from " + ip);

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                int type = Integer.parseInt(bufferedReader.readLine());

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    content.append(line);

                switch (type) {
                    case 0:
                        StudentController.Process(content.toString());
                        break;
                    case 1:
                        TeacherController.Process(content.toString());
                        break;
                    default:
                        break;
                }

                socket.close();
                System.out.println("Connection Count: " + count.decrementAndGet() + ", Disconnected from " + ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

}
