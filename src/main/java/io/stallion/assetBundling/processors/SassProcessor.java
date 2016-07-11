package io.stallion.assetBundling.processors;


import io.stallion.assetBundling.AssetFile;


public class SassProcessor extends Processor {

    @Override
    public void process(AssetFile af) {

    }

    @Override
    public String getName() {
        return "sass";
    }

    @Override
    public String[] getExtensions() {
        return new String[] {"sass"};
    }
}
