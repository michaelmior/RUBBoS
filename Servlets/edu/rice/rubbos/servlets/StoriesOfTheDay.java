package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;


public class StoriesOfTheDay extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "StoriesOfTheDay");
    sp.printHTMLheader("RUBBoS stories of the day");
    
    conn = getConnection();

    int bodySizeLimit = 512;

    ResultSet rs = null;
    try
    {
      stmt = conn.prepareStatement("SELECT * FROM stories ORDER BY date DESC LIMIT 10");
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for stories of the day: " +e);
      closeConnection();
      return;
    }
    try 
    {
      if (!rs.first()) 
      {
        sp.printHTML("<h2>Sorry, but there is no story available at this time.</h2><br>\n");
        closeConnection();
        return;
      }
  
      int    storyId;
      String storyTitle;
      int    writerId;
      String userName;
      String date;
      String body;
      do
      {
	sp.printHTML("<br><hr>\n");
        storyId = rs.getInt("id");
        storyTitle = rs.getString("title");
	sp.printHTMLHighlighted("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewStory?storyId="+storyId+"\">"+storyTitle+"</a>");

	writerId = rs.getInt("writer");
	userName = sp.getUserName(writerId, conn);
	date = rs.getString("date");
	sp.printHTML("<B>Posted by "+userName+" on "+date+"</B><br>\n");
	body = rs.getString("body");
	if (body.length()  > bodySizeLimit)
	{
	  sp.printHTML(body.substring(0, bodySizeLimit));
	  sp.printHTML("<br><B>...</B>");
        }
        else
	  sp.printHTML(body);
	sp.printHTML("<br>\n");
      }
      while (rs.next());
    } 
    catch (Exception e) 
    {
      sp.printHTML("Exception getting stories of the day: " + e +"<br>");
      closeConnection();
    }

    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
