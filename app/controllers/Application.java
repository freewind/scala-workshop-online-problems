package controllers;

import coderunner.AnswerCodeGenerator;
import coderunner.ScalaCodeRunner;
import coderunner.SubmitResult;
import models.*;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import utils.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class Application extends Controller {

    public static final String USERNAME_SESSION_KEY = "username";
    private static UsernameManager usernameManager = new UsernameManager();
    private static ProblemManager problemManager = new ProblemManager();
    private static UserSubmitManager userSubmitManager = new UserSubmitManager();

    @Before(unless = {"index", "login"})
    static void checkAuthentification() throws IOException {
        tryToRememberMe();
        if (session.get(USERNAME_SESSION_KEY) == null) index();
    }

    private static void tryToRememberMe() {
        Http.Cookie tokenCookie = request.cookies.get("token");
        if (tokenCookie != null) {
            String value = tokenCookie.value;
            String username = StringUtils.substringBefore(value, ":");
            if (StringUtils.equals(username + ":" + Crypto.sign(username), value)) {
                session.put(USERNAME_SESSION_KEY, username);
            }
        }
    }

    public static void index() throws IOException {
        if (isNotBlank(session.get(USERNAME_SESSION_KEY))) {
            center();
        }

        List<String> usernames = usernameManager.getAll();
        String scalaVersion = getScalaVersion();
        render(usernames, scalaVersion);
    }

    private static String getScalaVersion() {
        return Play.configuration.getProperty("scala.version");
    }

    public static void login(String username, String password) throws IOException {
        if (passwordIsCorrect(password) && usernameIsCorrect(username)) {
            session.put(USERNAME_SESSION_KEY, username);
            response.setCookie("token", username + ":" + Crypto.sign(username), "100000d");
            center();
        } else {
            flash("error", "用户名或密码不正确");
            params.flash();
            index();
        }
    }

    // ------------------ following need login ----------------------

    public static void runCode(String problemId, String code) throws IOException, InterruptedException {
        String username = getSessionUsername();
        ScalaCodeRunner runner = new ScalaCodeRunner(username, problemId, code);
        SubmitResult submitResult = runner.run();
        render(submitResult);
    }

    private static boolean usernameIsCorrect(String username) throws IOException {
        return isNotBlank(username) && usernameManager.getAll().contains(username.trim());
    }

    private static boolean passwordIsCorrect(String password) {
        return isNotBlank(password) && StringUtils.equals(password.trim(), Play.configuration.getProperty("password"));
    }

    public static void center() throws IOException {
        List<Problem> problems = problemManager.getAll();
        List<UserSummary> userSummaries = userSubmitManager.getSummaries();
        render(problems, userSummaries);
    }

    public static void showProblem(String id, String code) throws IOException {
        Problem problem = problemManager.find(id);

        String username = getSessionUsername();
        List<SubmitResult> submits = userSubmitManager.get(username, id);

        code = StringUtils.trimToEmpty(code);
        render(problem, submits, code);
    }

    public static void showTestData(String problemId) throws IOException {
        Problem problem = problemManager.find(problemId);
        render(problem);
    }

    public static void showAllSubmits(String problemId, String currentUsername) throws IOException {
        List<Pair<String, Integer>> userAndSubmitCount = userSubmitManager.getUserAndSubmitCountForProblem(problemId);
        if (isBlank(currentUsername) && !userAndSubmitCount.isEmpty()) {
            currentUsername = userAndSubmitCount.get(0).key;
        }

        List<SubmitResult> submits = new ArrayList<SubmitResult>();
        if (isNotBlank(currentUsername)) {
            submits = userSubmitManager.get(currentUsername, problemId);
        }
        render(userAndSubmitCount, currentUsername, submits, problemId);
    }

    public static void deleteSubmit(String problemId, String submitId) throws IOException {
        String username = getSessionUsername();
        userSubmitManager.deleteSubmit(username, problemId, submitId);
        showProblem(problemId, "");
    }

    public static void previewAnswerCode(String problemId, String userCode) throws IOException {
        String code = new AnswerCodeGenerator(problemId, userCode).generate();
        render(problemId, code);
    }

    public static void showDemo() {
        render();
    }

    private static String getSessionUsername() {
        return session.get(USERNAME_SESSION_KEY);
    }

}
