package LocalServer.Component;

class StudentController {

    /**
     * 返回结果
     * 0 - 签到成功
     * 1 - 签到失败，学号与MAC地址不匹配
     * 2 - 重复签到
     * 3 - 本轮签到已关闭
     */

    static String Process(String content){
        LocalServer.status.get();
        return "0";
    }



}
