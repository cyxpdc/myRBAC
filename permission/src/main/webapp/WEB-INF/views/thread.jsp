<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>服务器线程信息</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="This is my page">
</head>
<body>
    <pre>
        <%
            for(Map.Entry<Thread,StackTraceElement[]> stackTrace : Thread.getAllStackTraces().entrySet()){
                Thread thread = stackTrace.getKey();
                StackTraceElement[] stack = stackTrace.getValue();
                if(thread.equals(Thread.currentThread())){
                    continue;
                }
                out.print("\n线程："+thread.getName()+"\n");
                for(StackTraceElement element : stack){
                    out.print("\t"+element+"\n");
                }
            }
        %>
    </pre>
</body>
</html>
