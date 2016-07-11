package io.stallion.assetBundling.processors;

import io.stallion.assetBundling.AssetFile;


public class CommandProcessor extends Processor {
    @Override
    public void process(AssetFile af) {
        String commandName = af.getParams().getOrDefault("command", "");
        if ("".equals(commandName)) {
            throw new RuntimeException("CommandProcessor requires a valid query string parameter 'command' which is the name of the executable to run, for file with path " + af.getRelativePath());
        }
        String content = executeCommand(af);
        if (af.getRelativePath().endsWith(".css")) {
            af.setCss(content);
        } else if (af.getRelativePath().contains(".head.")) {
            af.setHeadJavaScript(content);
        } else if (af.getRelativePath().endsWith(".js")) {
            af.setJavaScript(content);
        } else {
            throw new RuntimeException("Unknown extension: " + af.getExtension() + " on bundle file " + af.getRelativePath());
        }
    }

    private String executeCommand(AssetFile af) {
        return "";
    }

    @Override
    public String getName() {
        return "command";
    }

    @Override
    public String[] getExtensions() {
        return new String[0];
    }
}
