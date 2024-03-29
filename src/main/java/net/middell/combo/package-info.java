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
/**
 * YUI-compatible utilities for creating resource combos,
 * e.g. CSS/JavaScript rollups.
 *
 * <p>In order to <a href="https://developers.google.com/speed/docs/best-practices/rtt" title="Google Developers - Best Practices">minizime round-trip times</a>
 * between web servers and clients, external resources (e. g. JavaScript and CSS files) are combined on the server-side
 * and delivered as a single resource. Frameworks like Yahoo's <a href="http://yuilibrary.com/" title="YUI Homepage">YUI</a>
 * support this best practice.</p>
 *
 * <p>This package provides utility classes to retrieve {@link net.middell.combo.TextResource text-based resources} from a
 * {@link net.middell.combo.TextResourceResolver#mount(String, TextResourceCollection) configurable} set of
 * {@link net.middell.combo.TextResourceCollection resource collections}, concatenate them to a single “{@link net.middell.combo.TextResourceCombo resource combo}” and
 * jointly provide them for streaming, e.g. to a servlet response stream, a file cache etc.).</p>
 *
 * <p>Rudimentary support is implemented for {@link net.middell.combo.TextResource.CSSURLRewriteFilterReader rewriting links} to external
 * resources within the combined ones, e.g. <code>url()</code> constructs in CSS resources.</p>
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
package net.middell.combo;