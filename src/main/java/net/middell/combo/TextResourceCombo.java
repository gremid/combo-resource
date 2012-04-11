package net.middell.combo;

import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResourceCombo extends ArrayList<TextResource> {

    public TextResourceCombo(Iterable<TextResource> resources) {
        super(Iterables.size(resources));
        Iterables.addAll(this, resources);
    }

    public String getMediaType() {
        return (isEmpty() ? TextResource.TEXT_PLAIN : get(0).getMediaType());
    }

    public long lastModified() {
        long lastModified = -1;
        for (TextResource rd : this) {
            lastModified = Math.max(lastModified, rd.resource.lastModified());
        }
        return (lastModified >= 0 ? lastModified : System.currentTimeMillis());
    }

    public long maxAge() {
        long maxAge = Long.MAX_VALUE;
        for (TextResource rd : this) {
            maxAge = Math.min(maxAge, rd.maxAge);
        }
        return (maxAge < Long.MAX_VALUE ? maxAge : 0);
    }

    public void copyTo(Writer out) throws IOException {
        CharStreams.copy(CharStreams.join(this), out);
    }
}
