package io.stallion.assetBundling;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class TestEndToEnd {

    @Test
    public void testConcatenateBundle() throws Exception {
        URL resourceUrl = TestEndToEnd.class.
                getResource("/assets/site.bundle");
        AssetBundle b = new AssetBundle(new File(resourceUrl.toURI()));
        b.hydrateFilesIfNeeded(false);
        b.writeToDisk();
        String path = Paths.get(resourceUrl.toURI()).toString();
        File cssFile = new File(path + ".css");
        String css = FileUtils.readFileToString(cssFile, "UTF-8");
        // Basic CSS
        assertTrue(css.contains("background-color: blue"));
        // Vue CSS
        assertTrue(css.contains(".red-thing-vue {"));
        // SCSS
        assertTrue(css.contains(".partial-stuff bold{font-size:17px}.main-column div,.main-column p{font-family:sans-serif}"));
        // Vue SCSS
        assertTrue(css.contains(".with-scss h2{font-family:comic sans,avenir;color:green;background-color:#700}"));



        assertTrue(!css.contains("console.log"));
        assertTrue(!css.contains("background-color: yellow;"));

        File jsFile = new File(path + ".js");
        String js = FileUtils.readFileToString(jsFile, "UTF-8");

        assertTrue(js.contains("console.log('this is the site.js file');"));
        assertTrue(js.contains("console.log('the red thing is ready');"));
        assertTrue(!js.contains("console.log(\"I am in the head.\")"));
        assertTrue(js.contains("console.log('first file in the lib')"));
        assertTrue(js.contains("console.log('second file in the lib');"));
        assertTrue(js.contains("module.exports.template = \"<div class=\\\"red-thing-vue\\\">\\n        <button></button>\\n    </div>\";"));

        File jsHeadFile = new File(path + ".head.js");
        String jsHead = FileUtils.readFileToString(jsHeadFile, "UTF-8");
        assertTrue(jsHead.contains("console.log(\"I am in the head.\")"));
        assertTrue(!jsHead.contains("console.log('this is the site.js file');"));

    }
}
