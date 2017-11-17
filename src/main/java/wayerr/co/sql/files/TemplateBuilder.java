/*
 * Copyright (C) 2017 wayerr
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author wayerr
 */
public class TemplateBuilder {
    private static final char TEMPLATE = '#';
    private static final char FIELD = '@';
    private static final char PARAM = '$';

    private class ContextImpl implements TemplateParser.Context {

        private final String token;

        public ContextImpl(String token) {
            this.token = token;
        }

        @Override
        public TemplateBuilder getTemplateBuilder() {
            return TemplateBuilder.this;
        }

        @Override
        public String getString() {
            // we remove first character #, @ or $
            return token.substring(1);
        }
    }

    public interface TokenFilter {
        boolean skip(TemplateBuilder tb, Token token);
    }

    enum MacroType {
        TEMPLATE, FIELD, PARAM
    }

    private String templateName;
    private final List<SqlTemplate.Field> fields = new ArrayList<>();
    private final List<SqlTemplate.Param> params = new ArrayList<>();
    private final List<String> queryChunks = new ArrayList<>();
    private final StringBuilder queryBuilder = new StringBuilder();
    private final TemplateParser templateParser;
    private final Consumer<SqlTemplate> consumer;
    /**
     * This flag allow us to detect when queryBuilder is modifiead after
     * 'accept(' begin. This is need for replacing unprocesed comments with spaces
     */
    private boolean replaced;
    private MacroType lastMacro;
    private TokenFilter tokenFilter;

    TemplateBuilder(TemplateParser templateParser, Consumer<SqlTemplate> consumer) {
        Objects.requireNonNull(templateParser, "templateParser is null");
        Objects.requireNonNull(consumer, "template consumer is null");
        this.templateParser = templateParser;
        this.consumer = consumer;
    }

    public void accept(Token token) {
        if (tokenFilter != null) {
            if(tokenFilter.skip(this, token)) {
                return;
            }
        }
        TokenType type = token.getType();
        MacroType mt = null;
        if(!type.isCode()) {
            String content = token.getContent();
            mt = lastMacro = detectMacro(content);
            if(mt != null) {
                replaced = false;
                processMacro(content, mt);
                if(!replaced) {
                    // do default replacing of macro comment
                    //comment may act as space character, in cases like 'from/*comment* /tableName'
                    // therefore we must add extra space instead of 'macro' comments
                    queryChunks.add(" ");
                }
            }
        } 
        if(templateName == null) {
            // we not appent anything before template name
            return;
        }
        if(lastMacro == null) {
            // we reconstruct code and
            // non macro comments, because it may act as hints for some sql engines
            queryChunks.add(token.getRaw());
        }
        if(lastMacro != null && !type.isCode()) {
            // when comment is macro we must remove full comment include open and closing parts
            lastMacro = null;
        }
    }

    private void replaceWith(String replacer) {
        queryChunks.add(replacer);
        replaced = true;
    }

    private void processParam(String token) {
        final SqlTemplate.Param param = templateParser.parseParam(new ContextImpl(token));
        params.add(param);
        // we also may add 'replacer()' concept which will got param and
        // had replaced it with ':paramName' for example
        replaceWith(" ? ");
    }

    private void processField(String token) {
        final SqlTemplate.Field field = templateParser.parseField(new ContextImpl(token));
        fields.add(field);
    }

    private void processTemplate(String token) {
        if(templateName != null) {
            buildTemplate();
        }
        final String name = templateParser.parseTemplate(new ContextImpl(token));
        Objects.requireNonNull(name, templateParser + " return null name from " + token);
        templateName = name;
    }

    private MacroType detectMacro(String str) {
        if(str.isEmpty()) {
            return null;
        }
        switch(str.charAt(0)) {
            case FIELD:
                return MacroType.FIELD;
            case PARAM:
                return MacroType.PARAM;
            case TEMPLATE:
                return MacroType.TEMPLATE;
        }
        return null;
    }
    private void processMacro(String token, MacroType macroType) {
        switch(macroType) {
            case FIELD:
                processField(token);
                break;
            case PARAM:
                processParam(token);
                break;
            case TEMPLATE:
                processTemplate(token);
                break;
        }
    }

    void close() {
        buildTemplate();
    }

    private void buildTemplate() {
        if(templateName == null) {
            if(!fields.isEmpty() || !params.isEmpty()) {
                throw new IllegalStateException("Corrupted state of builder: has fileds or params but name is null");
            }
            return;
        }
        queryBuilder.setLength(0);
        queryChunks.forEach(queryBuilder::append);
        SqlTemplate st = new SqlTemplate(templateName, queryBuilder.toString().trim(), fields, params);
        //clear immediate after build
        templateName = null;
        fields.clear();
        params.clear();
        queryChunks.clear();

        consumer.accept(st);
    }

    public TokenFilter getTokenFilter() {
        return tokenFilter;
    }

    public void setTokenFilter(TokenFilter tokenFilter) {
        this.tokenFilter = tokenFilter;
    }
}
