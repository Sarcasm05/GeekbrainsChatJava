import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.ReversedLinesFileReader;

public class ChatLogging {
    private File logfile;
    private BufferedWriter writer;
    private ReversedLinesFileReader reader;

    public ChatLogging(String login) {
        //String filePath = "history_" + login + ".txt";
        this.logfile = new File("history_" + login + ".txt");
        if (!logfile.exists()){
            try {
                logfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readFromLog(){
        List<String> resultReadLine = readLastLine(logfile, 100);
        StringBuilder sb = new StringBuilder();
        for (String s : resultReadLine) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static List readLastLine(File file, int numLastLineToRead) {
        List<String> result = new ArrayList<>();
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
            String line = "";
            while ((line = reader.readLine()) != null && result.size() < numLastLineToRead) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void writeToLog(String txt){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logfile, true));) {
            writer.write(txt);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
