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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayerr
 */
public class SqlParserTest {

    static class ParserResult {
        private final boolean comment;
        private final String text;

        public ParserResult(boolean comment, String text) {
            this.comment = comment;
            this.text = text;
        }

        public boolean isComment() {
            return comment;
        }

        public String getText() {
            return text;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.comment ? 1 : 0);
            hash = 41 * hash + Objects.hashCode(this.text);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            final ParserResult other = (ParserResult)obj;
            if(this.comment != other.comment) {
                return false;
            }
            if(!Objects.equals(this.text, other.text)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return text + (comment?"|C|":"|X|");
        }
    }
    
    public SqlParserTest() {
    }

    private static ParserResult pr(boolean b, String s) {
        return new ParserResult(b, s);
    }
    
    @Test
    public void test() throws Exception {
        testParser("a", pr(false, "a"));
        testParser("/**/", pr(true, "/**/"));
        testParser("'/**/'", pr(false, "'/**/'"));

        testParser("/*#queryName*/ select t.*, \'/* \'\' */\' as \"te\"\"st\" from t where t.id = 1//*$id*/ /*#queryName end*/",
                pr(true, "/*#queryName*/"),
                pr(false, " select t.*, "), pr(false, "'/* '"),
                pr(false, "' */'"),
                pr(false, " as "),
                pr(false, "\"te\""),
                pr(false, "\"st\""),
                pr(false, " from t where t.id = 1/"),
                pr(true, "/*$id*/"), pr(false, " "),
                pr(true, "/*#queryName end*/")
                );
    }
    
    private void testParser(String query, ParserResult ... prs) throws Exception {
        List<ParserResult> actuals = new ArrayList<>();
        ParserContext ctx = new ParserContext();
        ctx.setTokenHandler(() -> {
            String token = ctx.getRaw();
            System.out.println("** " + token);
            TokenType type = ctx.getType();
            actuals.add(new ParserResult(!type.isCode(), token));
        });
        ctx.parse(new StringReader(query));
        assertArrayEquals(prs, actuals.toArray());
    }
    
    @Test
    public void testWithBuild() throws Exception {
        SqlParser parser = SqlParser.builder().build();
        List<SqlTemplate> list = new ArrayList<>();
        try(Reader r = new InputStreamReader(getClass().getResourceAsStream("./test.sql"), StandardCharsets.UTF_8)) {
            parser.parse(r, list::add);
        }
        System.out.println("List:"+ list);
        final SqlTemplate query0 = list.get(0);
        assertEquals(SqlTemplate.builder().name("fisrtQuery")
                .putAttribute("title", "First query")
                .addField(F("name", "string"))
                .addField(F("code", "tinyint"))
                .addField(F("user", "uuid"))
                .addParam(P("something", "string", SqlTemplate.Direction.INOUT))
                .query("select\n    pName,  \n    pCode,  \n    pUser   \n from pData where pSomething == ?")
                .build(), query0);
        assertEquals(SqlTemplate.builder().name("secondQuery")
                .query("select one, two three from values (1, 2, 3)")
                .build(), list.get(1));
        assertEquals(SqlTemplate.builder().name("queryWitTestData")
                .addField(F("name", "string"))
                .addParam(P("test", null, SqlTemplate.Direction.IN))
                .query("select pName  \n from pData where pSomething == ?")
                .build(), list.get(2));
        assertEquals(SqlTemplate.builder().name("queryWithNativeParam")
          .addParam(P("test.expr", null, SqlTemplate.Direction.IN))
          .query("select * from pData where pSomething == ?")
          .build(), list.get(3));
        assertEquals(4, list.size());
    }

    @Test
    public void testUTF() throws Exception {
        SqlParser parser = SqlParser.builder().build();
        List<SqlTemplate> list = new ArrayList<>();
        try(Reader r = new InputStreamReader(getClass().getResourceAsStream("./test2.sql"), StandardCharsets.UTF_8)) {
            parser.parse(r, list::add);
        }
        System.out.println(list);
        assertEquals(SqlTemplate.builder().name("utfFirst")
          .addField(F("date", null, "title", "Дата", "javaType", "java.util.Date"))
          .addField(F("weight", null, "title", "Вес", "javaType", "float"))
          .addParam(P("id", "INT", null))
          .query("select\n now(),  \n 17843,   \n        ?\n ;")
          .build(), list.get(0));
        assertEquals(SqlTemplate.builder().name("utfSecond")
          .addParam(P("startDate", "timestamp", null, "title", "Дата начала"))
          .addParam(P("endDate", "timestamp", null, "title", "Дата окончания"))
          .addParam(P("devices", null, null))
          .query("select t.*,\n       'комментарий©™' as comment\n  from telemetry t\n" +
            " where t.eventtime >= ?\n   and t.eventtime <= ?\n   and t.device = any (?::varchar[])")
          .build(), list.get(1));
    }

    private SqlTemplate.Field F(String name, String type, String ... attrs) {
        return new SqlTemplate.Field(name, type, attrs(attrs));
    }

    private SqlTemplate.Param P(String name, String type, SqlTemplate.Direction dir, String ... attrs) {
        return new SqlTemplate.Param(name, type, attrs(attrs), dir);
    }

    private Map<String, String> attrs(String ... str) {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < str.length; i += 2) {
            map.put(str[i], str[i + 1]);
        }
        return map;
    }
}
