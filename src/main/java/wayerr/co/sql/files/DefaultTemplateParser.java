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
        while(!cp.isEnd()) {
            cp.next();
            String key = cp.getKey();
            String val = cp.getValue();
            switch(key) {
                case "type":
                    type = val;
                    break;
            }
        }
        return new SqlTemplate.Field(name, type);
    }

    @Override
    public SqlTemplate.Param parseParam(Context ctx) {
        MacroParser cp = new MacroParser(ctx.getString(), 0);
        cp.next();
        String name = cp.getValue();
        String type = null;
        SqlTemplate.Direction direction = null;
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
            }
        }
        if(direction == null) {
            direction = SqlTemplate.Direction.IN;
        }
        return new SqlTemplate.Param(
                name,
                type,
                direction);
    }

    @Override
    public String parseTemplate(Context ctx) {
        // we also can define fields here
        return ctx.getString().trim();
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
