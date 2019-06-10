package Teacher;

import Teacher.Entity.Class;
import Teacher.Entity.Student;
import Teacher.ServerClient.DedicatedServer;
import Teacher.ServerClient.LocalServer;
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

public class Controller {

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

    @FXML
    // 初始化
    protected void initialize() {
        LocalServer.setTableView(tableView);
        DedicatedServer.setTableView(tableView);

        tableColumns = new TableColumn[]{col1, col2, col3, col4, col5, col6, col7, col8, col9};

        stuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        stuMacCol.setCellValueFactory(new PropertyValueFactory<>("mac"));
        bindIsChecked(5);

        LocalServer.launch();

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
            if (classComboBox.getValue() == null || classComboBox.getValue().equals("")) {
                showSimpleAlert(Alert.AlertType.WARNING, "Waring", "You need to choose a class to start check-in");
                toggleCheckInBtn.setSelected(false);
                return;
            }

            toggleCheckInBtn.setText("Stop Check-In");
            timesSlider.setDisable(true);
            classComboBox.setDisable(true);

            Class.CurrentClassId = classComboBox.getValue();
            if (Class.GetCurrentClass() == null) {
                Class.Classes.put(Class.CurrentClassId, new Class(Class.CurrentClassId, (int) timesSlider.getValue()));
                fillComboBox();
            }
            else
                Class.GetCurrentClass().setCheckInCount((int) timesSlider.getValue());
        } else {
            toggleCheckInBtn.setText("Start Check-In");
            timesSlider.setDisable(false);
            classComboBox.setDisable(false);

            Class.CurrentClassId = null;
        }
    }

    @FXML
    // 公网模式连接到服务器
    protected void connectToDedicatedServer() {

    }

    @FXML
    // 填充签到表
    public void fillTableView() {
        if (Class.Classes.get(classComboBox.getValue()) == null)
            tableView.getItems().clear();
        else {
            ObservableList<Student> studentObservableList = FXCollections.observableArrayList(Class.Classes.get(classComboBox.getValue()).getStudentsInClass().values());
            tableView.setItems(studentObservableList);
        }
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
        for (String classId : Class.Classes.keySet())
            if (!classComboBox.getItems().contains(classId))
                classComboBox.getItems().add(classId);
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
