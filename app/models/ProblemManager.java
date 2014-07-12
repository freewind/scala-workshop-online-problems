package models;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.io.FileUtils.readFileToString;

public class ProblemManager {

    public static final String PROBLEM_FILENAME = "problem.txt";
    public static final String VALIDATION_FILENAME = "validation.txt";
    private String root = Play.configuration.getProperty("problems.dir");

    public List<Problem> getAll() throws IOException {
        List<Problem> problems = new ArrayList<Problem>();
        File[] files = new File(root).listFiles();
        if (files == null) {
            throw new IOException("Can't find directory: " + root);
        }

        for (File problemDir : files) {
            Problem problem = new Problem();
            problem.id = problemDir.getName();
            problem.content = readFile(problemDir, PROBLEM_FILENAME);
            problem.validation = readFile(problemDir, VALIDATION_FILENAME);
            problems.add(problem);
        }

        sortProblems(problems);
        return problems;
    }

    private void sortProblems(List<Problem> problems) {
        Collections.sort(problems, new Comparator<Problem>() {
            @Override
            public int compare(Problem o1, Problem o2) {
                return Integer.parseInt(o1.id) - Integer.parseInt(o2.id);
            }
        });
    }

    private String readFile(File problemDir, String child) throws IOException {
        return readFileToString(new File(problemDir, child));
    }

    public Problem find(String id) throws IOException {
        List<Problem> problems = getAll();
        for (Problem problem : problems) {
            if (StringUtils.equals(problem.id, id)) {
                return problem;
            }
        }
        return null;
    }

    public void add(Problem problem) throws IOException {
        if (StringUtils.isNotBlank(problem.id)) {
            throw new IllegalArgumentException("problem.id should be empty when adding: " + problem.id);
        }
        problem.id = "" + nextId();
        recreateProblem(problem);
    }

    private void recreateProblem(Problem problem) throws IOException {
        File problemDir = new File(root, problem.id);
        if (problemDir.exists()) {
            FileUtils.deleteDirectory(problemDir);
        }
        problemDir.mkdirs();

        FileUtils.writeStringToFile(new File(problemDir, PROBLEM_FILENAME), problem.content, "UTF-8");
        FileUtils.writeStringToFile(new File(problemDir, VALIDATION_FILENAME), problem.validation, "UTF-8");
    }

    public void update(Problem problem) throws IOException {
        if (StringUtils.isBlank(problem.id)) {
            throw new IllegalArgumentException("problem.id should not be blank when updating");
        }
        recreateProblem(problem);
    }

    private int nextId() throws IOException {
        int maxId = 0;
        List<Problem> all = getAll();
        for (Problem problem : all) {
            int id = Integer.parseInt(problem.id);
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }

    public void deleteProblem(String id) throws IOException {
        File problemDir = new File(root, id);
        FileUtils.deleteDirectory(problemDir);
    }
}

