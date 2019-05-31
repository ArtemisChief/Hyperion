package DedicatedServer.Entity;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Class {

    private String id;

    private int checkInCount;

    private HashMap<String, Student> studentHashMap;

    public Class(String id, int checkInCount) {
        this.id = id;
        this.checkInCount = checkInCount;
        this.studentHashMap = new LinkedHashMap<>();
    }

    public Class(String id, int checkInCount, HashMap<String, Student> studentList) {
        this.id = id;
        this.checkInCount = checkInCount;
        this.studentHashMap = studentList;
    }

    public String getId() {
        return id;
    }

    public int getCheckInCount() {
        return checkInCount;
    }

    public HashMap<String, Student> getStudentList() {
        return studentHashMap;
    }

    public void setCheckInCount(int checkInCount) {
        this.checkInCount = checkInCount;
    }

}
