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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayerr
 */
public class MacroParserTest {

    public MacroParserTest() {
    }

    @Test
    public void test() {
        MacroParser mp = create("simple");
        notEnd(mp);
        assertNull(mp.getValue());
        assertNull(mp.getKey());
        mp.next();
        expectValue(mp, "simple");
        end(mp);
    }

    @Test
    public void testValueAndKv() {
        MacroParser mp = create("name key=value ");
        mp.next();
        expectValue(mp, "name");
        notEnd(mp);
        mp.next();
        expectKv(mp, "key", "value");
        end(mp);
    }

    @Test
    public void testKv() {
        MacroParser mp = create("key=value");
        mp.next();
        expectKv(mp, "key", "value");
        end(mp);
    }

    @Test
    public void testThemAll() {
        MacroParser mp = create("'n\"a\"me' \"va\\\" lue with = and spaces\" 'k ey' = 'v alue' ");
        mp.next();
        expectValue(mp, "n\"a\"me");
        notEnd(mp);
        mp.next();
        expectValue(mp, "va\" lue with = and spaces");
        notEnd(mp);
        mp.next();
        expectKv(mp, "k ey", "v alue");
        end(mp);
    }

    private void end(MacroParser mp) {
        assertTrue(mp.isEnd());
    }

    private void notEnd(MacroParser mp) {
        assertFalse(mp.isEnd());
    }

    private void expectValue(MacroParser mp, String str) {
        assertEquals(str, mp.getValue());
        assertNull(mp.getKey());
    }

    private void expectKv(MacroParser mp, String key, String value) {
        assertEquals(key, mp.getKey());
        assertEquals(value, mp.getValue());
    }

    private MacroParser create(String str) {
        return new MacroParser(str, 0);
    }

}
