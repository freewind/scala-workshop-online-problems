package coderunner;

import models.Problem;
import models.ProblemManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class AnswerCodeGenerator {

    private static final String answerTemplate = getAnswerTemplate();

    private ProblemManager problemManager = new ProblemManager();
    private String problemId;
    private String userCode;

    private static String getAnswerTemplate() {
        try {
            File templateFile = Play.getFile("conf/answer_template.txt");
            return FileUtils.readFileToString(templateFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AnswerCodeGenerator(String problemId, String userCode) {
        this.problemId = problemId;
        this.userCode = userCode;
    }

    public String generate() throws IOException {
        String answerCode = replaceUserCode(answerTemplate);
        return replaceValidationCode(answerCode);
    }

    private String replaceValidationCode(String code) throws IOException {
        Problem problem = problemManager.find(problemId);
        String validationCode = createValidationCode(problem);
        return StringUtils.replace(code, "#####___validation_rules___#####", validationCode);
    }

    private String createValidationCode(Problem problem) throws IOException {
        List<String> result = new ArrayList<String>();
        for (String line : IOUtils.readLines(new StringReader(problem.validation))) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] items = StringUtils.splitByWholeSeparator(line, "===");
            String test = items[0];
            String expected = items[1];
            result.add(String.format("validate(%s, %s, %s)", test, expected, "\"\"\"" + test + "\"\"\""));
        }
        return StringUtils.join(result, "\n");
    }

    private String replaceUserCode(String code) {
        return StringUtils.replace(code, "#####___user_code___#####", userCode);
    }

}
