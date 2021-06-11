import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.*;

public class ClientHandler {
    private String login;
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatLogging chatLog;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            this.login = "";
            new Thread(() ->{
                try {
                    authentication();
                    readMessages();
                }catch (SocketTimeoutException e){
                    sendMsg("/end");
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    closeConnection();
                }
            }).start();
        }catch (IOException e){
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            socket.setSoTimeout(120000);
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                try (Connection postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/geekbrains", "admin", "admin")) {
                    PreparedStatement prepareStatement = postgresConnection.prepareStatement("select nickname from \"Clients\" where login=? and password=?");
                    prepareStatement.setString(1, parts[1]);
                    prepareStatement.setString(2, parts[2]);
                    ResultSet clientsResultSet = prepareStatement.executeQuery();
                    postgresConnection.close();
                    if (clientsResultSet != null) {
                        while (clientsResultSet.next()) {
                            name = clientsResultSet.getString("nickname");
                            login = parts[1];
                            socket.setSoTimeout(0);
                            chatLog = new ChatLogging(login);
                            sendMsg("/auth_ok " + name);
                            myServer.broadcastMsg(name + " зашел в чат");
                            myServer.subscribe(this);
                            myServer.loadLogMsgs(name, chatLog.readFromLog());
                            return;
                        }
                    } else {
                        sendMsg("Неверные логин/пароль");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public void readMessages() throws IOException {
        while (socket.isConnected()) {
            String str = in.readUTF();
            if (str.startsWith("/")){
                if (str.equals("/end")) {
                    closeConnection();
                }
                if (str.startsWith("/w ")){
                    String[] tokens = str.split("\\s");
                    String nick = tokens[1];
                    String msg = str.substring(4 + nick.length());
                    myServer.sendMsgToClient(this, nick, msg);
                }
                if (str.startsWith("/changenickname")) { // /nickname newnickname
                    String[] tokens = str.split("\\s");
                    String newnickname = tokens[1];
                    try (Connection postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/geekbrains", "admin", "admin")) {
                        PreparedStatement prepareStatement = postgresConnection.prepareStatement("update \"Clients\" set nickname=?  where nickname=?");
                        prepareStatement.setString(1, newnickname);
                        prepareStatement.setString(2, name);
                        prepareStatement.executeUpdate(); // TODO добавить проверку
                        myServer.broadcastMsg("Пользователь " + name + " заменил ник на " + newnickname);
                        name = newnickname;
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                continue;
            }
            String broadcastMsg = name + ": " + str;
            myServer.broadcastMsg(broadcastMsg);
        }
    }


    public void sendMsg(String msg){
        try {
            out.writeUTF(msg + "\n");
            if (!msg.startsWith("/")){
                chatLog.writeToLog(msg + "\n");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
