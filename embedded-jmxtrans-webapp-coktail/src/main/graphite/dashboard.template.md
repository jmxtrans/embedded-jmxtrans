Cocktail App Dashboards - ${environment} - ${version}

# Website Traffic

<table>
<tr>
    <td>
        <img src="${graphite.baseUrl}/render/?width=400&height=300&from=-5days&lineWidth=3&target=alias(sumSeries(edu.servers.*.website.visitors.activeGauge)%2C%22Active%20Visitors%22)&target=alias(sumSeries(timeShift(edu.servers.*.website.visitors.activeGauge%2C%227d%22))%2C%22Active%20Visitors%20-7days%22)&xFormat=%25a%20%25H%25p&title=Active%20Visitors">
        <br/>
        <a href="${graphite.baseUrl}/render/?width=400&height=300&from=-5days&lineWidth=3&target=alias(sumSeries(edu.servers.*.website.visitors.activeGauge)%2C%22Active%20Visitors%22)&target=alias(sumSeries(timeShift(edu.servers.*.website.visitors.activeGauge%2C%227d%22))%2C%22Active%20Visitors%20-7days%22)&xFormat=%25a%20%25H%25p&title=Active%20Visitors&format=csv">
            <img src="img/csv-file-32.png"> Active Visitors Report
        </a>
    </td>
    <td>
        <img src=${graphite.baseUrl}/render/?width=400&height=300&from=-5days&xFormat=%25a%20%25d%25p&lineWidth=3&target=alias(summarize(sumSeries(nonNegativeDerivative(edu.servers.*.website.visitors.newVisitorsCounter))%2C%221h%22)%2C%22New%20visitors%20per%20Hour%22)&target=alias(summarize(sumSeries(nonNegativeDerivative(timeShift(edu.servers.*.website.visitors.newVisitorsCounter%2C%227d%22)))%2C%221h%22)%2C%22New%20visitors%20per%20Hour%20-7days%22)&title=New%20Visitors" >
        <br/>
        <a href=${graphite.baseUrl}/render/?width=400&height=300&from=-5days&xFormat=%25a%20%25d%25p&lineWidth=3&target=alias(summarize(sumSeries(nonNegativeDerivative(edu.servers.*.website.visitors.newVisitorsCounter))%2C%221h%22)%2C%22New%20visitors%20per%20Hour%22)&target=alias(summarize(sumSeries(nonNegativeDerivative(timeShift(edu.servers.*.website.visitors.newVisitorsCounter%2C%227d%22)))%2C%221h%22)%2C%22New%20visitors%20per%20Hour%20-7days%22)&title=New%20Visitors&format=csv">
            <img src="img/csv-file-32.png"> New Visitors Report
        </a>
    </td>
<tr>
</table>

# Sales

<table>
<tr>
    <td>
        <img src="${graphite.baseUrl}/render/?width=400&height=300&title=Revenue&xFormat=%25a%20%25d%25p&vtitle=USD&lineWidth=3&from=-5days&target=alias(scale(summarize(sumSeries(nonNegativeDerivative(edu.servers.*.sales.revenueInCentsCounter))%2C%221h%22)%2C0.01)%2C%22Revenue%20per%20Hour%22)&target=alias(scale(summarize(sumSeries(nonNegativeDerivative(timeShift(edu.servers.*.sales.revenueInCentsCounter%2C%227d%22)))%2C%221h%22)%2C0.01)%2C%22Revenue%20per%20Hour%20-7days%22)">
        <br/>
        <a href="${graphite.baseUrl}/render/?width=400&height=300&title=Revenue&xFormat=%25a%20%25d%25p&vtitle=USD&lineWidth=3&from=-5days&target=alias(scale(summarize(sumSeries(nonNegativeDerivative(edu.servers.*.sales.revenueInCentsCounter))%2C%221h%22)%2C0.01)%2C%22Revenue%20per%20Hour%22)&target=alias(scale(summarize(sumSeries(nonNegativeDerivative(timeShift(edu.servers.*.sales.revenueInCentsCounter%2C%227d%22)))%2C%221h%22)%2C0.01)%2C%22Revenue%20per%20Hour%20-7days%22)&format=csv">
            <img src="img/csv-file-32.png"> Revenue
        </a>
    </td>
    <td>
        <img src="${graphite.baseUrl}/render/?width=400&height=300&title=Cocktails%20Sold&xFormat=%25a%20%25d%25p&lineWidth=3&from=-5days&target=alias(summarize(sumSeries(nonNegativeDerivative(edu.servers.*.sales.itemsCounter))%2C%221h%22)%2C%22Cocktails%20sold%20per%20Hour%22)&target=alias(summarize(sumSeries(nonNegativeDerivative(timeShift(edu.servers.*.sales.itemsCounter%2C%227d%22)))%2C%221h%22)%2C%22Cocktails%20sold%20per%20Hour%20-7days%22)">
        <br/>
        <a href="${graphite.baseUrl}/render/?width=400&height=300&title=Cocktails%20Sales&xFormat=%25a%20%25d%25p&lineWidth=3&from=-5days&target=alias(summarize(sumSeries(nonNegativeDerivative(edu.servers.*.sales.itemsCounter))%2C%221h%22)%2C%22Cocktails%20sold%20per%20Hour%22)&target=alias(summarize(sumSeries(nonNegativeDerivative(timeShift(edu.servers.*.sales.itemsCounter%2C%227d%22)))%2C%221h%22)%2C%22Cocktails%20sold%20per%20Hour%20-7days%22)&format=csv">
            <img src="img/csv-file-32.png"> Cocktail Sales
        </a>
    </td>
<tr>
</table>