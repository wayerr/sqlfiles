# SQL-Files

A SQL template engine for Java. 

## Key features

* place templates as valid SQL code in sql files
* use template metadata from SQL comments
* no dependencies

## Sample

```sql

/*#secondTestQuery*/ -- query name
select
 -- definition of type and other field attributes
 now(), /*@date title=Date, javaType=java.util.Date*/ 
 17843,  --@weight title=Wight, javaType=float
-- definition of query parameter 'id' with type INT, the `123` used for test query in sql editor
        /*$id, type=INT {*/123/*}*/
 ;
/*#secondTestQuery end*/

/*#getAlarmButtonEvents */
select t.*,
       (select tc.comment from telemetrycomments tc where tc.id = t.uid) as comment
  from telemetry t
 where t.eventtime >= /*$startDate, type=timestamp {*/to_timestamp('20121206000000','YYYYMMDDHH24MISS')/*}*/
   and t.eventtime <= /*$endDate, type=timestamp {*/to_timestamp('20121206235959','YYYYMMDDHH24MISS')/*}*/
   and (t.provider || ':' || t.deviceid) = any (/*$devices {*/'{"test:test"}'/*}*/::varchar[])
   and t.eventtype = 'ALARM_BUTTON'
/*#getAlarmButtonEvents end*/
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
select t.*,
       (select tc.comment from telemetrycomments tc where tc.id = t.uid) as comment
  from telemetry t
 where t.eventtime >= ?
   and t.eventtime <= ?
   and (t.provider || ':' || t.deviceid) = any (?::varchar[])
   and t.eventtype = 'ALARM_BUTTON'
``` 

