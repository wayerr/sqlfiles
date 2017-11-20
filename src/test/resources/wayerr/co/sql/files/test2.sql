
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
