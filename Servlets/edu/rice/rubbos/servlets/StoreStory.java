package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

/** Builds the html page with the list of all categories and provides links to browse all
    items in a category or items in a category for a given region */
public class StoreStory extends RubbosHttpServlet
{
  private ServletPrinter sp = null;
  private PreparedStatement stmt = null, stmt2 = null;
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
    
    sp = new ServletPrinter(response, "StoreStory");
    

        
    String categoryName, nickname, title, body, category, table;
    String password= null;
    int  userId, access;
    ResultSet rs = null, rs2 = null;
    
    nickname = request.getParameter("nickname");
    password = request.getParameter("password");
    title = request.getParameter("title");
    body = request.getParameter("body");
    category = request.getParameter("category");
    
    conn = getConnection();

    if (title == null)
      {
         sp.printHTML("You must provide a story title!<br>");
         return;
      }

    if (body == null)
      {
	 sp.printHTML("<h3>You must provide a story body!<br></h3>");
         return;
      }

    if (category == null)
      {
	 sp.printHTML("<h3>You must provide a category!<br></h3>");
         return;
      }

    sp.printHTMLheader("RUBBoS: Story submission result");

    sp.printHTML("<center><h2>Story submission result:</h2></center><p>\n");
    
    //Authenticate the user
    userId = 0;
    access = 0;

    if ((nickname != null) && (password != null))
	{
	    try
		{
		    stmt = conn.prepareStatement("SELECT id,access FROM users WHERE nickname=\""+ nickname + "\" AND password=\"" + password + "\"");
		    rs = stmt.executeQuery();
		}
	    catch (Exception e)
		{
		    sp.printHTML("ERROR: Authentification query failed" +e);
		    closeConnection();
		    return;
		}
	    try 
		{
		    if (rs.first()) 
			{
			    userId = rs.getInt("id");
			    access = rs.getInt("access");
			}
	      
		} 
	    catch (Exception e) 
		{
		    sp.printHTML("Exception storing story " + e +"<br>");
		    closeConnection();
		}
	}


    table = "submissions";
    if (userId == 0)
      sp.printHTML("Story stored by the 'Anonymous Coward'<br>\n");
    else
    {
      if (access == 0)
        sp.printHTML("Story submitted by regular user " + userId+ "<br>\n");
      else
      {
        sp.printHTML("Story posted by author " +userId+ "<br>\n");
        table = "stories";
      }
    }

    // Add story to database

    
    
       
      try
      {
	  stmt2 = conn.prepareStatement("INSERT INTO "+ table + " VALUES (NULL, \"" + title + "\", \""+ body + "\", NOW(), \"" +  userId + "\", "+ category+")");
        
        rs2 = stmt2.executeQuery();
        if (!rs.first())
        {
          sp.printHTML(" ERROR: Failed to insert new story in database.");
          closeConnection();
          return ;
        }
           }
      catch (SQLException e)
      {
        sp.printHTML("Failed to execute Query for StoreStory: " +e);
        closeConnection();
        return;
      }
    
    
    sp.printHTML("Your story has been successfully stored in the " + table + " database table<br>\n");

    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
