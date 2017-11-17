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

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ExampleTest {

    @Test
    public void example1() throws Exception {
        // load templates
        Map<String, SqlTemplate> templates;
        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("./example.sql"), "UTF-8")) {
            templates = SqlParser.getDefault().parseToMap(reader);
        }
        // use template with JDBC
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
        Map<String, Object> exampleValues = getExampleValues();
        SqlTemplate template = templates.get("FirstExample");
        PreparedStatement statement = connection.prepareStatement(template.getQuery());
        // place parameters from template to prepared statement
        List<SqlTemplate.Param> params = template.getParams();
        for(int i = 0; i < params.size(); i++) {
            SqlTemplate.Param param = params.get(i);
            Object value = exampleValues.get(param.getName());
            String typeName = param.getType();
            // we refer to types by its name, but JDBC require int code
            // you can use int and simply parse it
            Integer sqlType = JDBCType.valueOf(typeName).getVendorTypeNumber();
            int sqlIndex = i + 1;
            if(value == null) {
                statement.setNull(sqlIndex, sqlType);
            } else {
                statement.setObject(sqlIndex, value, sqlType);
            }
        }
        // execute
        ResultSet result = statement.executeQuery();
        List<SqlTemplate.Field> fields = template.getFields();
        StringBuilder sb = new StringBuilder();
        while(result.next()) {
            sb.append(result.getRow()).append('\t');
            for(int i = 0; i < fields.size(); i++) {
                SqlTemplate.Field field = fields.get(i);
                Object value = result.getObject(i + 1);
                sb.append(field.getName()).append("=").append(value).append('\t');
            }
            System.out.println(sb);
            sb.setLength(0);
        }
    }

    private Map<String, Object> getExampleValues() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("string", "An example string");
        map.put("int", 1);
        map.put("now", new Date(System.currentTimeMillis()));
        return map;
    }
}
