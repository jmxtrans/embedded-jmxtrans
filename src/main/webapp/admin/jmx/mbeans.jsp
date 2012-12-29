<%@ page import="java.lang.management.ManagementFactory"%>
<%@ page import="javax.management.*"%>
<%@ page import="java.io.*,java.util.*"%>
<%@page import="java.net.InetAddress"%>
<%@page import="java.net.URLEncoder"%><html>
<head>
<title>MBeanServers</title>
</head>
<body>
<h1>MBeanServers</h1>
<%
    try {
        out.println("Server: " + InetAddress.getLocalHost() + ", date: "
                    + new java.sql.Timestamp(System.currentTimeMillis()).toString() + "<br>");
        
        List<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(null);
        for (MBeanServer mbeanServer : mbeanServers) {
            
            out.println("<h1>MbeanServer " + mbeanServer + "</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>Object Name</th><th>Description</th></tr>");
            List<ObjectInstance> objectInstances = new ArrayList<ObjectInstance>(mbeanServer.queryMBeans(ObjectName.WILDCARD, null));
            Collections.sort(objectInstances, new Comparator<ObjectInstance>() {
                public int compare(ObjectInstance o1, ObjectInstance o2) {
                    return o1.getObjectName().compareTo(o2.getObjectName());
                }
                
            });
            for (ObjectInstance objectInstance : objectInstances) {
                ObjectName objectName = objectInstance.getObjectName();
                out.println("<tr>");
                out.println("<td><a href='mbean.jsp?name=" + URLEncoder.encode(objectName.getCanonicalName(), "UTF-8") + "'>"
                            + objectName + "</a></td>");
                out.println("<td><em>" + mbeanServer.getMBeanInfo(objectName).getDescription() + "</em></td>");
                out.println("</tr>");
                out.flush();
            }
            
            out.println("</table>");
            out.println("Total mbeans count <b>" + objectInstances.size() + "</b>");
        }
    } catch (Throwable e) {
        out.println("<pre>");
        PrintWriter printWriter = new PrintWriter(out);
        e.printStackTrace(printWriter);
        out.println("</pre>");
        printWriter.flush();
    }
%>
</body>
</html>