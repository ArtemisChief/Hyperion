package DedicatedServer.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

class StudentController {

    private static Socket TCPSocket;

    public StudentController(Socket tCPSocket) {
        super();
        TCPSocket = tCPSocket;
    }

    //	public Socket getTCPSocket() {
//		return TCPSocket;
//	}

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
