package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class BrowseCategories extends RubbosHttpServlet
{
  private ServletPrinter sp = null;
  private PreparedStatement stmt = null;
  private Connection conn = null;

  public int getPoolSize()
  {
    return Config.BrowseCategoriesPoolSize;
  }

  private void closeConnection()
  {
    try 
    {
      if (stmt != null) stmt.close();	// close statement
    } 
    catch (Exception ignore) 
    {
    }
  }


  /** Build the html page for the response */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    sp = new ServletPrinter(response, "BrowseCategories");
    sp.printHTMLheader("RUBBoS available categories");
    
    conn = getConnection();
  

    ResultSet rs = null;
    try
    {
	stmt = conn.prepareStatement("SELECT * FROM categories");
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for BrowseCategories: " +e);
      closeConnection();
      return;
    }
    try 
    {
      if (!rs.first()) 
      {
        sp.printHTML("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
        closeConnection();
        return;
      }
      else
	  sp.printHTML("<h2>Currently available categories</h2><br>\n");
	  
      int    categoryId;
      String categoryName;

      do
      {
        categoryId = rs.getInt("id");
        categoryName = rs.getString("name");
	sp.printHTMLHighlighted("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.BrowseStoriesByCategory?category="+categoryId+"&categoryName="+URLEncoder.encode(categoryName)+"\">"+ categoryName+"</a><br>\n");
      }
      while (rs.next());
    } 
    catch (Exception e) 
    {
      sp.printHTML("Exception getting categories: " + e +"<br>");
      closeConnection();
    }

    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
