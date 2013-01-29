<%@ page session="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>embedded-jmxtrans cocktail demo</title>

    <link rel="shortcut icon" href=${pageContext.request.contextPath}/img/favicon.ico
    ">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/img/favicon.png">

    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <link href="//current.bootstrapcdn.com/bootstrap-v204/css/bootstrap-combined.min.css" media="screen"
          rel="stylesheet" type="text/css"/>
    <link href="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/themes/base/jquery-ui.css" rel="Stylesheet"
          type="text/css"/>

    <!-- Le javascript -->
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
    <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js" type="text/javascript"></script>
    <script src="//current.bootstrapcdn.com/bootstrap-v204/js/bootstrap.min.js" type="text/javascript"></script>
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
            <a class="brand" href="${pageContext.request.contextPath}/"> <img alt='jmxtrans logo' height='28'
                                                                              src='${pageContext.request.contextPath}/img/jmxtrans-logo-28x109.gif'
                                                                              width='109'/> Jmxtrans Demo
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
                <input id="searchCocktailByIngredient" name="ingredient" type="text" class="search-query input-medium"
                       placeholder="Search by ingredient">
            </form>
        </div>
    </div>
</div>

<div class="container">

    <div class="row">
        <div class="span12">
            <div class="alert alert-info">
                Source code available <a
                    href="https://github.com/jmxtrans/embedded-jmxtrans-samples/tree/master/embedded-jmxtrans-webapp-coktail">here</a>!
            </div>
        </div>
    </div>
    <div class="row">
        <div class="span6">
            <div class="hero-unit">
                <h2>A to Z cocktails list</h2>

                <p>A to Z list of cocktail recipes.</p>

                <p>
                    <a class="btn btn-primary btn-large" href="${pageContext.request.contextPath}/cocktail/"> Visit our
                        cocktails! </a>
                </p>
            </div>
        </div>
        <div class="span6">
            <div class="hero-unit">
                <h2><img src="${pageContext.request.contextPath}/img/hosted-graphite-logo-small.png" width="25px"
                         height="28px"/>cocktails metrics</h2>
                <img src="https://www.hostedgraphite.com/a8592969/90bf2733-71b5-4ed5-8e4c-a57236f4e3ed/graphite/render/?width=588&height=309&_salt=1358386171.081&title=Cocktails%20Total&target=alias(sumSeries(servers.*.cocktail.AddedCommentCount)%2C%22Comments%22)&target=alias(sumSeries(servers.*.cocktail.DisplayedHomeCount)%2C%22Displayed%20Home%22)&target=alias(sumSeries(servers.*.cocktail.DisplayedCocktailCount)%2C%22Displayed%20Cocktails%22)&target=alias(sumSeries(servers.*.cocktail.SearchedCocktailCount)%2C%22Searched%20Cocktails%22)"/>
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
                <img src="http://www.hostedgraphite.com/a8592969/90bf2733-71b5-4ed5-8e4c-a57236f4e3ed/graphite/render/?width=588&height=309&_salt=1358346534.26&from=-60minutes&title=embedded-jmxtrans%20internal%20metrics&target=legendValue(alias(summarize(nonNegativeDerivative(sumSeries(servers.*.jmxtrans.jmxtrans.CollectedMetricsCount))%2C%221min%22)%2C%22Collected%20Metrics%20%2Fmin%22)%2C%22last%22)&target=legendValue(alias(summarize(nonNegativeDerivative(scale(sumSeries(servers.*.jmxtrans.jmxtrans.CollectionDurationInNanos)%2C0.000001)%2C5)%2C%221min%22)%2C%22Collection%20duration%20in%20micros%20%2Fmin%22)%2C%22last%22)&target=legendValue(alias(summarize(nonNegativeDerivative(scale(sumSeries(servers.*.jmxtrans.jmxtrans.ExportDurationInNanos)%2C0.000001)%2C5)%2C%221min%22)%2C%22Export%20duration%20in%20micros%20%2Fmin%22)%2C%22last%22)">
                <em>Powered by <a href="http://www.hostedgraphite.com/">Hosted Graphite</a> </em>
            </div>
        </div>
    </div>

</div>

</body>
</html>