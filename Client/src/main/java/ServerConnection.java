import java.io.IOException;
import java.net.Socket;

public class ServerConnection {
    private static String SERVER_IP = "localhost";
    private static int SERVER_PORT = 8188;
    private static Socket socket;

    public static Socket getSocket() throws IOException{
        if (socket == null) {
            socket = new Socket(SERVER_IP, SERVER_PORT);
        }
        return socket;
    }
}
