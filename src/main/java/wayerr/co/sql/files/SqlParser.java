/*
 *    Copyright 2017 wayerr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
package wayerr.co.sql.files;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Parse sql file to sequence of {@link SqlTemplate}
 *
 * @author wayerr
 */
public class SqlParser {

    public static class Builder {

        private TemplateParser templateParser = new DefaultTemplateParser();

        public TemplateParser getTemplateParser() {
            return templateParser;
        }

        public Builder templateParser(TemplateParser templateParser) {
            setTemplateParser(templateParser);
            return this;
        }

        public void setTemplateParser(TemplateParser templateParser) {
            this.templateParser = templateParser;
        }

        public SqlParser build() {
            return new SqlParser(this);
        }
    }

    private final TemplateParser templateParser;

    SqlParser(Builder builder) {
        this.templateParser = Objects.requireNonNull(builder.templateParser, "templateParser is null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SqlParser getDefault() {
        return new Builder().build();
    }

    /**
     * Parse specified sql template to consumer.
     * @param text template
     * @param templatesConsumer consumer
     * @throws IOException
     */
    public void parse(Reader text, Consumer<SqlTemplate> templatesConsumer) throws IOException {
        TemplateBuilder tb = new TemplateBuilder(templateParser, templatesConsumer);
        ParserContext ctx = new ParserContext();
        ctx.setTokenHandler(() -> {
            tb.accept(ctx);
        });
        ctx.parse(text);
        tb.close();
    }

    /**
     * Parse specified sql template to immutable map of templates. Name of template is a key for map.
     * @param text template
     * @return map with (template.name, template) entries.
     * @throws IOException
     */
    public Map<String, SqlTemplate> parseToMap(Reader text) throws IOException {
        Map<String, SqlTemplate> map = new HashMap<>();
        parse(text, (template) -> {
            map.put(template.getName(), template);
        });
        return Collections.unmodifiableMap(map);
    }

}
