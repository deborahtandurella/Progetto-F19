<%--
  Created by IntelliJ IDEA.
  User: Utente
  Date: 19/08/2019
  Time: 12:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <!-- Custom styles for this template -->
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
                <a class="navbar-brand" href="#"><strong>Caporetto Team</strong></a>

            </div>
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav">
                    <li><a href="#"><i class="fa fa-home"></i><strong> Home </strong><span class="sr-only">(current)</span></a></li>
                    <li>
                        <a href="#">
                            <i class="fa fa-refresh">
                                <span class="badge badge-primary">5</span>
                            </i>
                            <strong>Exchange</strong>
                        </a>
                    </li>

                </ul>
                <form class="navbar-form navbar-left">
                    <div class="form-group">
                        <input type="text" class="form-control" placeholder="Search Cards">
                        <!------  <input class="form-control mr-sm-2" type="text" placeholder="Search Cards "> -------->

                    </div>

                    <button type="submit" class="btn btn-success active">Search</button>
                    <!------<button class="btn btn-outline-success my-2 my-sm-0" type="submit">Search</button>------->

                </form>
                <ul class="nav navbar-nav navbar-right">
                    <li>
                        <a href="#">
                            <i class="fa fa-comment">
                                <span class="badge badge-danger">11</span>
                            </i>
                            <strong> Chat </strong>
                        </a>
                    </li>
                    <li>
                        <a href="#">
                            <i class="fa fa-question">
                            </i>
                            <strong> Help </strong>
                        </a>
                    </li>
                    <li>
                        <a href="../views/userprofile.jsp">
                            <i class="fa fa-user-circle-o">
                            </i>
                            <strong> Account </strong>
                        </a>
                    </li>
                    <li>
                        <a href="#">
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

<!-----SCRIPT OF THE SCROLL TO TOP-------->
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
<script src="../bootstrap-3.3.7/js/bootstrap.min.js"></script>
</body>
</html>