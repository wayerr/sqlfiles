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

import wayerr.co.sql.files.SqlTemplate.Field;
import wayerr.co.sql.files.SqlTemplate.Param;

/**
 * Strategy for parsing of template parts. Note that eachmethod take full 
 * token include type identified (symbols '#', '@' and '$') part.
 * @see DefaultTemplateParser
 * @author wayerr
 */
public interface TemplateParser {
    interface Context {
        TemplateBuilder getTemplateBuilder();
        String getString();
    }
    public Field parseField(Context ctx);
    public Param parseParam(Context ctx);
    /**
     * Extract name of template from token. Also may extract some other info like fields, if need.
     * TODO return SqlTemplateFactory or some like for allow to produce custom object
     * @param ctx
     * @return name of template
     */
    public String parseTemplate(Context ctx);
}
