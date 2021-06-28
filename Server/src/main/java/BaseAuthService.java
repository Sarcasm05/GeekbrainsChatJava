import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOG = LogManager.getLogger(BaseAuthService.class.getName());
    private List<Entry> entries;

    @Override
    public void start() {
        LOG.info("Сервис аутентификации запущен.");
    }

    @Override
    public void stop() {
        LOG.info("Сервис аутентификации остановлен");
    }

    public BaseAuthService(){
        entries = new ArrayList<>();
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
