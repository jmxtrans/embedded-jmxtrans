<%@ page import="javax.management.*" %>
<%@ page import="java.io.*,java.util.*" %>
<%@page import="java.net.InetAddress" %>
<%@ page import="java.lang.reflect.Array" %>
<%!
    String formatValue(String name, Object rawValue) {
        String value;
        try {
            if (name.indexOf("classpath") >= 0 ||
                    name.indexOf("java.library.path") >= 0 ||
                    name.indexOf("ws.ext.dirs") >= 0 ||
                    name.indexOf("java.class.path") >= 0 ||
                    name.indexOf("sun.boot.class.path") >= 0) {
                value = "";

                String classpath = rawValue == null ? "" : rawValue.toString();
                String[] arrClasspath = classpath.split(System.getProperty("path.separator"));
                for (int i = 0; i < arrClasspath.length; i++) {
                    value += arrClasspath[i] + System.getProperty("path.separator") + "<br/>\n";
                }
                value += "";
            } else if (
                    (name.toLowerCase().indexOf("password") >= 0) ||
                            (name.toLowerCase().indexOf("secret") >= 0)) {
                value = "***";
            } else if (rawValue == null) {
                value = "";
            } else if (rawValue.getClass().isArray()) {
                value = "[";
                for (int i = 0; i < Array.getLength(rawValue); i++) {
                    value += Array.get(rawValue, i) + ",<br/>\n";
                }
                value += "]";
            } else if (rawValue instanceof Iterable) {
                value = "[";
                for (Object o : (Iterable) rawValue) {
                    value += o + ",<br/>\n";
                }
                value += "]";
            } else {
                value = rawValue == null ? "" : rawValue.toString();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            value = "Exception formatting <code>" + rawValue.getClass() + "</code><br/><pre>" + sw + "</pre>";
        }
        return value;
    }
%>
<html>
<head>
    <title>MBean</title>
</head>
<body>
<h1>MBean</h1>
<%
    try {
        String name = request.getParameter("name");
        out.write("Server: " + InetAddress.getLocalHost() + ", date: "
                + new java.sql.Timestamp(System.currentTimeMillis()).toString() + "<br>");

        if (name == null || name.length() == 0) {
            out.print("<form>ObjectName<input name='name' /><input type='submit'/></form>");
        } else {

            List<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(null);
            for (MBeanServer mbeanServer : mbeanServers) {

                Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(new ObjectName(name), null);

                for (ObjectInstance objectInstance : objectInstances) {
                    ObjectName objectName = objectInstance.getObjectName();
                    MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);
                    MBeanAttributeInfo[] attributeInfos = mbeanInfo.getAttributes();
                    Arrays.sort(attributeInfos, new Comparator<MBeanAttributeInfo>() {
                        public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    out.println("<h1>" + objectName + "</h1>");
                    out.println("<table border='1'>");

                    out.println("<tr><th>Name</th><td>Value</td><td>Type</td><td>Description</td></tr>");
                    for (MBeanAttributeInfo attributeInfo : attributeInfos) {
                        out.println("<tr>");
                        out.println("<th>" + attributeInfo.getName() + "</th>");
                        out.println("<td>");
                        if (attributeInfo.isReadable()) {
                            try {
                                Object value = mbeanServer.getAttribute(objectName, attributeInfo.getName());
                                out.println(formatValue(attributeInfo.getName(), value));
                            } catch (Exception e) {
                                out.println("#" + e.toString() + "#");
                            }
                        } else {
                            out.println("#NOT_READABLE#");
                        }

                        out.println("</td>");
                        out.println("<td>" + attributeInfo.getType() + "</td>");
                        out.println("<td>" + attributeInfo.getDescription() + "</td>");
                        out.println("</tr>");
                    }
                    out.flush();
                    out.println("</table>");
                }

                out.println("Total mbeans count <b>" + objectInstances.size() + "</b>");
            }
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