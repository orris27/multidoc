package app;

import java.awt.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import view.DocController;
import view.HomeController;

import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Main extends Application {


    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{

        //网格布局
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        //网格垂直间距
        grid.setHgap(10);
        //网格水平间距
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        //新建场景
        Scene scene = new Scene(grid, 300, 275);
        primaryStage.setScene(scene);
        //添加标题
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        //添加标签及文本框
        Label userName = new Label("Username:");
        grid.add(userName, 0, 1);

        TextField usernameBox = new TextField();
        grid.add(usernameBox, 1, 1);
        //添加标签及密码框
        Label pwdLabel = new Label("Password:");
        grid.add(pwdLabel, 0, 2);

        PasswordField pwdBox = new PasswordField();
        grid.add(pwdBox, 1, 2);

        Button btnSignup = new Button("Sign up");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(btnSignup);
//        grid.add(hbBtn, 1, 4);
        //添加提交按钮
        Button btn = new Button("Log in");
//        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        FXMLLoader loader = new FXMLLoader();
        // store current stage
        loader.setLocation(getClass().getResource("../view/Home.fxml"));
        Parent root = loader.load();
        HomeController controller = loader.getController();
        // assign the controller to the socket
        SocketClient.getSocketClient().setHomeController(controller);

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        btnSignup.setOnAction(event -> {
            String username = usernameBox.getText();
            String pwd = pwdBox.getText();
            if(username.length() == 0 || pwd.length() == 0) {
                return;
            }
            SocketClient.getSocketClient().signup(username, pwd);

        });
        btn.setOnAction(event -> {
            String username = usernameBox.getText();
            String pwd = pwdBox.getText();
            if(username.length() == 0 || pwd.length() == 0) {
                return;
            }
            SocketClient.getSocketClient().login(username, pwd);


            primaryStage.setScene(new Scene(root, 600, 400));
            this.stage = primaryStage;
            controller.setMainApp(this);
        });

        primaryStage.setTitle("Login");
        primaryStage.show();




        // loader the node in the scene

//
//
//
//
//        loader.setLocation(Main.class.getResource("../view/Home.fxml"));
//        root = loader.load();
//
//        // show stage
//        primaryStage.setTitle("Multidoc");
//        primaryStage.setScene(new Scene(root, 600, 400));
//        primaryStage.show();

//        // store current stage
//        this.stage = primaryStage;
//        loader.setLocation(getClass().getResource("../view/Home.fxml"));
//        HomeController controller = loader.getController();
//        // assign the controller to the socket
//        SocketClient.getSocketClient().setHomeController(controller);
//        controller.setMainApp(this);
    }


    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void openDocument(int id){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../view/Doc.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            DocController controller = loader.getController();
            controller.setStage(dialogStage);
            controller.initDocument(id);
            SocketClient.getSocketClient().setDocController(controller);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
