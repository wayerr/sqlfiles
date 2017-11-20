# Examples

## Sql templates

```sql

/*#secondTestQuery*/ -- query name
select
 -- definition of type and other field attributes
 now(), /*@date title=Date javaType=java.util.Date*/ 
 17843,  --@weight title=Wight javaType=float
-- definition of query parameter 'id' with type INT, the `123` used for test query in sql editor
        /*$id type=INT {*/123/*}*/
 ;

/*#getAlarmButtonEvents */
select t.*, "comment" as comment
  from telemetry t
 where t.eventtime >= /*$startDate type=timestamp*/ 
   and t.eventtime <= /*$endDate type=timestamp {*/to_timestamp('20121206235959','YYYYMMDDHH24MISS')/*}*/
   and t.device = any (/*$devices {*/'{"test:test"}'/*}*/::varchar[])
```

After parsing system provide following queries:

```sql
-- secondTestQuery
select
 now(),
 17843,
 ?
 ;
-- getAlarmButtonEvents
select t.*, "comment" as comment
  from telemetry t
 where t.eventtime >= ? 
   and t.eventtime <= ?
   and t.device = any (?::varchar[])
``` 

## How to use with JDBC

Code:
```java
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
```

Sql:
```sql
/*#FirstExample*/
select
  *
  /*@date type=TIMESTAMP*/
  /*@weight type=INT*/
  /*@now type=TIMESTAMP*/
 from (values
   (TIMESTAMP '2008-11-22 20:30:40', 1, /*$now type=TIMESTAMP*/),
   (TIMESTAMP '2009-01-01 20:30:40', 2, /*$now type=TIMESTAMP*/),
   (TIMESTAMP '2011-04-13 20:30:40', 3, /*$now type=TIMESTAMP*/)
 );
```

Output:

```
1	date=2008-11-22 20:30:40.0	weight=1	now=2017-11-17	
2	date=2009-01-01 20:30:40.0	weight=2	now=2017-11-17	
3	date=2011-04-13 20:30:40.0	weight=3	now=2017-11-17	
```