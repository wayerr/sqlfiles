
/*#secondTestQuery*/ --название запроса
select
 now(), /*@date title=Дата, javaType=java.util.Date*/ -- тут определяется тип и аттрибуты поля
 17843,  --@weight title=Вес, javaType=float
--тут определеяется параметр запроса id с типом INT, строка `123` для использования в SQL редакторе, т.е. только для тестирования и отладки
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
/*   После парсинга шаблоны запросов будут преобразованы в код пригодный для выполнения:   */
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