package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;


public class AcceptStory extends RubbosHttpServlet
{
  private ServletPrinter sp = null;
  private PreparedStatement stmt = null;
  private PreparedStatement stmtdel = null;
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
    sp = new ServletPrinter(response, "AcceptStory");
    sp.printHTMLheader("RUBBoS: Story submission result");
    sp.printHTML("<center><h2>Story submission result:</h2></center><p>\n");

    conn = getConnection();

    int storyId = (Integer.valueOf(request.getParameter("storyId"))).intValue();


   
    if (storyId == 0)
    {
      sp.printHTML( "<h3>You must provide a story identifier !<br></h3>");
      return;
    }
    


    ResultSet rs = null, rs2 = null;

    try
    {
      stmt = conn.prepareStatement("SELECT * FROM submissions WHERE id= storyId");
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML(" Failed to execute Query for AcceptStory: " +e);
      closeConnection();
      return;
    }
    try 
	{
	    if (!rs.first()) 
		{
		    sp.printHTML("<h3>ERROR: Sorry, but this story does not exist.</h3><br>");
		    closeConnection();
		    return;
		}
  
	    //Add story to database
	    String categoryTitle = rs.getString("title");
	    int categoryDate = rs.getInt("date");
	    String categoryBody = rs.getString("body");
	    String categoryWriter = rs.getString("writer");
	    String category = rs.getString("category");
	    
	    stmt = conn.prepareStatement("INSERT INTO stories VALUES (NULL, \""+ categoryTitle +"\", \""+categoryBody+"\", '"+categoryDate+"', "+categoryWriter+", "+category+")");
	    stmtdel = conn.prepareStatement("DELETE FROM submissions WHERE id=storyId" );

	    rs = stmt.executeQuery();
	    rs2= stmtdel.executeQuery();
	} 
    catch (Exception e) 
	{
	    sp.printHTML("Exception accepting stories: " + e +"<br>");
	    closeConnection();
	}

    sp.printHTML("The story has been successfully moved from the submission to the stories database table<br>\n");
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
