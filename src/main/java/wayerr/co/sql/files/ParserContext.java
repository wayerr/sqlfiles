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

import java.io.IOException;
import java.io.Reader;

/**
 * Context which hold state of pasing process
 * @author wayerr
 */
class ParserContext implements Token {

    private final StringBuilder sb = new StringBuilder();
    private TokenType state = TokenType.CODE;
    private int curr;
    private Runnable tokenHandler;
    private String raw;
    
    void setTokenHandler(Runnable tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    void parse(Reader text) throws IOException {
        while((curr = text.read()) != -1) {
            sb.append((char)curr);
            if(curr == '\'') {
                if(state == TokenType.STRING) {
                    transfer(TokenType.CODE, 0);
                } else if(state == TokenType.CODE) {
                    transfer(TokenType.STRING, 1);
                }
            } else if(curr == '\"') {
                if(state == TokenType.QUOTED_IDENTIFIER) {
                    transfer(TokenType.CODE, 0);
                } else if(state == TokenType.CODE) {
                    transfer(TokenType.QUOTED_IDENTIFIER, 1);
                }
            } else if(matchEnd("--")) {
                if(state == TokenType.CODE) {
                    transfer(TokenType.COMMENT_LINE, 2);
                }
            } else if(curr == '\n' || curr == '\r') {
                if(state == TokenType.COMMENT_LINE) {
                    transfer(TokenType.CODE, 1);
                }
            } else if(state == TokenType.CODE && matchEnd("/*")) {
                transfer(TokenType.COMMENT, 2);
            } else if(state == TokenType.COMMENT && matchEnd("*/")) {
                transfer(TokenType.CODE, 0);
            }
        }
        transfer(null, 0);
    }

    private void transfer(TokenType state, int offset) {
        int newlen = sb.length() - offset;
        String tail = sb.substring(newlen);
        if(newlen > 0) {
            // it may happen only for CODE state
            sb.setLength(newlen);
            tokenHandler.run();
            raw = null;
        }
        sb.setLength(0);
        sb.append(tail);
        this.state = state;
    }

    private boolean matchEnd(String string) {
        int strlen = string.length();
        int sbOff = sb.length() - strlen;
        if(sbOff < 0) {
            return false;
        }
        for(int i = 0; i < strlen; i++) {
            char actual = sb.charAt(sbOff + i);
            char expected = string.charAt(i);
            if(actual != expected) {
                return false;
            }
        }
        return true;
    }

    String getToken() {
        return sb.toString();
    }

    @Override
    public TokenType getType() {
        return state;
    }

    @Override
    public String getRaw() {
        if(raw == null) {
            raw = sb.toString();
        }
        return raw;
    }

    @Override
    public String getContent() {
        String raw = getRaw();
        String content = raw;
        switch(state) {
            case COMMENT:
               content = raw.substring(2, raw.length() - 2);
               break;
           case COMMENT_LINE:
               content = raw.substring(2);
               break;
           case QUOTED_IDENTIFIER:
           case STRING:
               content = raw.substring(1, 1);
               break;
        }
        return content;
    }
}
