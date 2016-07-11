package io.stallion.assetBundling;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class AssetHelpers {

    public static String renderDebugModeBundleFileByName(String bundleName, String filePath) {
        AssetBundle bundle = BundleRegistry.instance().getByName(bundleName);
        bundle.hydrateFilesIfNeeded(true);
        return bundle.renderPath(filePath, true);
    }

    public static String renderDebugModeBundleFileByPath(String bundlePath, String filePath) {
        AssetBundle bundle = BundleRegistry.instance().getByPath(bundlePath);
        bundle.hydrateFilesIfNeeded(true);
        return bundle.renderPath(filePath, true);
    }

    public static String renderDebugModeBundleFileByUrl(URL url, String filePath) {
        String path = url.toString().replace("/target/classes/", "/src/resources");
        if (!new File(path).exists()) {
            throw new NotFoundException("No source file found for URL " + url);
        }
        return renderDebugModeBundleFileByPath(path, filePath);
    }
}
