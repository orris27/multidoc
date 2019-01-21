package utils;


import javafx.application.Platform;
import utils.*;
import view.DocController;
import view.HomeController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SocketClient {
    private static SocketClient socketClient = null;

    private String host = "127.0.0.1";
    private int port = 2333;
    private Socket socket = null;
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;
    private boolean isConnected = false;
    private boolean isLogin = false;

    private User user;

    private HomeController homeController;
    public void setHomeController(HomeController homeController) {
        System.out.println("set home controller");
        this.homeController = homeController;
    }

    private DocController docController;
    public void setDocController(DocController docController){
        this.docController=docController;
    }

    public boolean getLoginState (){
        return isLogin;
    }

    private SocketClient(){
        try{
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            new Handler(inputStream).start();
            System.out.println("Connected");
            isConnected=true;

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public Boolean readBoolean() {
        try{
            Boolean a =  inputStream.readBoolean();
            return a;
        } catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }
    public void signup(String username, String password) {
        try {
            outputStream.writeObject(new SocketData<>(
                    "signup",
                    new Login(username, password)
            ));

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void login(String username, String password) {
        try {
            outputStream.writeObject(new SocketData<>(
                    "login",
                    new Login(username, password)
            ));

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addDocument(String title){
        try{
            outputStream.writeObject(new SocketData<>(
                    "addDocument",
                    title
            ));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void startDocumentEditing(int id){
        try{
            outputStream.writeObject(new SocketData<>(
                    "startEditDocument",
                    new Integer(id)
            ));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void stopDocumentEditing(){
        try{
            outputStream.writeObject(new SocketData<>(
                    "stopEditDocument",
                    null
            ));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void editDocument(String content){
        try{
            outputStream.writeObject(new SocketData<>(
                    "editDocument",
                    content
            ));
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static SocketClient getSocketClient(){
        if(socketClient==null){
            socketClient = new SocketClient();
        }

        return socketClient;
    }

    private class Handler extends Thread {
        ObjectInputStream in;
        Handler(ObjectInputStream in){
            this.in=in;
        }
        public void run(){
            try {
                while (true){
                    SocketDataBase data = (SocketDataBase) in.readObject();
                    System.out.println("Data received: "+data.getMeta());
                    switch (data.getMeta()){
                        case "updateUser":
                            handleUpdateUser(data);
                            break;
                        case "updateDocuments":
                            handleUpdateDocuments(data);
                            break;
                        case "updateDocument":
                            handleUpdateDocument(data);
                            break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        private void handleError(SocketDataBase data){
            String msg =((SocketData<String>)data).getData();
//            if (msg.equals("incorrect")) {
//                Platform.runLater(()->homeController.handlePasswordError());
//            }


        }
        private void handleUpdateUser(SocketDataBase data){
            isLogin = true;
            user=((SocketData<User>)data).getData();
            Platform.runLater(()->homeController.updateUser(user));
        }
        private void handleUpdateDocuments(SocketDataBase data){
            ArrayList<Document> documents=((SocketData<ArrayList<Document>>)data).getData();
            System.out.println(documents.size());
            homeController.updateDocumentList(documents);
        }
        private void handleUpdateDocument(SocketDataBase data){
            String content=((SocketData<String>)data).getData();
            Platform.runLater(()->{
                System.out.println("document update value:");
                System.out.println(content);
                docController.setText(content);
            });
        }
    }

}


