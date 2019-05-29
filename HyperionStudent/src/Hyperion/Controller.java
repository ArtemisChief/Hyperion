package Hyperion;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

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
    private RadioButton lsmRadioBtn;
    @FXML
    private RadioButton dsmRadioBtn;

    @FXML
    protected void initialize() {
        localMacAddressLabel.setText("Local Mac: " + getLocalMac());
        routerMacAddressLabel.setText("Router Mac: " + getRouterMac());
        infoLabel.setText("");
    }

    @FXML
    // 显示说明信息
    protected void updateInfo(MouseEvent event){
        if(event.getSource()==localMacAddressLabel)
            infoLabel.setText("This shows your local MAC address");
        else if(event.getSource()==routerMacAddressLabel)
            infoLabel.setText("This shows your router MAC address");
        else if (event.getSource()==IPTxtField)
            infoLabel.setText("Please type the dedicated server IP here");
        else if (event.getSource()==stdNoTxtField)
            infoLabel.setText("Please type your Student No. here");
        else if (event.getSource()==lsmRadioBtn)
            infoLabel.setText("This mode requests teacher and student use the same router");
        else if (event.getSource()==dsmRadioBtn)
            infoLabel.setText("This mode sends your check-in information to the dedicated server");
        else
            infoLabel.setText("Click here to complete check-in");
    }

    @FXML
    // 切换局域网/公网模式
    protected void switchMode() {
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
