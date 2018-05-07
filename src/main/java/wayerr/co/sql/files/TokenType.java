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
 *
 * @author wayerr
 */
public enum TokenType {
    CODE(true),
    COMMENT(false),
    COMMENT_LINE(false),
    QUOTED_IDENTIFIER(true),
    STRING(true),
    NAMED_PARAM(true),
    ;

    private final boolean code;

    TokenType(boolean code) {
        this.code = code;
    }

    public boolean isCode() {
        return code;
    }
}
