<%@ page contentType="text/plain;charset=ISO-8859-1" import="javax.management.MBeanServer,javax.management.MBeanServerFactory,javax.management.ObjectInstance"%><%@ page import="javax.management.ObjectName"%><%@ page import="java.util.Set"%><%

    // get the mbean server
    MBeanServer mbeanServer = MBeanServerFactory.findMBeanServer(null).get(0);

    // get the mbeans
    Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(new ObjectName("cocktail:type=ShoppingCartController,name=ShoppingCartController"), null);

    // render the mbeans
    for (ObjectInstance objectInstance : objectInstances) {
        ObjectName objectName = objectInstance.getObjectName();

    %>{
    "Epoch":<%=(System.currentTimeMillis() / 1000)%>,
    "SalesRevenueInCentsCounter":<%= mbeanServer.getAttribute(objectName, "SalesRevenueInCentsCounter") %>,
    "SalesItemsCounter":<%= mbeanServer.getAttribute(objectName, "SalesItemsCounter") %>,
    "SalesOrdersCounter":<%= mbeanServer.getAttribute(objectName, "SalesOrdersCounter") %>
}<% } %>
