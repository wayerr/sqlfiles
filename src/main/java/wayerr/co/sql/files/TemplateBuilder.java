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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Assembly template tags into single template.
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

    private final SqlTemplate.Builder builder = new SqlTemplate.Builder();
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
        boolean reconstruct = true;
        if(!type.isCode()) {
            String content = token.getContent();
            mt = lastMacro = detectMacro(content);
            if(mt != null) {
                reconstruct = false;
                replaced = false;
                processMacro(content, mt);
                if(!replaced) {
                    // do default replacing of macro comment
                    //comment may act as space character, in cases like 'from/*comment* /tableName'
                    // therefore we must add extra space instead of 'macro' comments
                    queryChunks.add(" ");
                }
            }
        } else if(type == TokenType.NAMED_PARAM) {
            reconstruct = false;
            builder.addParam(new SqlTemplate.Param(token.getContent(), null, null, SqlTemplate.Direction.IN));
            replaceWith("?");
        }
        if(!inBuild()) {
            // we not append anything before template name
            return;
        }
        if(reconstruct) {
            // we reconstruct code and
            // non macro comments, because it may act as hints for some sql engines
            queryChunks.add(token.getRaw());
        }
        if(lastMacro != null && !type.isCode()) {
            // when comment is macro we must remove full comment include open and closing parts
            lastMacro = null;
        }
    }


    /**
     * True when build of SqlTemplate in process.
     * @return true when build in process
     */
    public boolean inBuild() {
        return builder.getName() != null;
    }

    /**
     * Get internal template builder. Note that it reused, therefore do not store it anywhere.
     * @return builder
     */
    public SqlTemplate.Builder getBuilder() {
        return builder;
    }

    private void replaceWith(String replacer) {
        queryChunks.add(replacer);
        replaced = true;
    }

    private void processParam(String token) {
        final SqlTemplate.Param param = templateParser.parseParam(new ContextImpl(token));
        builder.addParam(param);
        // we also must add 'replacer()' concept which will got param and
        // had replaced it with ':paramName' for example

        //note, that we must replace without additional spaces, because code may use parameter in some expressions
        // which is sensitivity to spaces
        replaceWith("?");
    }

    private void processField(String token) {
        final SqlTemplate.Field field = templateParser.parseField(new ContextImpl(token));
        builder.addField(field);
    }

    private void processTemplate(String token) {
        if(inBuild()) {
            buildTemplate();
        }
        final String name = templateParser.parseTemplate(new ContextImpl(token));
        Objects.requireNonNull(name, templateParser + " return null name from " + token);
        builder.setName(name);
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
        if(!inBuild()) {
            if(!builder.getFields().isEmpty() || !builder.getParams().isEmpty()) {
                throw new IllegalStateException("Corrupted state of builder: has fileds or params but name is null");
            }
            return;
        }
        queryBuilder.setLength(0);
        queryChunks.forEach(queryBuilder::append);
        builder.query(queryBuilder.toString().trim());
        SqlTemplate st = builder.build();
        //clear immediate after build
        builder.clear();
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
