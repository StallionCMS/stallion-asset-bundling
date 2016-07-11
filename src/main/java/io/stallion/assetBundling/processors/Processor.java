package io.stallion.assetBundling.processors;

import io.stallion.assetBundling.AssetFile;

import java.util.List;
import java.util.Map;

public abstract class Processor {
    public AssetFile submitForProcessing(AssetFile original) {
        AssetFile af = original.deepCopy();
        process(af);
        return af;
    }

    public abstract void process(AssetFile af);

    public abstract String getName();

    public abstract String[] getExtensions();

}
