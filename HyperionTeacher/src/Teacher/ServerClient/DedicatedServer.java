package Teacher.ServerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


public class DedicatedServer {
	private static final int STU_PORT = 20076;
	
	private static final int PROF_PORT = 20077;
	
	private static final int THREAD_POOL_SIZE = 10;
	
	private static ServerSocket stu_ServerSocket;
	
	private static ServerSocket prof_ServerSocket;
	
	public static void launchServerPool() throws IOException{
		prof_ServerSocket = new ServerSocket(PROF_PORT);
		stu_ServerSocket = new ServerSocket(STU_PORT);
		
		Thread thread_prof = new Thread() {
			public void run() {
				try {
					//等待教授客户端的连接
					Socket prof = prof_ServerSocket.accept();
					ProfTCPConnection.processMessage(prof);;
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
                            TCPConnection.processMessage(student);
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

class ProfTCPConnection implements Runnable{
	private static Socket ProfTCPSocket;
	
	
	public ProfTCPConnection(Socket profTCPSocket) {
		super();
		ProfTCPSocket = profTCPSocket;
	}


	public void run() {
		processMessage(ProfTCPSocket);
	}
	
	public static void processMessage(Socket tcpSocket) {
		ProfTCPSocket = tcpSocket;
		try {
			PrintStream printStream = (PrintStream) ProfTCPSocket.getOutputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ProfTCPSocket.getInputStream()));
			String line = bufferedReader.readLine();
			while(line!=null&&line!="") {
				//TODO:implement the processing of prof message.
				printStream.println("Hello prof, your message received.");
				printStream.flush();
				System.out.println("From prof:"+line);
			}
			printStream.close();
            bufferedReader.close();
            ProfTCPSocket.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
}

class TCPConnection implements Runnable{
	private static Socket TCPSocket;
	
	public TCPConnection(Socket tCPSocket) {
		super();
		TCPSocket = tCPSocket;
	}

//	public Socket getTCPSocket() {
//		return TCPSocket;
//	}
	@Override
	public void run() {
		processMessage(TCPSocket);
	}
	
	public static void processMessage(Socket tcpSocket) {
		TCPSocket = tcpSocket;
		try {
			PrintStream printStream = (PrintStream) TCPSocket.getOutputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
			String line = bufferedReader.readLine();
			while(line!=null&&line!="") {
				//TODO:implement the processing of student message.
				printStream.println("Hello student, your message received.");
				printStream.flush();
				System.out.println("From student:"+line);
			}
			printStream.close();
            bufferedReader.close();
            TCPSocket.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
}
