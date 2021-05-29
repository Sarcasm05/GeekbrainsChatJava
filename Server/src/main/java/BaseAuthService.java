import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService{
    private class Entry{
        private String login;
        private String pass;
        private String nick;


        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен.");
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }

    public BaseAuthService(){
        entries = new ArrayList<>();
        entries.add(new Entry("qwe", "qwe", "qwe_nick"));
        entries.add(new Entry("asd", "asd", "asd_nick"));
        entries.add(new Entry("zxc", "zxc", "zxc_nick"));
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries ){
            if (o.login.equals(login) && o.pass.equals(pass))
                return o.nick;
        }
        return null;
    }
}
