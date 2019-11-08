package servlets;

import collection.Card;
import collection.CollectionOwn;
import dao.CardsDao;
import dao.CardsDaoImpl;
import platform.Platform;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@WebServlet(name = "SearchServlet", urlPatterns = "/Search")
public class SearchServlet extends AbstractServlet {
    private String DEFAULT_ROUTE = "/views/userprofile.jsp";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {



    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.getSession().setAttribute("filter", null);
            ArrayList<Card> filter = searchFilter(request);

            if(filter!= null){
                request.getSession().setAttribute("filter",filter);
                response.sendRedirect(request.getContextPath()+DEFAULT_ROUTE);
            }
            else{
                forwardTo(request, response, INDEXPROFILE_ROUTE);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private  ArrayList<Card> searchFilter(HttpServletRequest request) throws SQLException {
        String filter = request.getParameter("filtersearch");
        String search = request.getParameter("Filter");
        CollectionOwn logged= (CollectionOwn) request.getSession().getAttribute("logged");
        String username= logged.getOwner().getUsername();
        Platform platform = Platform.getInstance();
        switch (filter){
            case "Category":
                return platform.findByCategory(username,search);

            case "Class":
                return platform.findByClass(username,search);

            case "Type":
                return platform.findByType(username, search);
        }
        return null;
    }

}


