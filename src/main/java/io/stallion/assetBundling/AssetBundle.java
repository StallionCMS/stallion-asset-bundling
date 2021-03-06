package io.stallion.assetBundling;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.*;

public class AssetBundle {
    private File file;
    private long loadedAt = 0;
    private String hash = null;
    private List<AssetFile> files = null;
    private Map<String, AssetFile> fileByPath = new HashMap<String, AssetFile>();
    private String content;
    private String md5 = null;
    private Map<String, Set<String>> globbedFoldersFileNames;

    public AssetBundle(File file) {
        this.file = file;
    }

    public void hydrateFilesIfNeeded(boolean autoReload) {
        hydrateFilesIfNeeded(autoReload, autoReload);
    }

    public void hydrateFilesIfNeeded(boolean autoReload, boolean checkGlobbed) {
        if (files == null) {
            hydrateFiles();
        } else if (autoReload) {
            if (file.lastModified() > loadedAt)  {
                hydrateFiles();
            } else if (checkGlobbed) {
                boolean newFilesFound = false;
                for (Map.Entry<String, Set<String>> entry: globbedFoldersFileNames.entrySet()) {
                    boolean hasNew = globbedFolderHasChangedFiles(entry.getKey(), entry.getValue());
                    if (hasNew == true) {
                        newFilesFound = true;
                        break;
                    }
                }
                if (newFilesFound) {
                    hydrateFiles();
                }
            }
        }
    }

    public void hydrateFiles() {
        long localLoaded = new Date().getTime();
        synchronized (this) {
            if (loadedAt >= localLoaded) {
                return;
            }
            doHydrateFiles();
        }
    }
    public void doHydrateFiles() {
        String directory = file.getParent();
        List<AssetFile> files = new ArrayList<AssetFile>();
        globbedFoldersFileNames = new HashMap<String, Set<String>>();
        content = "";
        try {
            content = FileUtils.readFileToString(file, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> lines = Arrays.asList(content.split("\n"));
        for(String line: lines) {
            line = line.trim();
            if ("".equals(line.trim())) {
                continue;
            }
            int i = line.indexOf("?");
            String query = "";
            if (i > -1) {
                query = line.substring(i + 1);
                line = line.substring(0, i);
            }
            Map<String, String> params = splitQuery(query);
            String[] parts = line.split("\\|");
            String path = parts[0];
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (path.contains("*")) {
                addFilesForDirectoryGlob(files, directory + path, directory);
            } else if (fileByPath.containsKey(path)) {
                files.add(fileByPath.get(path));
            } else{
                AssetFile af = new AssetFile(directory + path, path, params);
                files.add(af);
                fileByPath.put(path, af);
            }
        }


        this.loadedAt = new Date().getTime();
        this.files = files;
        this.md5 = null;
    }

    private boolean globbedFolderHasChangedFiles(String directory, Set<String> existingFiles) {
        Set<String> existingCopy = new HashSet<String>(existingFiles);
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(directory), null, true);
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            if (!existingFiles.contains(file.getName())) {
                return true;
            } else {
                existingCopy.remove(file.getName());
            }
        }
        if (existingCopy.size() > 0) {
            return true;
        }

        return false;
    }

    private void addFilesForDirectoryGlob(List<AssetFile> files, String glob, String bundleDirectory) {
        String[] parts = StringUtils.split(glob, "*", 2);
        String directory = parts[0];
        String end = parts[1];
        String pre = "";
        if (!directory.endsWith("/")) {
            pre = FilenameUtils.getName(directory);
            directory = new File(directory).getParent();
        }

        Set<String> foundFiles = new HashSet<String>();
        globbedFoldersFileNames.put(directory, foundFiles);


        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(directory), null, true);
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            foundFiles.add(file.getName());
            if (end.length() > 0 && !file.getAbsolutePath().endsWith(end)) {
                continue;
            }
            if (pre.length() > 0 && !file.getName().startsWith(pre)) {
                continue;
            }
            String name = file.getName();
            if (name.startsWith(".") || name.startsWith("~") || name.startsWith("#") || name.contains("flycheck.")) {
                continue;
            }
            // Hack to prevent including temporary generated files created by a previous run.
            // TODO: make this less hacky
            if (name.contains(".css.css")) {
                continue;
            }
            if (name.contains(".js.js")) {
                continue;
            }
            if (name.contains(".js.head.js")) {
                continue;
            }
            String relativePath = file.getAbsolutePath().replace(bundleDirectory, "");
            AssetFile af = fileByPath.getOrDefault(relativePath, null);
            if (af == null) {
                af = new AssetFile(file.getAbsolutePath(), relativePath, new HashMap<String, String>());
            }
            files.add(af);
            fileByPath.put(relativePath, af);
        }

    }

    private Map<String, String> splitQuery(String query)  {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > -1) {
                try {
                    query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                query_pairs.put(pair, "");
            }
        }
        return query_pairs;
    }



    public void writeToDisk() {
        try {
            if (files == null) {
                hydrateFiles();
            }
            for (AssetFile file: files) {
                file.hydrateIfNeeded(false);
            }
            writeCss();
            writeHeadJavaScript();
            writeJavaScript();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeJavaScript() throws IOException {
        File out = new File(file.getAbsoluteFile() + ".js");
        String source = getConcatenatedJavaScript();
        if (!"".equals(source.trim())) {
            FileUtils.write(out, source, "UTF-8");
        }
    }

    protected void writeHeadJavaScript() throws IOException {
        File out = new File(file.getAbsoluteFile() + ".head.js");
        String source = getConcatenatedHeadJavaScript();
        if (!"".equals(source.trim())) {
            FileUtils.write(out, source, "UTF-8");
        }

    }

    protected void writeCss() throws IOException {
        File out = new File(file.getAbsoluteFile() + ".css");
        String source = getConcatenatedCss();
        if (!"".equals(source.trim())) {
            FileUtils.write(out, source, "UTF-8");
        }

    }

    public void hydrateAllFilesIfNeeded(boolean autoReload) {
        hydrateFilesIfNeeded(autoReload);
        for (AssetFile file: files) {
            file.hydrateIfNeeded(autoReload);
        }
    }

    public String getConcatenatedJavaScript() {
        StringBuilder builder = new StringBuilder();
        for (AssetFile f: files) {
            if (!"".equals(f.getJavaScript())) {
                builder.append("\n// from " + f.getRelativePath() + "\n" + f.getJavaScript());
            }
        }
        return builder.toString();
    }

    public String getConcatenatedHeadJavaScript() {
        StringBuilder builder = new StringBuilder();
        for (AssetFile f: files) {
            if (!"".equals(f.getHeadJavaScript())) {
                builder.append("\n// from " + f.getRelativePath() + "\n" + f.getHeadJavaScript());
            }
        }
        return builder.toString();
    }

    public String getConcatenatedCss() {
        StringBuilder builder = new StringBuilder();
        for (AssetFile f: files) {
            if (!"".equals(f.getCss())) {
                builder.append("\n /* from " + f.getRelativePath() + "*/\n" + f.getCss());
            }
        }
        return builder.toString();
    }

    public File getBundleFile() {
        return file;
    }

    public List<AssetFile> getFiles() {
        return files;
    }

    public String renderPath(String path, boolean autoReload) throws NotFoundException {
        hydrateFilesIfNeeded(autoReload);
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith(".head.js")) {
            path = path.substring(0, path.length() - 8);
            AssetFile af = fileByPath.get(path);
            if (af == null) {
                throw new NotFoundException("No AssetFile for path " + path + " in bundle " + this.file.getAbsolutePath());
            }
            af.hydrateIfNeeded(autoReload);
            return af.getHeadJavaScript();
        } else if (path.endsWith(".js")) {
            path = path.substring(0, path.length() - 3);
            AssetFile af = fileByPath.get(path);
            if (af == null) {
                throw new NotFoundException("No AssetFile for path " + path + " in bundle " + this.file.getAbsolutePath());
            }
            af.hydrateIfNeeded(autoReload);
            return af.getJavaScript();
        } else if (path.endsWith(".css")) {
            path = path.substring(0, path.length() - 4);
            AssetFile af = fileByPath.get(path);
            if (af == null) {
                throw new NotFoundException("No AssetFile for path " + path + " in bundle " + this.file.getAbsolutePath());
            }
            af.hydrateIfNeeded(autoReload);
            return af.getCss();
        } else {
            throw new RuntimeException("Unknown extension for path " + path);
        }
    }

    public List<String> listHeadJavaScriptPaths(boolean autoReload) {
        hydrateFilesIfNeeded(autoReload);
        List<String> paths = new ArrayList<String>();
        for(AssetFile af: files) {
            af.hydrateIfNeeded(autoReload);
            if (af.getHeadJavaScript().length() > 0) {
                paths.add(af.getRelativePath());
            }
        }
        return paths;
    }


    public List<String> listCssPaths(boolean autoReload) {
        hydrateFilesIfNeeded(autoReload);
        List<String> paths = new ArrayList<String>();
        for(AssetFile af: files) {
            af.hydrateIfNeeded(autoReload);
            if (af.getCss().length() > 0) {
                paths.add(af.getRelativePath());
            }
        }
        return paths;
    }

    public List<String> listJavascriptPaths(boolean autoReload) {
        hydrateFilesIfNeeded(autoReload);
        List<String> paths = new ArrayList<String>();
        for(AssetFile af: files) {
            af.hydrateIfNeeded(autoReload);
            if (af.getJavaScript().length() > 0) {
                paths.add(af.getRelativePath());
            }
        }
        return paths;
    }

    public String getHash() {
        if (md5 == null) {
            MessageDigest md5Digest = DigestUtils.getMd5Digest();
            md5Digest.update(this.content.getBytes());
            for (AssetFile af: files) {
                md5Digest.update(af.getRawContent().getBytes());
            }
            md5 = md5Digest.toString();
        }
        return md5;
    }
}
