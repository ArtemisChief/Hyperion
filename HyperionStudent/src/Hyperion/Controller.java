package Hyperion;

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
    protected void initialize() {
        localMac = getLocalMac();
        routerMac = getRouterMac();

        localMacAddressLabel.setText("Local Mac: " + localMac);
        routerMacAddressLabel.setText("Router Mac: " + routerMac);
        infoLabel.setText("");
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
        StringBuilder message = new StringBuilder();
        String receivedString = null;
        Alert alert;

        if (stdNoTxtField.getText().equals("")) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please input your Student No.");
            alert.showAndWait();
            return;
        }

        if (dedicatedModeRadioBtn.isSelected()) {
            if (IPTxtField.getText().equals("")) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please input the Dedicated Server IP");
                alert.showAndWait();
                return;
            }

            // TCP发送到公网IP地址的服务器
            try {
                // 发送
                message.append(stdNoTxtField.getText()).append("\n").append(localMac).append("\n").append(routerMac);
                Socket socket = new Socket(IPTxtField.getText(), 60076);
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter printWriter = new PrintWriter(outputStream);
                printWriter.write(message.toString());
                printWriter.flush();
                socket.shutdownOutput();

                // 接收
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                receivedString = bufferedReader.readLine();

                // 关闭连接
                bufferedReader.close();
                inputStream.close();
                printWriter.close();
                outputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Cannot connect to the server, please check your network or the Dedicated Server IP");
                alert.showAndWait();
            }
        } else {
            // UDP 广播寻找同一路由下的服务器
            try {
                // 得到广播地址
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

                // 发送
                message.append(stdNoTxtField.getText()).append("\n").append(localMac);
                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(1000);
                DatagramPacket packet = new DatagramPacket(message.toString().getBytes(), message.length(), InetAddress.getByName(broadCastIP), 60076);
                socket.send(packet);

                // 接收
                byte[] receivedByte = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(receivedByte, receivedByte.length);
                socket.receive(receivedPacket);

                receivedString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

                // 关闭连接
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Cannot connect to the server, please check your network");
                alert.showAndWait();
            }
        }

        if (receivedString != null) {
            if (receivedString.equals("0")) {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Check-in Successfully");
                alert.showAndWait();
            } else if (receivedString.equals("1")) {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Failure");
                alert.setHeaderText(null);
                alert.setContentText("Fail to Check-in, please check your Student No.");
                alert.showAndWait();
            }
        }
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

}
