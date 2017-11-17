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

import wayerr.co.sql.files.SqlTemplate.Field;
import wayerr.co.sql.files.SqlTemplate.Param;

/**
 * Strategy for parsing of template parts.
 * @see DefaultTemplateParser
 * @author wayerr
 */
public interface TemplateParser {
    interface Context {
        TemplateBuilder getTemplateBuilder();
        String getString();
    }
    Field parseField(Context ctx);
    Param parseParam(Context ctx);
    /**
     * Extract name of template from token. Also may extract some other info like fields, if need.
     * @param ctx
     * @return name of template
     */
    String parseTemplate(Context ctx);
}
