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
 * A token iface. Note that it can be invalid out of handler scope, therefore
 * can not be stored.
 * @author wayerr
 */
public interface Token {
    /**
     * provide raw content of token, how it appeared in parsed code
     * @return
     */
    String getRaw();
    /**
     * Provide processed contexnt. Unescaped string, comments content & etc.
     * @return
     */
    String getContent();

    TokenType getType();
}
