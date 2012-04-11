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
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResource implements InputSupplier<Reader> {

    final File resource;
    final String path;
    final Charset charset;
    final long maxAge;

    public TextResource(File resource, String path, Charset charset, long maxAge) {
        this.resource = resource;
        this.path = path;
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
        return Objects.toStringHelper(this).addValue(path).toString();
    }

    public static final String TEXT_CSS = "text/css";
    public static final String APPLICATION_JAVASCRIPT = "application/javascript";
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_XML = "application/xml";

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

    private static final Pattern URL_REF_PATTERN = Pattern.compile("url\\(([^\\)]+)\\)");

    private class CSSURLRewriteFilterReader extends Reader {
        private final URI path = URI.create(TextResource.this.path);
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
                    urlRefMatcher.appendReplacement(rewritten, "url(" + path.resolve(urlRefMatcher.group(1)) + ")");
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
}
