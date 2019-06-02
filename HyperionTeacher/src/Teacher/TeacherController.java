package Teacher;

import LocalServer.Component.LocalServer;
import LocalServer.Entity.Class;
import LocalServer.Entity.Student;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private ComboBox<String> classComboBox;
    @FXML
    private Button connectBtn;
    @FXML
    private ToggleButton toggleCheckInBtn;
    @FXML
    private Slider timesSlider;
    @FXML
    private TableView<Student> tableView;
    @FXML
    private TableColumn<Student, String> stuIdCol;
    @FXML
    private TableColumn<Student, String> stuMacCol;
    @FXML
    private TableColumn<Student, String> col1;
    @FXML
    private TableColumn<Student, String> col2;
    @FXML
    private TableColumn<Student, String> col3;
    @FXML
    private TableColumn<Student, String> col4;
    @FXML
    private TableColumn<Student, String> col5;
    @FXML
    private TableColumn<Student, String> col6;
    @FXML
    private TableColumn<Student, String> col7;
    @FXML
    private TableColumn<Student, String> col8;
    @FXML
    private TableColumn<Student, String> col9;

    private TableColumn[] tableColumns;
    private ConcurrentHashMap<String, Class> classes;

    @FXML
    // 初始化
    protected void initialize() {
        classes = new ConcurrentHashMap<>();
        tableColumns = new TableColumn[]{col1, col2, col3, col4, col5, col6, col7, col8, col9};

        stuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        stuMacCol.setCellValueFactory(new PropertyValueFactory<>("mac"));
        bindIsChecked(5);

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
    // 设置当前签到次数
    private void setCurrentCheckInCount() {
        int curr = (int) timesSlider.getValue();

        if (curr < 6) {
            bindIsChecked(5);
            for (int i = 0; i < 9; i++)
                tableColumns[i].setText(Integer.toString(i + 1));
        } else if (curr > 15) {
            bindIsChecked(16);
            for (int i = 0; i < 9; i++)
                tableColumns[i].setText(Integer.toString(i + 12));
        } else {
            bindIsChecked(curr);
            for (int i = 0; i < 9; i++)
                tableColumns[i].setText(Integer.toString(curr - 4 + i));
        }
    }

    @FXML
    // 开启或关闭签到
    protected void StartCheckIn() {
        if (toggleCheckInBtn.isSelected()) {
            toggleCheckInBtn.setText("Stop Check-In");
        } else {
            toggleCheckInBtn.setText("Start Check-In");
            writeCheckInData();
        }
    }

    @FXML
    // 公网模式连接到服务器
    protected void connectToDedicatedServer() {

    }

    @FXML
    // 填充签到表
    private void fillTableView() {
        ObservableList<Student> studentObservableList = FXCollections.observableArrayList(classes.get(classComboBox.getValue()).getStudents());
        tableView.setItems(studentObservableList);
    }

    // 绑定签到次数列
    private void bindIsChecked(int count) {
        col1.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 4)));
        col2.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 3)));
        col3.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 2)));
        col4.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 1)));
        col5.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count)));
        col6.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 1)));
        col7.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 2)));
        col8.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 3)));
        col9.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 4)));
        tableView.refresh();
    }

    // 填充班级下拉菜单
    private void fillComboBox() {
        for (String classId : classes.keySet())
            classComboBox.getItems().add(classId);
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