package io.stallion.assetBundling.processors;

import io.stallion.assetBundling.AssetFile;


public class DefaultProcessor extends Processor {
    @Override
    public void process(AssetFile af) {
        if (af.getRelativePath().endsWith(".css")) {
            af.setCss(af.getRawContent());
        } else if (af.getRelativePath().endsWith(".head.js")) {
            af.setHeadJavaScript(af.getRawContent());
        } else if (af.getRelativePath().endsWith(".js")) {
            af.setJavaScript(af.getRawContent());
        } else {
            throw new RuntimeException("Unknown extension: " + af.getExtension() + " on bundle file " + af.getRelativePath());
        }
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public String[] getExtensions() {
        return new String[0];
    }
}
