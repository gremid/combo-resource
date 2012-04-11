package net.middell.combo;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResourceResolver implements Function<String, TextResource> {

    private final Map<String, MountPoint> mountPoints = Maps.newHashMap();

    public void mount(String root, File directory, String mappedPath, Charset charset, long maxAge) {
        Preconditions.checkArgument(directory.isDirectory(), directory + " is not a directory");
        Preconditions.checkArgument(directory.canRead(), directory + " cannot be read");
        mountPoints.put(root, new MountPoint(directory, mappedPath, charset, maxAge));
    }

    public TextResourceCombo resolve(Iterable<String> paths) {
        return new TextResourceCombo(Iterables.transform(paths, this));
    }

    @Override
    public TextResource apply(String input) {
        for (Map.Entry<String, MountPoint> mountPoint : mountPoints.entrySet()) {
            String rootPath = mountPoint.getKey();
            if (input.startsWith(rootPath)) {
                try {
                    return mountPoint.getValue().resolve(input.substring(rootPath.length()).replaceAll("^\\/+", ""));
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalArgumentException(input);

    }

    private static class MountPoint {
        private static final Joiner PATH_JOINER = Joiner.on('/');

        private final File root;
        private final Charset charset;
        private final String rootPath;
        private final long maxAge;

        private MountPoint(File root, String rootPath, Charset charset, long maxAge) {
            this.root = root;
            this.charset = charset;
            this.rootPath = Objects.firstNonNull(rootPath, "").replaceAll("\\/+$", "");
            this.maxAge = maxAge;
        }

        private TextResource resolve(String relativePath) throws IOException {
            final File resource = new File(root, relativePath);
            Preconditions.checkArgument(isDescendant(root, resource), resource + " is not contained in " + root);
            return new TextResource(resource, PATH_JOINER.join(rootPath, relativePath), charset, maxAge);
        }

        private static boolean isDescendant(File parent, File descendant) throws IOException {
            parent = parent.getCanonicalFile();
            File toCheck = descendant.getCanonicalFile().getParentFile();
            while (toCheck != null) {
                if (toCheck.equals(parent)) {
                    return true;
                }
                toCheck = toCheck.getParentFile();
            }
            return false;
        }
    }
}
