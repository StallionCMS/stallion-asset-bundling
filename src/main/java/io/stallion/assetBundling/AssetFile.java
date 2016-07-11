package io.stallion.assetBundling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import io.stallion.assetBundling.processors.Processor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AssetFile {
    private File file;
    private Processor processor;
    private String extension;
    private String relativePath;

    private String rawContent;
    private String css;
    private String headJavaScript;
    private String javaScript;

    private long hydratedAt = 0;

    private Map<String, String> params = new HashMap<String, String>();

    public AssetFile(File file) {
        this.file = file;
    }

    public AssetFile(String fullPath, String relativePath, Map<String, String> params) {
        file = new File(fullPath);
        extension = FilenameUtils.getExtension(file.getAbsolutePath());
        this.params = params;
        this.relativePath = relativePath;
        if (params.containsKey("processor")) {
            String processorName = params.get("processor");
            if (!"".equals(processorName)) {
                processor = ProcessorRegistry.instance().getProcessorByName(processorName);
            }
        } else if (ProcessorRegistry.instance().processorExistsForExtension(extension)) {
            processor = ProcessorRegistry.instance().getProcessorByExtension(extension);
        } else {
            processor = ProcessorRegistry.instance().DEFAULT;
        }
    }

    public void writeProcessedContents() {
        hydrateIfNeeded(true);
        try {
            if (css.length() > 0) {
                File cssFile = new File(file.getAbsoluteFile() + ".css");
                FileUtils.writeStringToFile(cssFile, css, "utf-8");
            }
            if (javaScript.length() > 0) {
                File jsFile = new File(file.getAbsoluteFile() + ".js");
                FileUtils.writeStringToFile(jsFile, javaScript, "utf-8");

            }
            if (javaScript.length() > 0) {
                File headJsFile = new File(file.getAbsoluteFile() + ".head.js");
                FileUtils.writeStringToFile(headJsFile, headJavaScript, "utf-8");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void hydrateIfNeeded(boolean autoReload) {
        if (this.rawContent == null) {
            synchronized (this) {
                if (this.rawContent != null) {
                    return;
                }
                hydrate();
                return;
            }
        }
        if (autoReload && hydratedAt < file.lastModified()) {
            synchronized (this) {
                if (hydratedAt < file.lastModified()) {
                    hydrate();
                }
            }
        }

    }

    public void hydrate() {
        try {
            this.rawContent = FileUtils.readFileToString(file, "utf-8");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        this.css = "";
        this.javaScript = "";
        this.headJavaScript = "";
        processor.process(this);
        this.hydratedAt = new Date().getTime();
    }



    public AssetFile deepCopy() {
        AssetFile af = new AssetFile(file.getAbsolutePath(), getRelativePath(), getParams());
        return af;
    }

    public Processor getProcessor() {
        return processor;
    }

    public AssetFile setProcessor(Processor processor) {
        this.processor = processor;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getExtension() {
        return extension;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public String getCss() {
        return css;
    }

    public AssetFile setCss(String css) {
        this.css = css;
        return this;
    }

    public String getHeadJavaScript() {
        return headJavaScript;
    }

    public AssetFile setHeadJavaScript(String headJavaScript) {
        this.headJavaScript = headJavaScript;
        return this;
    }

    public String getJavaScript() {
        return javaScript;
    }

    public AssetFile setJavaScript(String javaScript) {
        this.javaScript = javaScript;
        return this;
    }

    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }
}
