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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wayerr
 */
public class DefaultTemplateParser implements TemplateParser {

    /**
     * Appear of this is mean that follow code must been excluded
     */
    private static final String SAMPLE_VALUE = "{";

    @Override
    public SqlTemplate.Field parseField(Context ctx) {
        MacroParser cp = new MacroParser(ctx.getString(), 0);
        cp.next();
        String name = cp.getValue();
        String type = null;
        Map<String, String> attrs = null;
        while(!cp.isEnd()) {
            cp.next();
            String key = cp.getKey();
            String val = cp.getValue();
            switch(key) {
                case "type":
                    type = val;
                    break;
                default:
                    attrs = putAttr(attrs, key, val);
            }
        }
        return new SqlTemplate.Field(name, type, attrs);
    }

    @Override
    public SqlTemplate.Param parseParam(Context ctx) {
        MacroParser cp = new MacroParser(ctx.getString(), 0);
        cp.next();
        String name = cp.getValue();
        String type = null;
        SqlTemplate.Direction direction = null;
        Map<String, String> attrs = null;
        while(!cp.isEnd()) {
            cp.next();
            String val = cp.getValue();
            if(SAMPLE_VALUE.equals(val)) {
                ctx.getTemplateBuilder().setTokenFilter(new TokenFilterImpl());
                break;
            }
            String key = cp.reqireKey();
            switch(key) {
                case "type":
                    type = val;
                    break;
                case "dir":
                    direction = SqlTemplate.Direction.from(val);
                    break;
                default:
                    attrs = putAttr(attrs, key, val);
            }
        }
        if(direction == null) {
            direction = SqlTemplate.Direction.IN;
        }
        return new SqlTemplate.Param(
                name,
                type,
                attrs,
                direction);
    }

    private Map<String, String> putAttr(Map<String, String> attrs, String key, String val) {
        if(attrs == null) {
            attrs = new HashMap<>();
        }
        attrs.put(key, val);
        return attrs;
    }

    @Override
    public String parseTemplate(Context ctx) {
        // we also can define fields here
        MacroParser cp = new MacroParser(ctx.getString(), 0);
        cp.next();
        String name = cp.getValue();
        SqlTemplate.Builder builder = ctx.getTemplateBuilder().getBuilder();
        while(!cp.isEnd()) {
            cp.next();
            String key = cp.getKey();
            String val = cp.getValue();
            builder.putAttribute(key, val);

        }
        return name;
    }

    private static class TokenFilterImpl implements TemplateBuilder.TokenFilter {

        @Override
        public boolean skip(TemplateBuilder tb, Token token) {
            if(!token.getType().isCode() && token.getContent().trim().equals("}")) {
                TemplateBuilder.TokenFilter curr = tb.getTokenFilter();
                if (curr != this) {
                    throw new IllegalStateException("Unexpected token filter: " + curr + ", expect: " + this);
                }
                tb.setTokenFilter(null);
            }
            return true;
        }
    }

}
