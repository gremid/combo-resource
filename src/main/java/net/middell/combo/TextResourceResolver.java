/*
 * #%L
 * Text Resource Combo Utilities
 * %%
 * Copyright (C) 2012 Gregor Middell
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.middell.combo;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Maps paths, specified e.g. in a URL, to text-based resources located in the filesystem.
 * <p/>
 * The mapping can be configured by {@link #mount(String, java.io.File, String, java.nio.charset.Charset, long) mounting}
 * a set of directories which are searched for resources matching the provided paths.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResourceResolver implements Function<String, TextResource> {

    /**
     * Maps an ordered sequence of paths to a combination of text-based resources.
     *
     * @param paths the list of paths to be mapped
     * @return a new resource combo containing the mapped resources
     * @throws IllegalArgumentException in case a path cannot be mapped, e.g. because a corresponding file does not exist,
     *                                  cannot be read or the specified path references a file not contained in the mounted
     *                                  directories
     */
    public TextResourceCombo resolve(Iterable<String> paths) throws IllegalArgumentException {
        return new TextResourceCombo(Iterables.transform(paths, this));
    }

    /**
     * Registers a local directory with the resolver, whose contents it can subsequently resolve.
     *
     * @param root      the path prefix under which resources in this directory will be mounted
     * @param directory the directory to be mounted
     * @param sourceURI a base URI via which resources contained in this directory can be directly retrieved. This is
     *                  used e.g. in the rewriting of links to external resources
     * @param charset   the character set with with text-based resources in this directory are encoded
     * @param maxAge    the maximum time a resource of this directory may be cached by browsers/HTTP proxies
     * @throws IllegalArgumentException in case the provided file is not a directory or cannot be read
     */
    public void mount(String root, File directory, String sourceURI, Charset charset, long maxAge) throws IllegalArgumentException {
        Preconditions.checkArgument(directory.isDirectory(), directory + " is not a directory");
        Preconditions.checkArgument(directory.canRead(), directory + " cannot be read");
        mountPoints.put(root, new MountPoint(directory, (sourceURI.replaceAll("\\/+$", "") + "/"), charset, maxAge));
    }

    /**
     * Maps a path to a text-based resource.
     *
     * @param input the path to be mapped
     * @return the mapped resource
     * @throws IllegalArgumentException in case the path cannot be mapped, e.g. because a corresponding file does not exist,
     *                                  cannot be read or the specified path references a file not contained in the mounted
     *                                  directories
     */
    @Override
    public TextResource apply(String input) throws IllegalArgumentException {
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

    private final Map<String, MountPoint> mountPoints = Maps.newHashMap();

    private static class MountPoint {
        private final File root;
        private final Charset charset;
        private final URI source;
        private final long maxAge;

        private MountPoint(File root, String sourceURI, Charset charset, long maxAge) {
            this.root = root;
            this.charset = charset;
            this.source = URI.create(sourceURI);
            this.maxAge = maxAge;
        }

        private TextResource resolve(String relativePath) throws IOException, IllegalArgumentException {
            final File resource = new File(root, relativePath);
            Preconditions.checkArgument(resource.isFile(), resource + " is not a file");
            Preconditions.checkArgument(resource.canRead(), resource + " cannot be read");
            Preconditions.checkArgument(isDescendant(root, resource), resource + " is not contained in " + root);
            return new TextResource(resource, source.resolve(relativePath), charset, maxAge);
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
