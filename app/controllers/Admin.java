package controllers;

import models.Problem;
import models.ProblemManager;
import models.UsernameManager;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;

import java.io.IOException;
import java.util.List;

public class Admin extends Controller {
    private static UsernameManager usernameManager = new UsernameManager();
    private static ProblemManager problemManager = new ProblemManager();


    public static void index() {

    }

    public static void problems() throws IOException {
        List<Problem> problems = problemManager.getAll();
        render(problems);
    }

    public static void editProblem(String problemId) throws IOException {
        Problem problem = new Problem();
        if (StringUtils.isNotBlank(problemId)) {
            problem = problemManager.find(problemId);
        }
        render(problem);
    }

    public static void addOrUpdateProblem(String problemId, String content, String validation) throws IOException {
        Problem problem = new Problem();
        problem.id = problemId;
        problem.content = content;
        problem.validation = validation;
        if (StringUtils.isBlank(problem.id)) {
            problemManager.add(problem);
            flash("message", "更新成功");
        } else {
            problemManager.update(problem);
            flash("message", "更新成功");
        }
        problems();
    }

    public static void users() throws IOException {
        String content = usernameManager.readFileContent();
        render(content);
    }

    public static void updateUserFile(String content) throws IOException {
        usernameManager.update(content);
        users();
    }

    public static void deleteProblem(String id) throws IOException {
        problemManager.deleteProblem(id);
        flash("message", "删除成功");
        problems();
    }

}
