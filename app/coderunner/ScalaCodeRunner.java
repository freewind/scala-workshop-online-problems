package coderunner;

import com.google.gson.Gson;
import models.Problem;
import models.ProblemManager;
import org.apache.commons.io.FileUtils;
import play.Play;

import java.io.File;
import java.io.IOException;

import static utils.Helper.notBlank;

public class ScalaCodeRunner {

    public static final int WRONG_RESULT = 444;
    private final long submitId;
    private ProblemManager problemManager = new ProblemManager();

    private String userSubmitsDir = Play.configuration.getProperty("user.submits.dir");

    public static final int MAX_RUNNING_SECONDS = 10;
    private String username;
    private String problemId;
    private String userCode;
    private File codeFile;
    private long submitTime;
    private File submitDir;

    public ScalaCodeRunner(String username, String problemId, String userCode) {
        notBlank(username, "username");
        notBlank(problemId, "problemId");
        notBlank(userCode, "userCode");

        this.username = username;
        this.problemId = problemId;
        this.userCode = userCode;
        this.submitTime = System.currentTimeMillis();
        this.submitId = this.submitTime;
    }

    public SubmitResult run() throws IOException, InterruptedException {
        submitDir = new File(userSubmitsDir, username + "/" + problemId + "/" + submitId);
        submitDir.mkdirs();

        generateUserAnswerFile();

        // compile
        ProcessBuilder pb = new ProcessBuilder("scalac", codeFile.getAbsolutePath());
        pb.directory(codeFile.getParentFile());
        pb.redirectErrorStream(true);
        File logFile = File.createTempFile("code", ".log");
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        Process p = pb.start();
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            SubmitResult result = createResult(logFile, exitValue, determineResultWhenCompile(exitValue));
            saveResult(result);
            return result;
        }

        Problem problem = problemManager.find(problemId);
        SubmitResult result = runScala(problem);

        saveResult(result);
        return result;
    }

    private void generateUserAnswerFile() throws IOException {
        codeFile = new File(submitDir, "app.scala");
        FileUtils.writeStringToFile(codeFile, new AnswerCodeGenerator(problemId, userCode).generate());
    }


    private void saveResult(SubmitResult result) throws IOException {
        String json = new Gson().toJson(result);
        FileUtils.writeStringToFile(new File(submitDir, "result.json"), json, "UTF-8");
    }

    private SubmitResult createResult(File logFile, int exitValue, String result) throws IOException {
        SubmitResult submitResult = new SubmitResult();
        submitResult.id = "" + submitTime;
        submitResult.exitValue = exitValue;
        submitResult.result = result;
        submitResult.problemId = problemId;
        submitResult.username = username;
        submitResult.submitTime = submitTime;
        submitResult.message = FileUtils.readFileToString(logFile);
        submitResult.userCode = userCode;
        return submitResult;
    }

    private SubmitResult runScala(Problem problem) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("scala", "App");
        pb.directory(codeFile.getParentFile());
        pb.redirectErrorStream(true);
        File logFile = File.createTempFile("code", ".log");
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        Process p = pb.start();

        new KillTimeoutProcess(p, MAX_RUNNING_SECONDS).start();

        int exitValue = p.waitFor();

        return createResult(logFile, exitValue, determineResultWhenRun(exitValue));
    }

    private String determineResultWhenCompile(int exitValue) {
        if (exitValue == 1) {
            return "编译出错";
        }
        return "未知结果: " + exitValue;
    }


    private String determineResultWhenRun(int exitValue) {
        if (exitValue == 0) {
            return "成功";
        } else if (exitValue == 143) {
            return "超时，未在" + MAX_RUNNING_SECONDS + "秒内执行完";
        } else if (exitValue == 1) {
            return "运行出错";
        } else if (exitValue == WRONG_RESULT) {
            return "结果不正确";
        }
        return "未知结果: " + exitValue;
    }
}

