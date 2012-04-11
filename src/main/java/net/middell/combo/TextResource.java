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
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text-based resource to be delivered via HTTP.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResource implements InputSupplier<Reader> {

    final File resource;
    final URI source;
    final Charset charset;
    final long maxAge;

    public TextResource(File resource, URI source, Charset charset, long maxAge) {
        this.resource = resource;
        this.source = source;
        this.charset = charset;
        this.maxAge = maxAge;
    }

    public String getMediaType() {
        return Objects.firstNonNull(MIME_TYPES.get(TO_FILENAME_EXTENSION.apply(resource.getName())), TEXT_PLAIN);
    }

    @Override
    public Reader getInput() throws IOException {
        final BufferedReader reader = Files.newReader(resource, charset);
        return (TEXT_CSS.equals(getMediaType()) ? new CSSURLRewriteFilterReader(reader) : reader);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(source).toString();
    }

    protected class CSSURLRewriteFilterReader extends Reader {
        private final Reader in;
        private StringReader buf;

        private CSSURLRewriteFilterReader(Reader in) {
            this.in = in;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (buf == null) {
                final String css = CharStreams.toString(in);
                final Matcher urlRefMatcher = URL_REF_PATTERN.matcher(css);
                final StringBuffer rewritten = new StringBuffer(css.length());
                while (urlRefMatcher.find()) {
                    urlRefMatcher.appendReplacement(rewritten, "url(" + source.resolve(urlRefMatcher.group(1)) + ")");
                }
                urlRefMatcher.appendTail(rewritten);
                buf = new StringReader(rewritten.toString());
            }
            return buf.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            Closeables.close(buf, false);
        }
    }

    private static final Pattern URL_REF_PATTERN = Pattern.compile("url\\(([^\\)]+)\\)");

    static final String TEXT_CSS = "text/css";
    static final String APPLICATION_JAVASCRIPT = "application/javascript";
    static final String APPLICATION_JSON = "application/json";
    static final String TEXT_PLAIN = "text/plain";
    static final String APPLICATION_XML = "application/xml";

    static Map<String, String> MIME_TYPES = Maps.newHashMap();

    static {
        MIME_TYPES.put("css", TEXT_CSS);
        MIME_TYPES.put("js", APPLICATION_JAVASCRIPT);
        MIME_TYPES.put("json", APPLICATION_JSON);
        MIME_TYPES.put("txt", TEXT_PLAIN);
        MIME_TYPES.put("xml", APPLICATION_XML);
    }

    static final Function<String,String> TO_FILENAME_EXTENSION = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return Objects.firstNonNull(Files.getFileExtension(input), "").toLowerCase();
        }
    };
}
