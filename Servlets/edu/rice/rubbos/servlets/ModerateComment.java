package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class ModerateComment extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "ModerateComment");
    
    
    conn = getConnection();
  
    String comment_table, commentId;

    ResultSet rs = null;
    
    comment_table =request.getParameter("comment_table");
    commentId=request.getParameter("commentId");

    
    if (comment_table == null)
	{
	    sp.printHTML("Moderating comment, You must provide a comment table!<br>");
	    return;
	}

    if (commentId == null)
	{
	    sp.printHTML("Moderating comment, You must provide a comment identifier!<br>");
	    return;
	}

    sp.printHTMLheader("RUBBoS: Comment moderation");
    
    try
    {
	stmt = conn.prepareStatement("SELECT * FROM "+comment_table+" WHERE id="+commentId);
	rs = stmt.executeQuery();
    } 
    catch (Exception e)
    { 
      sp.printHTML("Failed to execute Query for ModerateComment: " +e);
      closeConnection();
      return;
    }
    try 
	{
	    if (!rs.first()) 
		{
		    sp.printHTML("<h3>ERROR: Sorry, but this comment does not exist.</h3><br>\n");
		    closeConnection();
		    return;
		}     
    } 
    catch (Exception e) 
    {
      sp.printHTML("Exception moderating comments: " + e +"<br>");
      closeConnection();
    }

    try
	{
	    String storyId=rs.getString("story_id");
	    sp.printHTML("<p><br><center><h2>Moderate a comment !</h2></center><br>\n<br><hr><br>");
	    String username = sp.getUserName(rs.getInt("writer"), conn);
	    sp.printHTML("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\"><TR><TD><FONT size=\"4\" color=\"#000000\"><center><B><a href=\"/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="+comment_table+"&storyId="+storyId+"&commentId="+rs.getInt("id")+"\">"+rs.getString("subject")+"</a></B>&nbsp</FONT> (Score:"+rs.getString("rating")+")</center></TABLE>\n");
	    sp.printHTML("<TABLE><TR><TD><B>Posted by "+ username+" on "+rs.getString("date")+"</B><p>\n");
	    sp.printHTML("<TR><TD>"+rs.getString("comment")+"</TABLE><p><hr><p>\n"+
			 "<form action=\"/servlet/edu.rice.rubbos.servlets.StoreModeratorLog\" method=POST>\n"+
			 "<input type=hidden name=commentId value="+commentId+">\n"+
			 "<input type=hidden name=comment_table value="+comment_table+">\n"+
			 "<center><table>\n"+
			 "<tr><td><b>Nickname</b><td><input type=text size=20 name=nickname>\n"+
			 "<tr><td><b>Password</b><td><input type=text size=20 name=password>\n"+
			 "<tr><td><b>Rating</b><td><SELECT name=rating>\n"+
			 "<OPTION value=\"-1\">-1: Offtopic</OPTION>\n"+
			 "<OPTION selected value=\"0\">0: Not rated</OPTION>\n"+
			 "<OPTION value=\"1\">1: Interesting</OPTION>\n"+
			 "</SELECT></table><p><br>\n"+
			 "<input type=submit value=\"Moderate this comment now!\"></center><p>\n");
	}
    catch (Exception e2)
	{
	    sp.printHTML("Exception moderating comments part 2: " + e2 +"<br>");
	    closeConnection();
	}
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
