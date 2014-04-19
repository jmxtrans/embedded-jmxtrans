<%@ page import="javax.management.MBeanServer" %>
<%@ page import="javax.management.MBeanServerFactory,javax.management.ObjectInstance" %>
<%@ page import="javax.management.ObjectName" %>
<%@ page import="java.util.Set" %>
<html>
<head>
    <title>Sales Metrics</title>
</head>
<body>
<h1>Sales Metrics</h1>
<table border='1'>
    <tr>
        <th>SalesRevenueInCentsCounter</th>
        <th>SalesItemsCounter</th>
        <th>SalesOrdersCounter</th>
    </tr>
    <%
        // get the mbean server
        MBeanServer mbeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
        // get the mbeans
        Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(new ObjectName("cocktail:type=ShoppingCartController,name=ShoppingCartController"), null);
        // render the mbeans
        for (ObjectInstance objectInstance : objectInstances) {
            ObjectName objectName = objectInstance.getObjectName();
    %>
    <tr>
        <td><%=mbeanServer.getAttribute(objectName, "SalesRevenueInCentsCounter")%></td>
        <td><%=mbeanServer.getAttribute(objectName, "SalesItemsCounter")%></td>
        <td><%=mbeanServer.getAttribute(objectName, "SalesOrdersCounter")%></td>
    </tr>
    <%
        }
    %>
</table>
</body>
</html>