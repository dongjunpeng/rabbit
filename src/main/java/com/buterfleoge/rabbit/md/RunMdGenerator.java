package com.buterfleoge.rabbit.md;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;

/**
 * @author xiezhenzong
 *
 */

public class RunMdGenerator {
    public static void main(String[] args) throws Exception {
        MdGenerator md = new MdGenerator();
        Thread thread = new Thread(md);
        thread.start();
    }
}

class MdGenerator implements Runnable {

    public void run() {
        while (true) {
            try {

                String path = "D:/workspace/md/";
                String[] fileList = { "p00", "p01", "p02", "p03", "p04", "p05" };

                for (String fileName : fileList) {
                    File file = new File(path + "source/" + fileName + ".txt");
                    char[] text = FileUtils.readFileToString(file).toCharArray();
                    int lineNo = 1, i = 0, n = text.length;
                    String className = null, spanContent = null;
                    StringBuilder mdBuilder = new StringBuilder(text.length + 2048);
                    while (i < n) {
                        char c = text[i++];
                        if (c == '\n') {
                            lineNo++;
                        }
                        if (c == '@') {
                            Action action = new DollarAction(lineNo);
                            i = action.handle(text, i, n);
                            className = action.getValue();
                        } else if (c == '{') {
                            Action action = new LeftBraceAction(lineNo);
                            i = action.handle(text, i, n);
                            spanContent = action.getValue();
                        } else if (c == '}') {
                            mdBuilder.append("<span class=\"").append(className).append("\">");
                            mdBuilder.append(spanContent);
                            mdBuilder.append("</span>");
                        } else {
                            mdBuilder.append(c);
                        }
                    }
                    file = new File(path + fileName + ".md");
                    FileUtils.writeStringToFile(file, mdBuilder.toString());
                }
                System.out.println(new Date(System.currentTimeMillis()));
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}

abstract class Action {

    private int lineNo;
    private char tag;
    private StringBuilder value = new StringBuilder();

    public Action(int lineNo, char tag) {
        this.lineNo = lineNo;
        this.tag = tag;
    }

    public final int handle(char[] text, int i, int n) {
        int j = i;
        while (true) {
            if (j >= n) {
                error();
            }
            if (end(text[j])) {
                if (i == j) {
                    error();
                }
                return j;
            }
            value.append(text[j]);
            j++;
        }
    }

    public final String getValue() {
        return value.toString();
    }

    protected final void error() {
        throw new IllegalStateException("lineNo: " + lineNo + ", tag: " + tag);
    }

    protected abstract boolean end(char c);

}

class DollarAction extends Action {

    public DollarAction(int lineNo) {
        super(lineNo, '@');
    }

    @Override
    protected boolean end(char c) {
        if (c == '{') {
            return true;
        } else {
            if (c == '@' || c == '}') {
                error();
            }
            return false;
        }
    }
}

class LeftBraceAction extends Action {

    public LeftBraceAction(int lineNo) {
        super(lineNo, '{');
    }

    @Override
    protected boolean end(char c) {
        if (c == '}') {
            return true;
        } else {
            if (c == '@' || c == '{') {
                error();
            }
            return false;
        }
    }
}
