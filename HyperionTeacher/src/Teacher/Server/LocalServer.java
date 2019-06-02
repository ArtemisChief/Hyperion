package Teacher.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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

    // 开启服务器
    public static void launch() {
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

    private static String ProcessContent(String content){
        status.get();
        return "0";
    }

}
