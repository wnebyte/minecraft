package com.github.wnebyte.minecraft.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UriBuilder {

    public static String normalize(String s) {
        if (SEP.equals("\\")) {
            // windows
            return s.replace("/", "\\");
        } else if (SEP.equals("/")) {
            // linux
            return s.replace("\\", "/");
        } else {
            return s;
        }
    }

    private static final String SEP = File.separator;

    private String authority;

    private final List<String> paths;

    public UriBuilder() {
        this(null);
    }

    public UriBuilder(String authority) {
        this.authority = authority;
        this.paths = new ArrayList<>();
    }

    public UriBuilder setAuthority(String authority) {
        this.authority = authority;
        return this;
    }

    public UriBuilder appendPath(String path) {
        paths.add(path);
        return this;
    }

    public UriBuilder path(String path) {
        paths.clear();
        paths.add(path);
        return this;
    }

    public String build() {
        if (authority == null) {
            throw new IllegalArgumentException("URI must consist of an authority");
        }
        StringBuilder s = new StringBuilder();
        s.append(authority);
        if (!paths.isEmpty()) {
            s.append(SEP).append(String.join(SEP, paths));
        }
        return normalize(s.toString());
    }

    public URI toURI() throws URISyntaxException {
        URI uri = new URI(build());
        return uri;
    }

    public File toFile() {
        File file = new File(build());
        return file;
    }

    public Path toPath() {
        return Paths.get(build());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
