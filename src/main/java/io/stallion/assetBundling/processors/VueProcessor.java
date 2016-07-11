package io.stallion.assetBundling.processors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stallion.assetBundling.AssetFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;


public class VueProcessor extends Processor {
    @Override
    public void process(AssetFile af) {


        String content = af.getRawContent();
        String css = "";

        int index = content.indexOf("<style>");
        int lastIndex = content.indexOf("</style>", index + 7);

        if (index != -1 && lastIndex > index) {
            css = content.substring(index + 7, lastIndex);
        }
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
        int linesToAdd = 0;
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

    @Override
    public String getName() {
        return "vue";
    }

    @Override
    public String[] getExtensions() {
        return new String[]{"vue"};
    }
}
