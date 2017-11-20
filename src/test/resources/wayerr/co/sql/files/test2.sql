
--#utfFirst
select
 now(), --@date title=Дата javaType=java.util.Date
 17843,  --@weight title=Вес javaType=float
        /*$id type=INT {*/123/*}*/
 ;

/*#utfSecond*/
select t.*,
       'комментарий©™' as comment
  from telemetry t
 where t.eventtime >= /*$startDate type=timestamp title="Дата начала" {*/'20121206'//*}*/
   and t.eventtime <= /*$endDate type=timestamp  title="Дата окончания" {*/'20121206235959'/*}*/
   and t.device = any (/*$devices {*/'{"test:test"}'/*}*/::varchar[])

/*#queryName attrKey1=attrVal attrKey2="other val"*/
select
 -- field of template is defined as comments with '@' at begin
 now(), /*@date title=Date javaType=java.util.Date*/
 17843,  --@weight type=Wight
-- definition of query parameter 'id' with type INT, the `123` used for test query in sql editor
        /*$id type=INT {*/123/*}*/
 ;