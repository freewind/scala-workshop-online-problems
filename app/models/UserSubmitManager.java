package models;

import coderunner.SubmitResult;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import play.Play;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserSubmitManager {

    private UsernameManager usernameManager = new UsernameManager();

    private String submitsDir = Play.configuration.getProperty("user.submits.dir");

    public List<SubmitResult> get(String username, String problemId) throws IOException {
        ArrayList<SubmitResult> results = new ArrayList<SubmitResult>();

        File problemDir = getProblemDir(username, problemId);
        File[] files = problemDir.listFiles();
        if (files == null) {
            return results;
        }

        for (File submitDir : files) {
            File resultFile = new File(submitDir, "result.json");
            SubmitResult result = new Gson().fromJson(FileUtils.readFileToString(resultFile, "UTF-8"), SubmitResult.class);
            results.add(result);
        }

        sortFromNewToOld(results);

        return results;
    }

    private void sortFromNewToOld(ArrayList<SubmitResult> results) {
        Collections.sort(results, new Comparator<SubmitResult>() {
            @Override
            public int compare(SubmitResult r1, SubmitResult r2) {
                if (r1.submitTime > r2.submitTime) {
                    return -1;
                } else if (r1.submitTime == r2.submitTime) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }

    private File getProblemDir(String username, String problemId) {
        return new File(submitsDir, username + "/" + problemId);
    }

    public List<Pair<String, Integer>> getUserAndSubmitCountForProblem(String problemId) throws IOException {
        List<Pair<String, Integer>> pairs = new ArrayList<Pair<String, Integer>>();
        List<String> usernames = usernameManager.getAll();
        for (String username : usernames) {
            List<SubmitResult> submitResults = get(username, problemId);
            if (!submitResults.isEmpty()) {
                pairs.add(new Pair<String, Integer>(username, submitResults.size()));
            }
        }
        return pairs;
    }

    public void deleteSubmit(String username, String problemId, String submitId) throws IOException {
        File problemDir = getProblemDir(username, problemId);
        File submitDir = new File(problemDir, submitId);
        FileUtils.deleteDirectory(submitDir);
    }

    public List<UserSummary> getSummaries() throws IOException {
        List<UserSummary> userSummaries = new ArrayList<UserSummary>();
        List<String> usernames = usernameManager.getAll();
        for (String username : usernames) {
            UserSummary userSummary = new UserSummary();
            userSummary.username = username;
            userSummary.solvedProblems = getSolvedProblems(username);
            userSummary.lastSubmitTime = getLastSubmitTime(username);
            userSummaries.add(userSummary);
        }
        return userSummaries;
    }

    private long getLastSubmitTime(String username) throws IOException {
        long lastSubmitTime = 0;
        File userDir = new File(submitsDir, username);
        if (!userDir.exists()) {
            return lastSubmitTime;
        }
        String[] problemDirs = userDir.list();
        if (problemDirs == null) {
            return lastSubmitTime;
        }

        for (String problemId : problemDirs) {
            List<SubmitResult> submitResults = get(username, problemId);
            for (SubmitResult result : submitResults) {
                if (result.submitTime > lastSubmitTime) {
                    lastSubmitTime = result.submitTime;
                }
            }
        }
        return lastSubmitTime;
    }

    private List<String> getSolvedProblems(String username) throws IOException {
        List<String> ids = new ArrayList<String>();
        File userDir = new File(submitsDir, username);
        if (!userDir.exists()) {
            return ids;
        }

        String[] problemDirs = userDir.list();
        if (problemDirs == null) {
            return ids;
        }

        for (String problemId : problemDirs) {
            List<SubmitResult> submitResults = get(username, problemId);
            for (SubmitResult result : submitResults) {
                if (result.isPassed() && !ids.contains(result.problemId)) {
                    ids.add(result.problemId);
                }
            }
        }

        Collections.sort(ids, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.parseInt(o1) - Integer.parseInt(o2);
            }
        });
        return ids;
    }
}


