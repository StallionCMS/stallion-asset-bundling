package io.stallion.assetBundling.processors;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;
import org.w3c.css.sac.InputSource;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ScssStringResolver implements ScssStylesheetResolver {

    private String uri;

    Map<String, String> sources = new HashMap<String, String>();

    public ScssStringResolver(String originalFile, Map<String, String> sources) {
        this.sources = sources;
        this.uri = originalFile;
    }

    public ScssStringResolver(String originalFile, String name, String source) {
        this.sources = new HashMap<String, String>();
        this.sources.put(name, source);
        this.uri = originalFile;
    }


    public InputSource resolve(ScssStylesheet scssStylesheet, String identifier) {
        String source = sources.get(identifier);
        if (source == null) {
            String uriMaybe = new File(uri).getParent() + "/" + identifier + ".scss";
            File f = new File(uriMaybe);
            if (f.exists()) {
                try {
                    InputSource is = new InputSource(new FileReader(f));
                    is.setURI(uriMaybe);
                    return is;
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        } else {
            InputSource is = new InputSource(new StringReader(source));
            is.setURI(uri);
            return is;
        }
    }
}
