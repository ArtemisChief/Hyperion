package Hyperion;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    @FXML
    private Label localMacAddressLabel;
    @FXML
    private Label routerMacAddressLabel;
    @FXML
    private TextField IPTxtField;
    @FXML
    private RadioButton lsmRadioBtn;
    @FXML
    private RadioButton dsmRadioBtn;

    @FXML
    public void initialize() {
        localMacAddressLabel.setText("Local Mac: " + getLocalMac());
        routerMacAddressLabel.setText("Router Mac: " + getRouterMac());
    }

    @FXML
    // 切换局域网/公网模式
    protected void switchMode(ActionEvent event) {
        if (dsmRadioBtn.isSelected())
            IPTxtField.setDisable(false);
        else
            IPTxtField.setDisable(true);
    }

    @FXML
    // 向服务器发送消息
    protected void sendMessage(){

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
            // 得到默认网关的IP地址
            String gateway = callCmd("ipconfig");
            int index = gateway.contains("Default Gateway") ? gateway.indexOf("Default Gateway") : gateway.indexOf("默认网关");
            gateway = gateway.substring(index);
            index = gateway.indexOf(":") + 2;
            gateway = gateway.substring(index, gateway.indexOf("\n")).trim();

            // 得到默认网关的MAC地址
            String mac = callCmd("arp -a");
            Pattern pattern = Pattern.compile(gateway + "\\s+");
            Matcher matcher = pattern.matcher(mac);
            matcher.find();
            index = matcher.end();
            return mac.substring(index, index + 17).toUpperCase();
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
