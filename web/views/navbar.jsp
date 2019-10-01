<!-- NAVIGATION BAR : this page contains the top navigation bar with links to Home, Reservations and Catalogue ;
it is included in each page of the application -->

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <!-- Custom styles for this template -->
    <meta charset="UTF-8">
    <link href="../bootstrap-3.3.7/css/bootstrap.min.css" rel="stylesheet" id="bootstrap-css">
    <link rel="stylesheet" href="../stylesheets/navbar.css">
</head>
<body>
<!-------- NAVBAR------->
<div class="container">
    <nav class="navbar navbar-icon-top  navbar-default  navbar-fixed-top ">
        <div class="container-fluid">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" ><strong>CAPORETTO TEAM!</strong></a>

            </div>
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav">
                    <li><a href="../views/homepage.jsp"><i class="fa fa-home"></i><strong> Home </strong><span class="sr-only">(current)</span></a></li>
                    <li>
                        <a href="../views/exchange.jsp">
                            <i class="fa fa-refresh"></i>
                            <strong>Exchange</strong>
                        </a>
                    </li>

                </ul>
                <form class="navbar-form navbar-left" action="../homepage" method="get" name="search-frm" id="search_frm">
                    <div class="form-group">
                        <input type="text" class="form-control" placeholder="Search Cards">
                        <!------  <input class="form-control mr-sm-2" type="text" placeholder="Search Cards "> -------->

                    </div>

                    <button type="submit" class="btn btn-success active">Search</button>

                </form>
                <ul class="nav navbar-nav navbar-right">
                    <li>
                        <a href="#">
                            <i class="fa fa-question">
                            </i>
                            <strong> Help </strong>
                        </a>
                    </li>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="true">
                            <i class="fa fa-user-circle-o"></i>
                            <strong> Account </strong> <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a href="../views/userprofile.jsp" ><strong> Profile</strong></a></li>
                            <li role="separator" class="divider"></li>
                            <li><a href="../views/login.jsp">
                                <i class="glyphicon glyphicon-log-out">
                                </i>
                                <strong> Logout </strong>
                            </a>
                            </li>
                </ul>
            </div><!-- /.navbar-collapse -->
        </div><!-- /.container-fluid -->
    </nav>
</div><!------END NAVBAR------->

<!-----SCROLL-TO-TOP------>
<button onclick="topFunction()" id="myBtn" title="Go to top"><span class="glyphicon glyphicon-upload" ></span></button>
<!------END SCROLL-TO-TOP------->

<!-----SCRIPT OF THE NAVBAR AND SCROLL TO TOP-------->

<script src="../bootstrap-3.3.7/js/bootstrap.min.js"></script>

<script>
    // When the user scrolls down 20px from the top of the document, show the button
    window.onscroll = function() {scrollFunction()};

    function scrollFunction() {
        if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
            document.getElementById("myBtn").style.display = "block";
        } else {
            document.getElementById("myBtn").style.display = "none";
        }
    }

    // When the user clicks on the button, scroll to the top of the document
    function topFunction() {
        document.documentElement.scrollTop = 0;
    }
</script>

</body>
</html>
<!-- jQuery CDN - Slim version (=without AJAX) -->
<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
<!-- Popper.JS -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.0/umd/popper.min.js" integrity="sha384-cs/chFZiN24E4KMATLdqdvsezGxaGsi4hLGOzlXwp5UZB1LY//20VyM2taTB4QvJ" crossorigin="anonymous"></script>
<script src="../bootstrap-3.3.7/js/bootstrap.js"></script>
</body>
</html>
