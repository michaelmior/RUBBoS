package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class PostComment extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "BrowseCategoriesByCategory");
    
    
    conn = getConnection();
  
    String storyIdtest,categoryId, testparent, comment_table;
    int parent=0, storyId = 0;
    
    storyIdtest =request.getParameter("storyId");
    testparent=request.getParameter("parent");

    if (storyIdtest != null )
	{
	    storyId = (Integer.valueOf(request.getParameter("storyId"))).intValue();
	}
    
    if (testparent != null )
	{
	    parent = (Integer.valueOf(request.getParameter("parent"))).intValue();
	}

    comment_table = request.getParameter("comment_table");
        
    if (comment_table == null)
	{
	    sp.printHTML("Viewing comment, You must provide a comment table!<br>");
	    return;
	}

    sp.printHTMLheader("RUBBoS: Comment submission");
    sp.printHTML("<p><br><center><h2>Post a comment !</h2><br>\n"+
          "<form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.StoreComment\" method=POST>\n"+
          "<input type=hidden name=storyId value="+storyId+">\n"+
          "<input type=hidden name=parent value="+parent+">\n"+
          "<input type=hidden name=comment_table value="+comment_table+">\n"+
          "<center><table>\n"+
          "<tr><td><b>Nickname</b><td><input type=text size=20 name=nickname>\n"+
          "<tr><td><b>Password</b><td><input type=text size=20 name=password>\n"+
          "<tr><td><b>Subject</b><td><input type=text size=100 name=subject>\n"+
          "</SELECT></table><p><br>\n"+
          "<TEXTAREA rows=\"20\" cols=\"80\" name=\"body\">Write your comment here</TEXTAREA><br><p>\n"+
          "<input type=submit value=\"Post your comment now!\"></center><p>\n");

    
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
