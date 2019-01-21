package utils;

import java.io.Serializable;

public class SocketMessageBase implements Serializable {
    String meta;
    public String getMeta(){
        return meta;
    }
}
