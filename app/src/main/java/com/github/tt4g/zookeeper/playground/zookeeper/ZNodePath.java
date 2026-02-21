package com.github.tt4g.zookeeper.playground.zookeeper;

import org.apache.zookeeper.common.PathUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public final class ZNodePath {
    private static final String ROOT_PATH = "/";

    private static final String PATH_SEPARATOR = "/";

    /**
     * Parent ZNode path.
     */
    private final String parentPath;

    /**
     * ZNode path.
     */
    private final String path;

    /**
     * ZNode name.
     */
    private final String name;

    /**
     * Root ZNode path.
     */
    private ZNodePath() {
        this.parentPath = "";
        this.path = ROOT_PATH;
        this.name = "";
    }

    /**
     * Concat ZNode path.
     *
     * @param parent Parent {@link ZNodePath}.
     * @param name   ZNode name.
     */
    private ZNodePath(ZNodePath parent, String name) {
        if (name.contains(PATH_SEPARATOR)) {
            throw new IllegalArgumentException(
                "ZNode name cannot contains path separator \"" + PATH_SEPARATOR + "\""
            );
        }
        if (name.contains(".")) {
            throw new IllegalArgumentException(
                "ZNode name cannot contains dots \".\""
            );
        }

        var parentPath = parent.getPath();
        var path =
            switch (parentPath) {
                // `parent` is root ZNodePath.
                case ROOT_PATH -> ROOT_PATH + name;
                default -> parentPath + PATH_SEPARATOR + name;
            };
        // Zookeeper Official validation.
        PathUtils.validatePath(path);

        this.parentPath = parentPath;
        this.path = path;
        this.name = name;
    }

    public static ZNodePath root() {
        return new ZNodePath();
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Join ZNode path.
     *
     * @param name ZNode name.
     * @return Child ZNode path.
     */
    public ZNodePath join(String name) {
        return new ZNodePath(this, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ZNodePath zNodePath = (ZNodePath) o;
        return Objects.equals(parentPath, zNodePath.parentPath) && Objects.equals(path, zNodePath.path) && Objects.equals(name, zNodePath.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentPath, path, name);
    }

    @Override
    public String toString() {
        return "ZNodePath(" + this.path + ")";
    }
}
