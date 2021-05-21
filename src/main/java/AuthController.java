import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AuthController {
    @FXML
    private TextField loginTF;
    @FXML
    private PasswordField passwordTF;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    private void initialize() throws IOException {
        Socket socket = ServerConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith("/auth_ok")){
                            Config.nick = strFromServer.split(" ")[1];
                            Platform.runLater(() -> {
                                Stage stage = (Stage) loginTF.getScene().getWindow();
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
