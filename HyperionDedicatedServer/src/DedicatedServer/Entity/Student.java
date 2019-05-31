package DedicatedServer.Entity;

import java.util.ArrayList;
import java.util.List;

public class Student {

    private String id;

    private String mac;

    private List<Integer> checkList;

    public Student(String id, String mac) {
        this.id = id;
        this.mac = mac;
        this.checkList = new ArrayList<>();
    }

    public Student(String id, String mac, List<Integer> checkList) {
        this.id = id;
        this.mac = mac;
        this.checkList = checkList;
    }

    public String getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public List<Integer> getCheckList() {
        return checkList;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}
