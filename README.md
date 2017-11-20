# SQL-Files

A SQL template engine for Java. 

**Note**, library in development state, therefore API may be changed without backward capability.  

## Key features

* place templates as valid SQL code in sql files
* use template metadata from SQL comments
* no dependencies

# Format

Template tags is a usual SQL comments with marker symbol in begin. Currently we use `#`, `@` and `$` - symbols.
Each query template start as comment with `#` and name after it. 
Field of template is defined as comments with `@` at begin. Parameters will start with `$`, and may have 'sample value'. 
Its value will be removed at query generation, but can be used in query test and execution in sql editor.      

```sql
/*#queryName attrKey1=attrVal attrKey2="other val"*/
select
 now(), /*@date title=Date javaType=java.util.Date*/ 
 17843,  --@weight type=Wight
        /*$id type=INT {*/123/*}*/
 ;
```

So, this template engine declare following comment types:

* `#[name] [key]=[value]*` - template header
* `@[name] type=[value] [key]=[value]*` - template field, has predefined attribute: 'type' (sql type name)
* `$[name] type=[value] dir=[IN|OUT|INOUT] [key]=[value]*` - template parameter, has predefined 
attributes: 'type' see field attribute with same name, 'dir' - one of IN|OUT|INOUT it a parameter direction. 
Sample value can be placed in braces like following: `{*/sample value/*}*/`, note that only multiline comments can
 support this syntax.   

Parser consume template and provide SqlTemplate object:

```json
{
    "name":"queryName",
    "attributes":{"attrKey2":"other val", "attrKey1":"attrVal"}, 
    "fields":[
        {
            "name":"date", 
            "attributes":{
                "title":"Date", 
                "javaType":"java.util.Date"
            }
        }, 
        {
            "name":"weight", 
            "type":"Wight"
        }
    ], 
    "params":[
        {
            "name":"id", 
            "type":"INT", 
            "direction":"IN"
        }
    ],
    "query":"select\n    now(),  \n    17843,   \n           ?\n    ;"
}
``` 

It may be used in any database access framework, in examples we show usage with usual JDBC.

See [examples](doc/examples.md)