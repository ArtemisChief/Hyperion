package DedicatedServer.Entity;

import java.util.Vector;

public class Student {

    private String id;

    private String mac;

    private Vector<String> checkVector;

    public Student(String id, String mac) {
        this.id = id;
        this.mac = mac;
        this.checkVector = new Vector<>();
    }

    public Student(String id, String mac, Vector<String> checkVector) {
        this.id = id;
        this.mac = mac;
        this.checkVector = checkVector;
    }

    public String getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public Vector<String> getCheckList() {
        return checkVector;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setCheckVector(Vector<String> checkVector) {
        this.checkVector = checkVector;
    }

    public String getIsChecked(int count) {
        if (count > checkVector.size())
            return "";
        else if (!checkVector.get(count - 1).equals("\\"))
            return "√";
        else
            return "×";
    }

}
