<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>embedded-jmxtrans sos cocktail demo</title>

    <link rel="shortcut icon" href=${pageContext.request.contextPath}/favicon.ico">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.0/css/bootstrap-combined.min.css" rel="stylesheet">
    <link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
    <link href="//code.jquery.com/ui/1.10.1/themes/base/jquery-ui.css" rel="Stylesheet" type="text/css"/>

    <script src="//code.jquery.com/jquery-1.9.1.min.js"></script>
    <script src="//code.jquery.com/ui/1.10.1/jquery-ui.js" type="text/javascript"></script>
    <script src="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.0/js/bootstrap.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            $("input#searchCocktailByName").autocomplete({
                minLength: 2,
                source: "${pageContext.request.contextPath}/cocktail/suggest/name"
            });
            $("input#searchCocktailByIngredient").autocomplete({
                minLength: 2,
                source: "${pageContext.request.contextPath}/cocktail/suggest/ingredient"
            });
        });
    </script>
<body>
<div class="navbar">
    <div class="navbar-inner">
        <div class="container">
            <div class="span9">
                <a class="brand" style="padding: 0; padding-top: 10px; padding-right: 5px" href="${pageContext.request.contextPath}/"> <img alt='jmxtrans logo' height='28'
                                                                                  src='${pageContext.request.contextPath}/img/jmxtrans-logo-28x109.gif'
                                                                                  width='109'/> SOS Cocktail
                </a>
                <ul class="nav">
                    <li class="active"><a href="${pageContext.request.contextPath}/">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/cocktail/">Cocktails</a></li>
                </ul>
                <form class="navbar-search pull-left" action="${pageContext.request.contextPath}/cocktail/">
                    <input id="searchCocktailByName" name="name" type="text" class="search-query input-medium"
                           placeholder="Search by name">
                </form>
                <form class="navbar-search pull-left" action="${pageContext.request.contextPath}/cocktail/">
                    <input id="searchCocktailByIngredient" name="ingredient" type="text"
                           class="search-query input-medium"
                           placeholder="Search by ingredient">
                </form>
            </div>
            <div class="span3 pull-right">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/cart/" title="Shopping Cart">
                    <i class="icon-shopping-cart"></i>
                    ${shoppingCart.itemsCount} items
                    ${shoppingCart.prettyPrice}
                </a>
            </div>
        </div>
    </div>
</div>

<div class="container">

    <div class="row">
        <div class="span6">
            <div class="hero-unit">
                <h2>Enter the cocktail zone!</h2>

                <p>The best recipes just for you.</p>

                <p>
                    <a class="btn btn-primary btn-large" href="${pageContext.request.contextPath}/cocktail/"> Choose your
                        cocktails! </a>
                </p>
            </div>
        </div>
        <div class="span6">
            <div class="hero-unit">
                <h2><img src="${pageContext.request.contextPath}/img/hosted-graphite-logo-small.png" width="25px"
                         height="28px"/>cocktails metrics</h2>
                <img src="https://www.hostedgraphite.com/f74333af/5d5e150f-ab89-4b1d-801a-da65e6670544/graphite/render/?width=588&height=309&_salt=1358386171.081&title=Cocktails%20Total&target=alias(sumSeries(servers.*.cocktail.AddedCommentCount)%2C%22Comments%22)&target=alias(sumSeries(servers.*.cocktail.DisplayedHomeCount)%2C%22Displayed%20Home%22)&target=alias(sumSeries(servers.*.cocktail.DisplayedCocktailCount)%2C%22Displayed%20Cocktails%22)&target=alias(sumSeries(servers.*.cocktail.SearchedCocktailCount)%2C%22Searched%20Cocktails%22)"/>
                <em>Powered by <a href="http://www.hostedgraphite.com/">Hosted Graphite</a> </em>
            </div>
        </div>
    </div>


    <div class="row">
        <div class="span6">
            <div class="hero-unit">

            </div>
        </div>
        <div class="span6">
            <div class="hero-unit">
                <h2><img src="${pageContext.request.contextPath}/img/hosted-graphite-logo-small.png" width="25px"
                         height="28px"/>embedded-jmxtrans internal metrics</h2>
                <img src="http://www.hostedgraphite.com/f74333af/5d5e150f-ab89-4b1d-801a-da65e6670544/graphite/render/?width=588&height=309&_salt=1358346534.26&from=-60minutes&title=embedded-jmxtrans%20internal%20metrics&target=legendValue(alias(summarize(nonNegativeDerivative(sumSeries(servers.*.jmxtrans.jmxtrans.CollectedMetricsCount))%2C%221min%22)%2C%22Collected%20Metrics%20%2Fmin%22)%2C%22last%22)&target=legendValue(alias(summarize(nonNegativeDerivative(scale(sumSeries(servers.*.jmxtrans.jmxtrans.CollectionDurationInNanos)%2C0.000001)%2C5)%2C%221min%22)%2C%22Collection%20duration%20in%20micros%20%2Fmin%22)%2C%22last%22)&target=legendValue(alias(summarize(nonNegativeDerivative(scale(sumSeries(servers.*.jmxtrans.jmxtrans.ExportDurationInNanos)%2C0.000001)%2C5)%2C%221min%22)%2C%22Export%20duration%20in%20micros%20%2Fmin%22)%2C%22last%22)">
                <em>Powered by <a href="http://www.hostedgraphite.com/">Hosted Graphite</a> </em>
            </div>
        </div>
    </div>

</div>
<a href="https://github.com/jmxtrans/embedded-jmxtrans-samples/tree/master/embedded-jmxtrans-webapp-coktail"><img
        style="position: absolute; top: 0; right: 0; border: 0;"
        src="https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png" alt="Fork me on GitHub"></a>
</body>
</html>