package io.stallion.assetBundling.processors;


import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import io.stallion.assetBundling.AssetFile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;


public class ScssProcessor extends Processor {

    @Override
    public void process(AssetFile af) {
        try {
            SCSSErrorHandler errorHandler = new SCSSErrorHandler();
            errorHandler.setWarningsAreErrors(true);
            ScssStylesheet scss = ScssStylesheet.get(af.getAbsolutePath(), null,
                    new SCSSDocumentHandlerImpl(), errorHandler);
            scss.compile(ScssContext.UrlMode.MIXED);
            if (errorHandler.isErrorsDetected()) {
                throw new RuntimeException("Fatal errors while compiling scss for file " + af.getAbsolutePath() + ": " + errorHandler.toString());
            }

            StringWriter writer = new StringWriter();
            scss.write(writer, true);
            writer.close();
            af.setCss(writer.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getName() {
        return "scss";
    }

    @Override
    public String[] getExtensions() {
        return new String[] {"scss"};
    }
}
