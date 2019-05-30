package Hyperion;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    private int serverPort = 60076;

    private String localMac;
    private String routerMac;

    @FXML
    private Label localMacAddressLabel;
    @FXML
    private Label routerMacAddressLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private TextField IPTxtField;
    @FXML
    private TextField stdNoTxtField;
    @FXML
    private RadioButton localModeRadioBtn;
    @FXML
    private RadioButton dedicatedModeRadioBtn;
    @FXML
    private Button checkInBtn;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    protected void initialize() {
        localMac = getLocalMac();
        routerMac = getRouterMac();

        localMacAddressLabel.setText("Local Mac: " + localMac);
        routerMacAddressLabel.setText("Router Mac: " + routerMac);
        infoLabel.setText("");

        progressIndicator.setVisible(false);
    }

    @FXML
    // 显示说明信息
    protected void updateInfo(MouseEvent event) {
        if (event.getSource() == localMacAddressLabel)
            infoLabel.setText("This shows your local MAC address, which binds to your Student No.");
        else if (event.getSource() == routerMacAddressLabel)
            infoLabel.setText("This shows your router MAC address");
        else if (event.getSource() == IPTxtField)
            infoLabel.setText("Please type the dedicated server IP here");
        else if (event.getSource() == stdNoTxtField)
            infoLabel.setText("Please type your Student No. here");
        else if (event.getSource() == localModeRadioBtn)
            infoLabel.setText("This mode requests teacher and student use the same router");
        else if (event.getSource() == dedicatedModeRadioBtn)
            infoLabel.setText("This mode sends your check-in information to the dedicated server");
        else
            infoLabel.setText("Click here to complete check-in");
    }

    @FXML
    // 切换局域网/公网模式
    protected void switchMode() {
        if (dedicatedModeRadioBtn.isSelected())
            IPTxtField.setDisable(false);
        else
            IPTxtField.setDisable(true);
    }

    @FXML
    // 向服务器发送消息
    protected void sendMessage() {
        if (stdNoTxtField.getText().equals("")) {
            showSimpleAlert(Alert.AlertType.ERROR, "Error", "Please input your Student No.");
            return;
        }

        if (dedicatedModeRadioBtn.isSelected() && IPTxtField.getText().equals("")) {
            showSimpleAlert(Alert.AlertType.ERROR, "Error", "Please input the Dedicated Server IP");
            return;
        }

        checkInBtn.setDisable(true);
        localModeRadioBtn.setDisable(true);
        dedicatedModeRadioBtn.setDisable(true);
        IPTxtField.setDisable(true);
        stdNoTxtField.setDisable(true);

        Thread thread = new Thread(() -> {
            String receivedString = null;

            // TCP发送到公网IP地址的服务器
            if (dedicatedModeRadioBtn.isSelected()) {
                try {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(true);
                        progressIndicator.setProgress(-1);
                    });
                    receivedString = sendTCP(IPTxtField.getText(), serverPort, stdNoTxtField.getText() + "\n" + localMac + "\n" + routerMac);
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showSimpleAlert(Alert.AlertType.ERROR, "Error", "Cannot connect to the server, please check your network or the Dedicated Server IP");
                        progressIndicator.setVisible(false);
                        checkInBtn.setDisable(false);
                        localModeRadioBtn.setDisable(false);
                        dedicatedModeRadioBtn.setDisable(false);
                        IPTxtField.setDisable(false);
                        stdNoTxtField.setDisable(false);
                    });
                }
            }

            // UDP 广播寻找同一路由下的服务器
            if (localModeRadioBtn.isSelected()) {
                try {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(true);
                        progressIndicator.setProgress(-1);
                    });
                    receivedString = sendUDP(getbroadCastIP(), serverPort, stdNoTxtField.getText() + "\n" + localMac);
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showSimpleAlert(Alert.AlertType.ERROR, "Error", "Cannot connect to the server, please check your network");
                        progressIndicator.setVisible(false);
                        checkInBtn.setDisable(false);
                        localModeRadioBtn.setDisable(false);
                        dedicatedModeRadioBtn.setDisable(false);
                        IPTxtField.setDisable(false);
                        stdNoTxtField.setDisable(false);
                    });
                }
            }

            if (receivedString != null)
                if (receivedString.equals("0"))
                    Platform.runLater(() -> {
                        showSimpleAlert(Alert.AlertType.INFORMATION, "Success", "Check-in Successfully");
                        progressIndicator.setProgress(1);
                        progressIndicator.setPrefWidth(36);
                        progressIndicator.setPrefHeight(36);
                    });
                else if (receivedString.equals("1"))
                    Platform.runLater(() -> {
                        showSimpleAlert(Alert.AlertType.WARNING, "Failure", "Fail to Check-in, please check your Student No.");
                        progressIndicator.setVisible(false);
                        checkInBtn.setDisable(false);
                        localModeRadioBtn.setDisable(false);
                        dedicatedModeRadioBtn.setDisable(false);
                        IPTxtField.setDisable(false);
                        stdNoTxtField.setDisable(false);
                    });
        });
        thread.start();
    }

    // 利用TCP协议发送并接收信息
    private String sendTCP(String ip, int port, String message) throws IOException {
        // 发送
        Socket socket = new Socket(ip, port);
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.write(message);
        printWriter.flush();
        socket.shutdownOutput();

        // 接收
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String receivedString = bufferedReader.readLine();

        // 关闭连接
        bufferedReader.close();
        inputStream.close();
        printWriter.close();
        outputStream.close();
        socket.close();

        return receivedString;
    }

    // 利用UDP协议发送并接收信息
    private String sendUDP(String ip, int port, String message) throws IOException {
        // 发送
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(ip), port);
        socket.send(packet);

        // 接收
        byte[] receivedByte = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(receivedByte, receivedByte.length);
        socket.receive(receivedPacket);

        String receivedString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

        // 关闭连接
        socket.close();

        return receivedString;
    }

    // 得到广播地址
    private String getbroadCastIP() throws SocketException {
        String broadCastIP = null;
        Enumeration<?> netInterfaces = NetworkInterface.getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) netInterfaces.nextElement();
            if (!netInterface.isLoopback() && netInterface.isUp()) {
                List<InterfaceAddress> interfaceAddresses = netInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses)
                    //只有 IPv4 网络具有广播地址，因此对于 IPv6 网络将返回 null。
                    if (interfaceAddress.getBroadcast() != null)
                        broadCastIP = interfaceAddress.getBroadcast().getHostAddress();
            }
        }

        return broadCastIP;
    }

    // 得到本机MAC的地址
    private String getLocalMac() {
        try {
            //获取网卡，获取地址
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    stringBuilder.append("-");
                }
                //字节转换为整数
                int temp = mac[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    stringBuilder.append("0").append(str);
                } else {
                    stringBuilder.append(str);
                }
            }
            return stringBuilder.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 得到第一跳路由器（默认网关）的MAC地址
    private String getRouterMac() {
        try {
            Pattern pattern;
            Matcher matcher;
            String keyword;
            int index;

            // 得到默认网关的IP地址
            String gateway = callCmd("ipconfig");
            keyword = (gateway.contains("Default Gateway") ? "Default Gateway" : "默认网关") + "(.\\s)+:\\s";
            pattern = Pattern.compile(keyword);
            matcher = pattern.matcher(gateway);
            matcher.find();
            index = matcher.end();
            gateway = gateway.substring(index, gateway.indexOf("\n", index)).trim();

            // 得到默认网关的MAC地址
            String mac = callCmd("arp -a");
            keyword = gateway + "\\s+";
            pattern = Pattern.compile(keyword);
            matcher = pattern.matcher(mac);
            matcher.find();
            index = matcher.end();
            return mac.substring(index, index + 17).toUpperCase().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 调用CMD
    private String callCmd(String cmd) {
        StringBuilder result = new StringBuilder();
        String line;
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream(), "GBK");
            BufferedReader br = new BufferedReader(is);
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    // 弹出对话框
    private void showSimpleAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
