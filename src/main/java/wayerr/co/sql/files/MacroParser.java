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

/**
 * Parse comment with data like below:
 * <code>value value2 key1=value key2="value with \" = and spaces" key3=value </code>
 * @author wayerr
 */
public class MacroParser {
    private static final char KV_DELIM = '=';
    private final CharSequence src;
    private final int length;
    private int pos;
    private String key;
    private String value;
    private StringBuilder buff;
    private int oldPos;

    public MacroParser(CharSequence src, int offset) {
        this.src = src;
        this.pos = offset;
        this.length = src.length();
    }

    /**
     * Check that end of string is reached.
     * @return true if end of string is reached
     */
    public boolean isEnd() {
        return length <= pos;
    }

    /**
     * Do parsing of next token
     * @return this
     */
    public MacroParser next() {
        value = null;
        parse();
        return this;
    }

    private void parse() {
        if(value != null || isEnd()) {
            return;
        }
        key = null;
        oldPos = pos;
        final String token = parseToken();
        value = token;
        skipSpaces();
        if(pos < length && src.charAt(pos) == KV_DELIM) {
            pos++;
            // whet it a pair the first value is a key, therefore we must swap them
            key = token;
            value = parseToken();
        }
        skipSpaces();
    }

    private String parseToken() {
        skipSpaces();
        clearBuff();
        int begin = pos;
        int end = 0;
        char quote = 0;
        while(pos < length) {
            char c = src.charAt(pos++);
            if(c == '\\') {
                getOrCreateBuffer().append(src, begin, pos - 1);
                begin = pos;
                pos++;
                continue;
            }
            if(quote == 0) {
                if(c == '"' || c == '\'') {
                    quote = c;
                    begin = pos;
                    continue;
                }
                if(c == KV_DELIM || Character.isSpaceChar(c)) {
                    end = pos - 1;
                    // move backward for external code which must cansee this symbol
                    if(c == KV_DELIM) {
                        pos--;
                    }
                    break;
                }
            } else {
                if(quote == c) {
                    end = pos - 1;
                    break;
                }
            }
        }
        // below statement for cases when end is reached
        if(pos >= length && end == 0) {
            end = length;
        }
        if(end == 0) {
            return null;
        }
        if(buff != null && buff.length() > 0) {
            buff.append(src, begin, end);
            return buff.toString();
        }
        return src.subSequence(begin, end).toString();
    }

    private void skipSpaces() {
        while(pos < length) {
            char c = src.charAt(pos);
            if(!Character.isSpaceChar(c)) {
                break;
            }
            pos++;
        }
    }

    private void clearBuff() {
        if(buff != null) {
            buff.setLength(0);
        }
    }

    private StringBuilder getOrCreateBuffer() {
        if(buff == null) {
            buff = new StringBuilder();
        }
        return buff;
    }

    /**
     * Get value or null
     * @return value or null
     */
    public String getValue() {
        return value;
    }

    /**
     * Get key or null
     * @return key or null
     */
    public String getKey() {
        return key;
    }

    /**
     * Like {@link #getKey() } but throw exception when key is null
     * @return key
     */
    public String reqireKey() {
        if(key == null) {
            throw new NullPointerException("Require key between " + oldPos + " and " + pos + " in '" + src +"'");
        }
        return key;
    }
}
