import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final int PORT = 8188;
    private static MyServer server;

    private static MyServer getServer(){
        return server;
    }
    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    private static final Logger LOG = LogManager.getLogger(MyServer.class.getName());

    public MyServer(){
        try (ServerSocket server = new ServerSocket(PORT)){
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true){
                LOG.info("Сервер ожидает подключения...");
                Socket socket = server.accept();
                LOG.info("Клиент подключился ");
                new ClientHandler(this, socket);
            }
        } catch (IOException e){
            e.printStackTrace();
            LOG.error("Ошибка в работе сервера");
        }finally {
            if (authService != null){
                authService.stop();
            }
        }
    }

    public synchronized void broadcastMsg(String msg){
        for (ClientHandler o: clients) {
            o.sendMsg(msg);
        }
    }

    public synchronized void sendMsgToClient(ClientHandler from, String nickTo, String msg){
        for (ClientHandler o: clients){
            if (o.getName().equals(nickTo)){
                o.sendMsg("От " + from.getName() + ": " + msg);
                from.sendMsg("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("Пользователя " + nickTo + " нет в чате");
    }

    public synchronized void loadLogMsgs(String nick, String msg){
        for (ClientHandler o: clients){
            if (o.getName().equals(nick)){
                o.sendMsg(msg);
                return;
            }
        }
    }


    public synchronized void broadcastClientsList(){
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o: clients){
            sb.append(o.getName() + " ");
        }
        broadcastMsg(sb.toString());
    }

    public synchronized void unsubscribe(ClientHandler o){
        clients.remove(o);
        broadcastClientsList();
    }

    public synchronized void subscribe(ClientHandler o){
        clients.add(o);
        broadcastClientsList();
    }
}
