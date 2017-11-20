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