package cspackage;

import java.io.Serializable;

public class Message implements Serializable {	//Message serializable, taken from Lab 6
    private String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
