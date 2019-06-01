package Teacher;

import LocalServer.Component.LocalServer;
import LocalServer.Entity.Class;
import LocalServer.Entity.Student;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class TeacherController {

    @FXML
    private RadioButton localModeRadioBtn;
    @FXML
    private RadioButton dedicatedModeRadioBtn;
    @FXML
    private TextField IPTxtField;
    @FXML
    private ComboBox classComboBox;
    @FXML
    private Button connectBtn;
    @FXML
    private ToggleButton toggleCheckInBtn;
    @FXML
    private Slider timesSlider;
    @FXML
    private TableView<Student> tableView;

    private ConcurrentHashMap<String, Class> classes;

    @FXML
    // 初始化
    protected void initialize() {
        classes = new ConcurrentHashMap<>();

        LocalServer.launch();

        readCheckInData();

        fillComboBox();
    }

    @FXML
    // 切换局域网/公网模式
    protected void switchMode() {
        if (dedicatedModeRadioBtn.isSelected()) {
            IPTxtField.setDisable(false);
            connectBtn.setDisable(false);
            LocalServer.Close();
        } else {
            IPTxtField.setDisable(true);
            connectBtn.setDisable(true);
            LocalServer.launch();
        }
    }

    @FXML
    // 开启或关闭签到
    protected void StartCheckIn() {
        if (toggleCheckInBtn.isSelected()) {
            toggleCheckInBtn.setText("Stop Check-In");

        } else {
            toggleCheckInBtn.setText("Start Check-In");

        }
    }

    @FXML
    // 公网模式连接到服务器
    protected void connectToDedicatedServer() {

    }

    // 填充班级下拉菜单
    private void fillComboBox() {
        for (String classId : classes.keySet())
            classComboBox.getItems().add(classId);
    }

    // 填充签到表
    private void fillTableView() {

    }

    // 读取签到表文件
    private void readCheckInData() {
        try {
            File file = new File("HyperionData");

            if (!file.exists())
                return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String classId;
            int checkInCount;
            ConcurrentHashMap<String, Student> studentsInClass;

            String studentInfo;
            String studentId, studentMac;
            Vector<String> checkVector;

            while ((classId = reader.readLine()) != null) {
                checkInCount = Integer.parseInt(reader.readLine());
                studentsInClass = new ConcurrentHashMap<>();

                while ((studentInfo = reader.readLine()) != null) {
                    if (studentInfo.equals(""))
                        break;

                    String[] strings = studentInfo.split("\\s");

                    studentId = strings[0];
                    studentMac = strings[1];

                    checkVector = new Vector<>(Arrays.asList(strings).subList(2, strings.length));

                    Student student = new Student(studentId, studentMac, checkVector);
                    studentsInClass.put(studentId, student);
                }

                Class theClass = new Class(classId, checkInCount, studentsInClass);
                classes.put(classId, theClass);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 写入签到表文件
    private void writeCheckInData() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("HyperionData")));

            StringBuilder content = new StringBuilder();

            for (Class theClass : classes.values()) {
                content.append(theClass.getId()).append("\n").append(theClass.getCheckInCount()).append("\n");

                for (Student student : theClass.getStudents()) {
                    content.append(student.getId()).append(" ").append(student.getMac());

                    for (String checkIn : student.getCheckList())
                        content.append(" ").append(checkIn);

                    content.append("\n");
                }
                content.append("\n");
            }

            writer.write(content.toString());

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
