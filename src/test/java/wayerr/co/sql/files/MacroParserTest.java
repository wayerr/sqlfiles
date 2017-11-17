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
