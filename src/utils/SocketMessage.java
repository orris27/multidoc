package utils;


public class SocketMessage<T> extends SocketMessageBase {
    private T data;

    private SocketMessage(){}

    public SocketMessage(String meta, T data){
        this.meta=meta;
        this.data=data;
    }

    public T getData(){
        return data;
    }
}
