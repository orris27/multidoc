package server;

import com.sun.javafx.applet.ExperimentalExtensions;
import proto.*;



import javax.xml.stream.FactoryConfigurationError;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import redis.clients.jedis.Jedis;
import java.sql.*;


public class Main {


    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/multidoc";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "serena2ash";


    private static final int PORT = 2333;
    private static Connection conn = null;
    private static Statement stmt = null;


    private static Jedis jedis;
    private static HashSet<User> users = new HashSet<>();
    private static User getUser(int id){
        for (User d: users){
            if(d.getId()==id) return d;
        }
        return null;
    }
    private static User getUser(String username){
        for (User d: users){
            if(d.getUsername().equals(username)) return d;
        }
        return null;
    }

    private static HashSet<Document> documents = new HashSet<>();
    private static Document getDocument(int id){
        for (Document d: documents){
            if(d.getId()==id) return d;
        }
        return null;
    }

    private static HashMap<User, ObjectOutputStream> userToWriterMap = new HashMap<>();
    private static HashMap<User, Document> userToDocumentMap = new HashMap<>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    private static void storeRedis(String key, Object value) {
        try {

            //然后通过 jedis 对象就可以调用 redis 支持的命令了，比如
            jedis.set("hello","world");
        } catch (Exception e){
            e.printStackTrace();
        }


    }
    public static void main(String[] args) throws Exception {
        //生成一个Jedis对象，这个对象负责和指定Redis实例进行通信
        jedis = new Jedis("127.0.0.1",6379);


        try{
            Class.forName(JDBC_DRIVER);
            System.out.println("driver ok");
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        try{
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("connect to mysql success");
        } catch(SQLException e){
            e.printStackTrace();
        }

        try{
            stmt = conn.createStatement();
            stmt.executeQuery("select * from users");
        } catch(Exception e) {
            String sql = "create table users(id int, username varchar(20), password varchar(20), primary key(id))";
            stmt.executeUpdate(sql);
            stmt.executeUpdate("insert into users values(0, 'admin', 'admin')");
        }


        initData();
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
            if (jedis != null) {
                //使用完之后关闭连接
                jedis.close();
            }
        }
    }

    private static void initData(){


        try{
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery("select * from users where username = 'admin'");
            while(res.next()){
                int id = res.getInt("id");
                String username = res.getString("username");
                String password = res.getString("password");
                users.add(new User(id, username, password));
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        private User userForThisSocket;

        Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                writers.add(out);
                try {
                    while (true) {
                        System.out.println("going to read data");
                        SocketDataBase data = (SocketDataBase) in.readObject();
                        System.out.println(data);
                        if(data==null) break;
                        switch (data.getMeta()){
                            case "signup":
                                handleSignup(((SocketData<Login>)data).getData());
                                break;
                            case "login":
                                handleLogin(((SocketData<Login>)data).getData());
                                break;
                            case "addDocument":
                                handleAddDocument(((SocketData<String >)data).getData());
                                break;
                            case "startEditDocument":
                                handleStartEditDocument(((SocketData<Integer>)data).getData());
                                break;
                            case "stopEditDocument":
                                handleStopEditDocument();
                                break;
                            case "editDocument":
                                handleEditDocument(((SocketData<String>)data).getData());
                                break;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (out != null) {
                    writers.remove(out);
                    if(userForThisSocket!=null){
                        userToWriterMap.remove(userForThisSocket);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void handleSignup(Login login) throws IOException{
            User user = getUser(login.getUsername());
            if (user == null){
                return;
            }else{
                if(user.checkPassword(login.getPassword())){
                    userForThisSocket=user;
                    userToWriterMap.put(userForThisSocket,out);
                    out.writeObject(new SocketData<>(
                            "updateUser",
                            user
                    ));
                    updateDocuments();
                }
            }
        }

        private void handleLogin(Login login) throws IOException{
            User user = getUser(login.getUsername());
            if (user==null){
                return;
            }else{
                if(user.checkPassword(login.getPassword())){
                    userForThisSocket=user;
                    userToWriterMap.put(userForThisSocket,out);
                    out.writeObject(new SocketData<>(
                            "updateUser",
                            user
                    ));
                    updateDocuments();
                }
            }
        }
        private void handleAddDocument(String title) throws IOException{
            documents.add(new Document(userForThisSocket,title));
            System.out.println(documents.size());
            updateDocuments();
        }
        private void handleStartEditDocument(Integer id)throws IOException{
            Document document=getDocument(id);
            userToDocumentMap.put(userForThisSocket,document);
            out.writeObject(new SocketData<>(
                    "updateDocument",
                    document.getContent()
            ));
        }
        private void handleStopEditDocument(){
            userToDocumentMap.remove(userForThisSocket);
        }
        private void handleEditDocument(String content)throws IOException{
            Document document=userToDocumentMap.get(userForThisSocket);
            document.setContent(content);
            System.out.println(content);
            for(HashMap.Entry<User,Document> entry: userToDocumentMap.entrySet()){
                // if it is the document modified
                if(entry.getValue()==document){
                    // get the writer of the user
                    ObjectOutputStream writer=userToWriterMap.get(entry.getKey());
                    if(writer!=out){
                        // write them again completely
                        writer.writeObject(new SocketData<>(
                                "updateDocument",
                                document.getContent()
                        ));
                    }
                }
            }
        }

        private void updateDocuments() throws IOException{
            System.out.println("update documents");
            for(ObjectOutputStream out: writers){
                out.writeObject(new SocketData<>(
                        "updateDocuments",
                        new ArrayList<>(documents)
                ));
            }
        }
    }

}