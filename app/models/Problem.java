package models;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class Problem {

    public String id;
    public String content;
    public String validation;

    public String getTitle() {
        try {
            List<String> lines = IOUtils.readLines(new StringReader(content.trim()));
            return lines.isEmpty() ? content : lines.get(0);
        } catch (IOException e) {
            return content;
        }
    }

}
