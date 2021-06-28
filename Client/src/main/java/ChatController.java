import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.IOP.TAG_JAVA_CODEBASE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatController {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    public TextArea chatArea;
    @FXML
    public TextField inputField;

    private static final Logger LOG = LogManager.getLogger(ChatController.class.getName());

    @FXML
    private void initialize(){
        try {
            openLoginWindow();
            Main.mainStage.setTitle(Main.mainStage.getTitle() + " (" + Config.nick + ")");
            openConnection();
            addCloseListener();
        }catch (IOException e){
            LOG.error("Ошибка подключения, сервер не работает " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Сервер не работает");
            alert.setContentText("Не забудь включить сервер!");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void openLoginWindow() throws IOException {
        Parent root = FXMLLoader.load(ClassLoader.getSystemResource("auth.fxml"));
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(root));
        loginStage.setTitle("Вход");
        loginStage.showAndWait();
    }

    private void openConnection() throws IOException {
        socket = ServerConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                while (socket.isConnected()){
                    String strFromServer = in.readUTF();
                    System.out.println(strFromServer);
                    if (strFromServer.equalsIgnoreCase("/end")){
                        break;
                    }
                    chatArea.appendText(strFromServer);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addCloseListener(){
        EventHandler<WindowEvent> onCloseRequest = Main.mainStage.getOnCloseRequest();
        Main.mainStage.setOnCloseRequest(event -> {
            closeConnection();
            if (onCloseRequest != null){
                onCloseRequest.handle(event);
            }
        });
    }

    private void closeConnection(){
        try {
            out.writeUTF("/end");
            in.close();
            out.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        if (!inputField.getText().trim().isEmpty()){
            try {
                out.writeUTF(inputField.getText().trim());
                inputField.clear();
                inputField.requestFocus();
            }catch (IOException e){
                e.printStackTrace();
                LOG.error("Ошибка отправки сообщения " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ошибка отправки сообщения");
                alert.setHeaderText("Ошибка отправки сообщения");
                alert.setContentText("При отправке сообщения возникла ошибка: " + e.getMessage());
                alert.show();
            }
        }
    }
}
