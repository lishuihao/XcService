<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
<br/>
遍历数据模型中list中的学生信息名称(stus)
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>金额</td>
        <td>出生日期</td>
    </tr>
    <#list stus as stu>
        <tr>
            <td>${stu_index}</td>
            <td <#if stu.name=='小明'>style="background: chartreuse" </#if>>${stu.name}</td>
            <td>${stu.age}</td>
            <td <#if stu.money gt 300>style="background: chartreuse" </#if>>${stu.money}</td>
            <#--<td>${stu.birthday}</td>-->
        </tr>
    </#list>
</table>
<br/>
遍历数据模型中的map
姓名:${stuMap['stu1'].name}
</body>
</html>