package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;


public class Author extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "Author");
    
    conn = getConnection();

    // int storyId = (Integer.valueOf(request.getParameter("storyId"))).intValue();


   
/*    if (storyId == 0)
    {
      sp.printHTML( "<h3>You must provide a story identifier !<br></h3>");
      return;
      }  */
    

    String nickname, password;
    int userId = 0, access=0;
    ResultSet rs = null, rs2 = null;

    nickname = request.getParameter("nickname");
    password = request.getParameter("password");

    if (nickname == null)
	{
	    sp.printHTML("Author: You must provide a nick name!<br>");
	    return;
	}

    if (password == null)
	{
	    sp.printHTML("Author: You must provide a password!<br>");
	    return;
	}

   
    if ((nickname != null) && (password != null))
	{

	    try
		{
		    stmt = conn.prepareStatement("SELECT id,access FROM users WHERE nickname=\"" + nickname + "\" AND password=\"" + password + "\"");
		    rs = stmt.executeQuery();
		}
	    catch (Exception e)
		{
		    sp.printHTML(" Failed to execute Query for Author: " +e);
		    closeConnection();
		    return;
		}
	    try 
		{
		    if (rs.first()) 
			{
			    userId= rs.getInt("id");
			    access=rs.getInt("access");			    
			}
  		} 
	    catch (Exception e) 
		{
		    sp.printHTML("Exception verifying author: " + e +"<br>");
		    closeConnection();
		}
	}



    if ((userId == 0) || (access == 0))
	{
	    sp.printHTMLheader("RUBBoS: Author page");
	    sp.printHTML("<p><center><h2>Sorry, but this feature is only accessible by users with an author access.</h2></center><p>\n");
	}
    else
	{
	    sp.printHTMLheader("RUBBoS: Author page");
	    sp.printHTML("<p><center><h2>Which administrative task do you want to do ?</h2></center>\n" +
			 "<p><p><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ReviewStories?authorId= \"" + userId+ "\"\">Review submitted stories</a><br>\n");
	}
    sp.printHTMLfooter();

  }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	doGet(request, response);
    }

}
