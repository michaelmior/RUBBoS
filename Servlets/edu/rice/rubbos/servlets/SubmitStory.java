package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;


public class SubmitStory extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "Submit Story");
    sp.printHTMLheader("RUBBoS: Story submission");
    sp.printHTML("<center><h2>Submit your incredible story !</h2><br>\n");
    sp.printHTML("<form action=\"/servlet/edu.rice.rubbos.servlets.StoreStory\" method=POST>\n" +
          "<center><table>\n" +
          "<tr><td><b>Nickname</b><td><input type=text size=20 name=nickname>\n" +
          "<tr><td><b>Password</b><td><input type=text size=20 name=password>\n" +
          "<tr><td><b>Story title</b><td><input type=text size=100 name=title>\n" +
          "<tr><td><b>Category</b><td><SELECT name=category>\n");

    conn = getConnection();

    // int storyId = (Integer.valueOf(request.getParameter("storyId"))).intValue();


   
/*    if (storyId == 0)
    {
      sp.printHTML( "<h3>You must provide a story identifier !<br></h3>");
      return;
      }  */
    


    ResultSet rs = null, rs2 = null;

    try
    {
	stmt = conn.prepareStatement("SELECT * FROM categories");
        rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML(" Failed to execute Query for SubmitStory: " +e);
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
	    
	    String Name;
	    int Id;
	do
	    {      
	    Name = rs.getString("name");
	    Id = rs.getInt("id");
	    sp.printHTML("<OPTION value=\"" + Id + "\">" + Name + "</OPTION>\n");
	    } 
	while (rs.next());
	} 
    catch (Exception e) 
	{
	    sp.printHTML("Exception accepting stories: " + e +"<br>");
	    closeConnection();
	}

    sp.printHTML("</SELECT></table><p><br>\n" +
          "<TEXTAREA rows=\"20\" cols=\"80\" name=\"body\">Write your story here</TEXTAREA><br><p>\n" +
          "<input type=submit value=\"Submit this story now!\"></center><p>\n");
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
