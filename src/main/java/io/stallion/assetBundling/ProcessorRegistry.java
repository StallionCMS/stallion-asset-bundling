package io.stallion.assetBundling;

import io.stallion.assetBundling.processors.*;

import java.util.HashMap;
import java.util.Map;


public class ProcessorRegistry {
    private static ProcessorRegistry _instance;

    public static ProcessorRegistry instance() {
        if (_instance == null) {
            _instance = new ProcessorRegistry();
        }
        return _instance;
    }

    public static void reset() {
        _instance = new ProcessorRegistry();
    }

    ProcessorRegistry() {
        register(new CommandProcessor());
        register(new DefaultProcessor());
        register(new ScssProcessor());
        register(new VueProcessor());
    }


    public final Processor DEFAULT = new DefaultProcessor();
    private Map<String, Processor> processorByExtension = new HashMap<String, Processor>();
    private Map<String, Processor> processorByName = new HashMap<String, Processor>();


    public void register(Processor processor) {
        for(String ext: processor.getExtensions()) {
            processorByExtension.put(ext, processor);
        }
        processorByName.put(processor.getName(), processor);
    }

    public Processor getProcessorByName(String name) {
        return processorByName.get(name);
    }

    public boolean processorExistsForExtension(String extension) {
        boolean result = processorByExtension.containsKey(extension);
        return result;
    }

    public Processor getProcessorByExtension(String extension) {

        Processor p = processorByExtension.get(extension);
        return p;
    }
}
