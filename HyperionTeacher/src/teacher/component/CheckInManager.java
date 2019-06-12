package teacher.component;

import teacher.entity.Class;
import teacher.entity.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class CheckInManager {

    // 单例
    private static CheckInManager ourInstance = new CheckInManager();

    // 得到单例
    public static CheckInManager getInstance() {
        return ourInstance;
    }

    // 班级表
    private ConcurrentHashMap<String, Class> classes;

    // 当前签到的班级
    private String currentClassId;

    // 构造函数
    private CheckInManager() {
        classes = new ConcurrentHashMap<>();
        currentClassId = null;
    }

    // 得到班级表
    public ConcurrentHashMap<String, Class> getClasses() {
        return classes;
    }

    // 得到当前签到班级
    public Class getCurrentClass() {
        if (currentClassId == null)
            return null;

        return classes.get(currentClassId);
    }

    // 设置当前签到班级
    public void setCurrentClass(String currentClassId) {
        this.currentClassId = currentClassId;
    }

    // 从字符串中读取班级信息
    public void readClassesFromString(BufferedReader reader) throws IOException {
        classes = new ConcurrentHashMap<>();

        String classId;
        int checkInCount;
        ConcurrentHashMap<String, Student> studentsInClass;

        String studentInfo;
        String studentId, studentMac;
        Vector<String> checkVector;

        while (!(classId = reader.readLine()).equals(".")) {
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
    }

    // 转换班级信息到字符串
    public String writeClassToString() {
        StringBuilder content = new StringBuilder();

        for (Class theClass : classes.values()) {
            content.append(theClass.getId()).append("\n").append(theClass.getCheckInCount()).append("\n");

            for (Student student : theClass.getStudentsInClass().values()) {
                content.append(student.getId()).append(" ").append(student.getMac());

                Vector<String> checkList = student.getCheckList();

                if (currentClassId != null && theClass == getCurrentClass())
                    for (int i = checkList.size(); i < getCurrentClass().getCheckInCount(); i++)
                        checkList.add("\\");

                for (String checkIn : checkList)
                    content.append(" ").append(checkIn);

                content.append("\n");
            }
            content.append("\n");
        }
        content.append(".");

        return content.toString();
    }
}
