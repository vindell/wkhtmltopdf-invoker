package com.github.jhonnymertz.Calibre.wrapper;

import com.github.jhonnymertz.Calibre.wrapper.configurations.WrapperConfig;
import com.github.jhonnymertz.Calibre.wrapper.page.Page;
import com.github.jhonnymertz.Calibre.wrapper.page.PageType;
import com.github.jhonnymertz.Calibre.wrapper.params.Param;
import com.github.jhonnymertz.Calibre.wrapper.params.Params;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Pdf file
 */
public class Pdf {

    private static final String STDINOUT = "-";

    private final WrapperConfig wrapperConfig;

    private final Params params;

    private final List<Page> pages;

    private boolean hasToc = false;

    public Pdf() {
        this(new WrapperConfig());
    }

    public Pdf(WrapperConfig wrapperConfig) {
        this.wrapperConfig = wrapperConfig;
        this.params = new Params();
        this.pages = new ArrayList<Page>();
    }

    /**
     * Add a page to the pdf
     *
     * @deprecated Use the specific type method to a better semantic
     */
    @Deprecated
    public void addPage(String source, PageType type) {
        this.pages.add(new Page(source, type));
    }

    /**
     * Add a page from an URL to the pdf
     */
    public void addPageFromUrl(String source) {
        this.pages.add(new Page(source, PageType.url));
    }

    /**
     * Add a page from a HTML-based string to the pdf
     */
    public void addPageFromString(String source) {
        this.pages.add(new Page(source, PageType.htmlAsString));
    }

    /**
     * Add a page from a file to the pdf
     */
    public void addPageFromFile(String source) {
        this.pages.add(new Page(source, PageType.file));
    }

    public void addToc() {
        this.hasToc = true;
    }

    public void addParam(Param param, Param... params) {
        this.params.add(param, params);
    }

    public File saveAs(String path) throws IOException, InterruptedException {
        File file = new File(path);
        FileUtils.writeByteArrayToFile(file, getPDF());
        return file;
    }

    public byte[] getPDF() throws IOException, InterruptedException {

        try {
            Process process = Runtime.getRuntime().exec(getCommandAsArray());

            byte[] inputBytes = IOUtils.toByteArray(process.getInputStream());
            byte[] errorBytes = IOUtils.toByteArray(process.getErrorStream());

            process.waitFor();

            if (process.exitValue() != 0) {
                throw new RuntimeException("Process (" + getCommand() + ") exited with status code " + process.exitValue() + ":\n" + new String(errorBytes));
            }

            return inputBytes;
        } finally {
            cleanTempFiles();
        }
    }

    private String[] getCommandAsArray() throws IOException {
        List<String> commandLine = new ArrayList<String>();

        if (wrapperConfig.isXvfbEnabled()){
            commandLine.addAll(wrapperConfig.getXvfbConfig().getCommandLine());
        } else {
        	commandLine.add(wrapperConfig.getCalibreCommand());
        }
        commandLine.addAll(params.getParamsAsStringList());

        if (hasToc)
            commandLine.add("toc");

        for (Page page : pages) {
            if (page.getType().equals(PageType.htmlAsString)) {

                File temp = File.createTempFile("java-Calibre-wrapper" + UUID.randomUUID().toString(), ".html");
                FileUtils.writeStringToFile(temp, page.getSource(), "UTF-8");

                page.setSource(temp.getAbsolutePath());
            }

            commandLine.add(page.getSource());
        }
        commandLine.add(STDINOUT);
        return commandLine.toArray(new String[commandLine.size()]);
    }

    private void cleanTempFiles() {
        for (Page page : pages) {
            if (page.getType().equals(PageType.htmlAsString)) {
                new File(page.getSource()).delete();
            }
        }
    }

    public String getCommand() throws IOException {
        return StringUtils.join(getCommandAsArray(), " ");
    }

}
