package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class ReviewStories extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "ReviewStories");
    
    
    conn = getConnection();
  
    String date, title, id, body, username;
   
    ResultSet rs = null;
    
    sp.printHTMLheader("RUBBoS: Review Stories");
    
    try
    {
	stmt = conn.prepareStatement("SELECT * FROM submissions ORDER BY date DESC LIMIT 10");
	rs = stmt.executeQuery();
    } 
    catch (Exception e)
    { 
      sp.printHTML("Failed to execute Query for ReviewStories " +e);
      closeConnection();
      return;
    }
    try 
	{
	    if (!rs.first()) 
		{
		    sp.printHTML("<h2>Sorry, but there is no submitted story available at this time.</h2><br>\n");
		    closeConnection();
		    return;
		}
	    do
		{
		    title=rs.getString("title");
		    date=rs.getString("date");
		    id=rs.getString("id");
		    body=rs.getString("body");
		    
		    sp.printHTML("<br><hr>\n");
		    sp.printHTMLHighlighted(title);
		    username = rs.getString("writer");
		    sp.printHTML("<B>Posted by "+username+" on "+date+"</B><br>\n");
		    sp.printHTML(body);
		    sp.printHTML("<br><p><center><B>[ <a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.AcceptStory?storyId="+id+"\">Accept</a> | <a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.RejectStory?storyId="+ id+"\">Reject</a> ]</B><p>\n");
		} while (rs.next());
    } 
    catch (Exception e) 
    {
      sp.printHTML("Exception rejecting story: " + e +"<br>");
      closeConnection();
    }

       
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
