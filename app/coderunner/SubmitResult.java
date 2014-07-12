package coderunner;

import java.text.SimpleDateFormat;

public class SubmitResult {

    public String id;
    public int exitValue;
    public String code;
    public String problemId;
    public String message;
    public String username;
    public String result;
    public long submitTime;

    public boolean isPassed() {
        return exitValue == 0;
    }

    public String getSubmitTimeFormatted() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(submitTime);
    }
}
