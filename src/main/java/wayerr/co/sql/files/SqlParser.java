/*
 * Copyright (C) 2016 wayerr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wayerr.co.sql.files;

import java.io.IOException;
import java.io.Reader;
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

    public void parse(Reader text, Consumer<SqlTemplate> templatesConsumer) throws IOException {
        TemplateBuilder tb = new TemplateBuilder(templateParser, templatesConsumer);
        ParserContext ctx = new ParserContext();
        ctx.setTokenHandler(() -> {
            tb.accept(ctx);
        });
        ctx.parse(text);
        tb.close();
    }


}
