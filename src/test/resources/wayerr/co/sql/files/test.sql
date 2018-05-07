-- some common comment

--#fisrtQuery title="First query"
select
    pName, --@name type=string
    pCode, --@code type=tinyint
    pUser  --@user type=uuid
 from pData where pSomething == /*$ something type=string dir=INOUT*/

--#secondQuery 
select one, two three from values (1, 2, 3)

--#queryWitTestData
select pName --@name type=string
 from pData where pSomething == /*$ test {*/'code'/*}*/

--#queryWithNativeParam
select * from pData where pSomething == :test.expr
