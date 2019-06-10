package DedicatedServer.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
