<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ page session="false"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Cocktail - ${cocktail.name}</title>

<link rel="shortcut icon" href="${cdnUrl}${pageContext.request.contextPath}/img/favicon.ico">
<link rel="icon" type="image/png" href="${cdnUrl}${pageContext.request.contextPath}/img/favicon.png">

<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
<!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <link href="//current.bootstrapcdn.com/bootstrap-v204/css/bootstrap-combined.min.css" media="screen" rel="stylesheet" type="text/css" />
    <link href="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/themes/base/jquery-ui.css" rel="Stylesheet" type="text/css" />

    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript" ></script>
    <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js" type="text/javascript" ></script>
    <script src="//current.bootstrapcdn.com/bootstrap-v204/js/bootstrap.min.js" type="text/javascript"></script>
    <script type="text/javascript">
    $(document).ready(function() {
        $("input#searchCocktailByName").autocomplete({
            minLength : 2,
            source : "${pageContext.request.contextPath}/cocktail/suggest/name"
        });
        $("input#searchCocktailByIngredient").autocomplete({
            minLength : 2,
            source : "${pageContext.request.contextPath}/cocktail/suggest/ingredient"
        });
    });
</script>
</head>
<body>

    <div class="navbar">
        <div class="navbar-inner">
            <div class="container">
                <a class="brand" href="${pageContext.request.contextPath}/"> <img alt='Devoxx France Logo' height='28'
                    src='${pageContext.request.contextPath}/img/devoxx-france-logo.jpg' width='54' /> Devoxx Cocktails
                </a>
                <ul class="nav">
                    <li><a href="${pageContext.request.contextPath}/">Home</a></li>
                    <li class="active"><a href="${pageContext.request.contextPath}/cocktail/">Cocktails</a></li>
                </ul>
                <form class="navbar-search pull-left" action="${pageContext.request.contextPath}/cocktail/">
                    <input id="searchCocktailByName" name="name" type="text" class="search-query input-medium" placeholder="Search by name">
                </form>
                <form class="navbar-search pull-left" action="${pageContext.request.contextPath}/cocktail/">
                    <input id="searchCocktailByIngredient" name="ingredient" type="text" class="search-query input-medium"
                        placeholder="Search by ingredient">
                </form>
            </div>
        </div>
    </div>

    <div class="container">
        <div class="page-header">
            <h1>
                ${cocktail.name} <a href="${pageContext.request.contextPath}/cocktail/${cocktail.id}/edit-form" class="btn js-btn"><i
                    class="icon-edit"></i> Edit</a>
            </h1>
        </div>

        <div class="row">
            <div class="span2">
                <c:if test="${not empty cocktail.photoUrl}">
                    <img src="${cocktail.photoUrl}" width="100" />
                </c:if>
            </div>
            <div class="span4">
                <h2>Instructions</h2>
                <p>${cocktail.instructionsAsHtml}</p>
            </div>
            <div class="span4">
                <h2>Ingredients</h2>
                <ul>
                    <c:forEach items="${cocktail.ingredients}" var="ingredient">
                        <li>${ingredient.quantity} ${ingredient.name}</li>
                    </c:forEach>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="span8 offset2">
                <em><a href="${cocktail.sourceUrl}" target="_blank">${cocktail.sourceUrl}</a></em>
            </div>
        </div>
        <div class="row">
            <div class="span8 offset2">
                <hr />
                <h4>Comments</h4>
                <c:forEach items="${cocktail.comments}" var="comment">
                    <em>"${comment}"</em>
                    <br />
                </c:forEach>
                <form action="${pageContext.request.contextPath}/cocktail/${id}/comment" method="post" class="form-inline">
                    <fieldset>
                        <input id="comment" name="comment" type="text" placeholder="Add a comment..." />
                        <button type="submit" class="btn js-btn">
                            <i class="icon-comment"></i> Comment
                        </button>
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
</body>
</html>
