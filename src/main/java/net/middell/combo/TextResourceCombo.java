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
