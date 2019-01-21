package view;

import utils.SocketClient;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.Main;
import utils.Document;
import utils.User;
import utils.PassageRow;

import java.util.ArrayList;
import java.util.Optional;


public class MenuController {
    @FXML
    private TableView<PassageRow> documentTable;
    @FXML
    private TableColumn<PassageRow, String> creatorColumn;
    @FXML
    private TableColumn<PassageRow, String> titleColumn;
    @FXML
    private Button loginButton;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button addDocumentButton;


    private ObservableList<PassageRow> passageRowList = FXCollections.observableArrayList();

    public ObservableList<PassageRow> getPassageRowList() {
        return passageRowList;
    }


    // Reference to the main application.
    private Main mainApp;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public MenuController() {
        System.out.println("constructor of controller");
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        passageRowList.addListener((ListChangeListener<PassageRow>) change -> {
            documentTable.setItems(passageRowList);
//            while (change.next()) {
//                if (change.wasUpdated()) {
//                    SomeObservableClass changedItem = observableList.get(change.getFrom());
//                    System.out.println("ListChangeListener item: " + changedItem);
//                }
//            }
        });
        creatorColumn.setCellValueFactory(cellData -> cellData.getValue().creatorProperty());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        documentTable.setRowFactory(tv -> {
            TableRow<PassageRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PassageRow passageRow = row.getItem();
                    this.mainApp.openDocument(passageRow.getId());
                    System.out.println(passageRow.getTitle());
                }
            });
            return row ;
        });
    }


    @FXML
    private void loginButtonClicked(){
//        usernameLabel.setText("test login");
        TextInputDialog dialog;
        Optional<String> result;
        dialog = new TextInputDialog("");
        dialog.setTitle("Login");
        dialog.setHeaderText("Username");
        dialog.setContentText("Please enter your username:");
        result = dialog.showAndWait();
        String username;
        if (result.isPresent()){
            username=result.get();
        }else{
            return;
        }
        dialog = new TextInputDialog("");
        dialog.setTitle("Login");
        dialog.setHeaderText("Password");
        dialog.setContentText("Please enter your password:");
        result = dialog.showAndWait();
        String password;
        if (result.isPresent()){
            password=result.get();
        }else{
            return;
        }
        SocketClient.getSocketClient().login(username,password);

    }

//    @FXML
//    public void handlePasswordError(){
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Information Dialog");
//        alert.setHeaderText("Look, an Information Dialog");
//        alert.setContentText("I have a great message for you!");
//
//        alert.showAndWait();
//
//
//    }

    @FXML
    private void addDocumentButtonClicked(){
        TextInputDialog dialog;
        Optional<String> result;
        dialog = new TextInputDialog("");
        dialog.setTitle("Add document");
        dialog.setHeaderText("Add document");
        dialog.setContentText("Please enter the title:");
        result = dialog.showAndWait();
        if (result.isPresent()){
            SocketClient.getSocketClient().addDocument(result.get());
        }
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        // Add observable list data to the table
        documentTable.setItems(getPassageRowList());
    }

    public void updateUser(User user){
        if(user==null){
            usernameLabel.setText("Please login");
            loginButton.setVisible(true);
            addDocumentButton.setVisible(false);
        }else{
            usernameLabel.setText(user.getUsername());
            loginButton.setVisible(false);
            addDocumentButton.setVisible(true);
        }
    }

    public void updateDocumentList(ArrayList<Document> documents){
        passageRowList.clear();
        for (Document document: documents) {
            passageRowList.add(new PassageRow(document.getId(), document.getCreator().getUsername(), document.getTitle()));
        }
    }

}
