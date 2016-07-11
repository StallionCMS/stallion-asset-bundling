package io.stallion.assetBundling;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BundleRegistry {
    private static BundleRegistry _instance;
    public static BundleRegistry instance() {
        if (_instance == null) {
            _instance = new BundleRegistry();
        }
        return _instance;
    }
    public static void reset() {
        _instance = new BundleRegistry();
    }

    private Map<String, AssetBundle> bundleMap = new HashMap<String, AssetBundle>();

    public AssetBundle getByName(String name) {
        if (!bundleMap.containsKey(name)) {
            throw new NotFoundException("No bundle found in the registry with name: " + name);
        }
        return bundleMap.get(name);
    }

    public AssetBundle getByPath(String absolutePath) {
        if (!bundleMap.containsKey(absolutePath)) {
            synchronized (this) {
                if (!bundleMap.containsKey(absolutePath)) {
                    bundleMap.put(absolutePath, new AssetBundle(new File(absolutePath)));
                }
            }
        }
        return bundleMap.get(absolutePath);
    }

    public AssetBundle getByResourcePath(String path, Class cls) {
        URL url = cls.getResource(path);
        if (url == null) {
                throw new NotFoundException("Could not find resourece for bundle " + path + " in class loader for " + cls.getCanonicalName());
        }
        try {
            return getByPath(url.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
