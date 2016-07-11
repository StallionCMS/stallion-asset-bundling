package io.stallion.assetBundling.processors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import io.stallion.assetBundling.AssetFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.StringWriter;
import java.net.URI;


public class VueProcessor extends Processor {
    @Override
    public void process(AssetFile af) {


        String content = af.getRawContent();
        String css = "";

        int index = content.indexOf("<style");
        int endTag = content.indexOf(">", index+1);

        int lastIndex = content.indexOf("</style>", index + 7);

        // Add lines so that the end result source code lines match the line numbers in the original file,
        // this is useful for debugging
        int linesToAdd = 0;
        if (index != -1 && lastIndex > index) {
            String before = content.substring(0, index);
            linesToAdd = StringUtils.countMatches(before, "\n") - 1;
            if (linesToAdd < 0) {
                linesToAdd = 0;
            }
        }

        if (index != -1 && endTag > index && lastIndex > endTag) {
            String tag = content.substring(index, endTag + 1);
            css = content.substring(endTag + 1, lastIndex);
            if (tag.contains("lang=")) {
                if (tag.contains("lang=scss") || tag.contains("lang='scss'") || tag.contains("lang=\"scss\"")) {
                    css = toScssToCss(af, css);
                }
            }
        }
        css = StringUtils.repeat("\n", linesToAdd) + css;
        af.setCss(css);


        index = content.indexOf("<template>");
        lastIndex = content.lastIndexOf("</template>");
        String template = "";

        if (index != -1 && lastIndex > index) {
            template = content.substring(index + 10, lastIndex);
        }

        index = content.indexOf("<script>");
        lastIndex = content.lastIndexOf("</script>");
        String script = "";
        linesToAdd = 0;
        if (index != -1 && lastIndex > index) {
            script = content.substring(index + 8, lastIndex);
            String before = content.substring(0, index);
            linesToAdd = StringUtils.countMatches(before, "\n") - 1;
            if (linesToAdd < 0) {
                linesToAdd = 0;
            }
        }

        String tag = FilenameUtils.removeExtension(FilenameUtils.getName(af.getRelativePath()));
        String templateJson = "";

        ObjectMapper mapper = new ObjectMapper();


        try {
            templateJson = mapper.writeValueAsString(template.trim());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        script = "(function() {" +
                "var module = {exports: {}};\n" +
                StringUtils.repeat("\n", linesToAdd) +
                script + "\n";
        script += "module.exports.template = " + templateJson + ";\n";
        script += "window.vueComponents = window.vueComponents || {};\n";
        script += "window.vueComponents['"+ tag + "'] = Vue.component('" + tag + "', module.exports);\n";

        script += "})();";
        af.setJavaScript(script);
    }

    public String toScssToCss(AssetFile af, String scssContent) {
        try {
            SCSSErrorHandler errorHandler = new SCSSErrorHandler();
            errorHandler.setWarningsAreErrors(true);
            ScssStylesheet sheet = new ScssStylesheet();
            sheet.addResolver(new ScssStringResolver(af.getAbsolutePath(), "vue", scssContent));
            ScssStylesheet scss = ScssStylesheet.get("vue", sheet,
                    new SCSSDocumentHandlerImpl(), errorHandler);
            scss.compile(ScssContext.UrlMode.MIXED);
            StringWriter writer = new StringWriter();
            scss.write(writer, true);
            writer.close();
            if (errorHandler.isErrorsDetected()) {
                throw new RuntimeException("Fatal errors while compiling scss for file " + af.getAbsolutePath() + ": " + errorHandler.toString());
            }
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "vue";
    }

    @Override
    public String[] getExtensions() {
        return new String[]{"vue"};
    }
}
