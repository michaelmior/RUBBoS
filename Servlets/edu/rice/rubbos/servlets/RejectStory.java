package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class RejectStory extends RubbosHttpServlet
{
  private ServletPrinter sp = null;
  private PreparedStatement stmt = null, stmt2=null;
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
    sp = new ServletPrinter(response, "RejectStory");
    
    
    conn = getConnection();
  
    String storyId;
   
    ResultSet rs = null, rs2=null;
    
    storyId =request.getParameter("storyId");
    
    if (storyId == null)
	{
	    sp.printHTML("RejectStory: <h3>You must provide a story identifier !<br></h3>");
	    return;
	}
    
    sp.printHTMLheader("RUBBoS: Story submission result");
    sp.printHTML("<center><h2>Story submission result:</h2></center><p>\n");
    
    try
    {
	stmt = conn.prepareStatement("SELECT id FROM submissions WHERE id="+storyId);
	rs = stmt.executeQuery();
    } 
    catch (Exception e)
    { 
      sp.printHTML("Failed to execute Query for RejectStory: " +e);
      closeConnection();
      return;
    }
    try 
	{
	    if (!rs.first()) 
		{
		    sp.printHTML("<h3>ERROR: Sorry, but this story does not exist.</h3><br>\n");
		    closeConnection();
		    return;
		}
	     // Delete entry from database
	    stmt2=conn.prepareStatement("DELETE FROM submissions WHERE id="+storyId);
	    rs2= stmt2.executeQuery();
    } 
    catch (Exception e) 
    {
      sp.printHTML("Exception rejecting story: " + e +"<br>");
      closeConnection();
    }

    sp.printHTML("The story has been successfully removed from the submissions database table<br>\n");
    
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
