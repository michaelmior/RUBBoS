package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class ViewComment extends RubbosHttpServlet
{
  private ServletPrinter sp = null;
  private PreparedStatement stmt5=null, stmtfollow=null, stmt = null, stmt2=null;
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

    public void display_follow_up (int cid, int level, int display, int filter, Connection link, String comment_table, boolean separator)
    {
	ResultSet follow;
	int childs,story_id,rating, parent, id, i;
	String  subject, username, date, comment;
	try
	    {
		stmtfollow = conn.prepareStatement("SELECT * FROM "+comment_table+" WHERE parent="+cid);
		//+" AND rating>="+filter);
		follow= stmtfollow.executeQuery();
	
		while (follow.next())
	    {
		story_id=follow.getInt("story_id");
		id= follow.getInt("id");
		subject=follow.getString("subject");
		username=sp.getUserName(follow.getInt("writer"), conn);
		date=follow.getString("date");
		rating= follow.getInt("rating");
		parent=follow.getInt("parent");
		comment =follow.getString("comment");
		childs= follow.getInt("childs");
		
		if (rating >= filter)
		    {
			if (!separator)
			    {
				sp.printHTML("<br><hr><br>");
				separator = true;
			    }
			if (display == 1) // Preview nested comments
			    {
				for (i = 0 ; i < level ; i++)
				    sp.printHTML(" &nbsp &nbsp &nbsp ");
				sp.printHTML("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="+comment_table+ "&storyId="+ story_id+ "&commentId="+ id+ "&filter="+ filter+ "&display=" +display+"\">"+subject+"</a> by "+ username+" on "+date+"<br>\n");
			    }
			else
			    {
				sp.printHTML("<TABLE bgcolor=\"#CCCCFF\"><TR>");
				for (i = 0 ; i < level ; i++)
				    sp.printHTML("<TD>&nbsp&nbsp&nbsp");
				sp.printHTML("<TD><FONT size=\"4\" color=\"#000000\"><B><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table=" +comment_table+ "&storyId="+ story_id+ "&commentId="+ id+ "&filter="+ filter+"&display="+ display+"\">"+ subject +"</a></B>&nbsp</FONT> (Score:"+ rating + ")</TABLE>\n");
				sp.printHTML("<TABLE>");
				for (i = 0 ; i < level ; i++)
				    sp.printHTML("<TD>&nbsp&nbsp&nbsp");
				sp.printHTML("<TD><B>Posted by "+ username+ " on "+ date+ "</B><p><TR>\n");
				for (i = 0 ; i < level ; i++)
				    sp.printHTML("<TD>&nbsp&nbsp&nbsp");
				sp.printHTML("<TD>"+ comment+"<TR>");
				for (i = 0 ; i < level ; i++)
				    sp.printHTML("<TD>&nbsp&nbsp&nbsp");
				sp.printHTML("<TD><p>[ <a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.PostComment?comment_table="+comment_table+ "&storyId="+ story_id+ "&parent="+ id+ "\">Reply to this</a>"+
					     "&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="+comment_table+ "&storyId="+ story_id+ "&commentId="+ parent+
					     "&filter=" +filter+"&display="+ display+ "\">Parent</a>&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ModerateComment?comment_table="+ comment_table+ "&commentId="+
					     id+"\">Moderate</a> ]</TABLE><br>");
			    }
		    }
		if (childs > 0)
		    display_follow_up(id, level+1, display, filter, link, comment_table, separator);
	    } 
	    }
	catch (Exception e)
		{ 
		    sp.printHTML("Failure at display_follow_up: " +e);
		    closeConnection();
		    return;
		}
      	  }



  /** Build the html page for the response */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    sp = new ServletPrinter(response, "ViewComment"); 
    
    conn = getConnection();
  
    String categoryName, filterstring,  username, categoryId, comment=null, displaystring, storyId, commentIdstring, comment_table;
    int parent=0, childs, page = 0, filter=0, display=0, commentId;
      int i=0, count, rating;

    ResultSet rs = null,rs5=null, rs2=null;
    
    filterstring =request.getParameter("filter");
    storyId = request.getParameter("storyId");
    displaystring= request.getParameter("display");
    commentIdstring=request.getParameter("commentId");
    comment_table=request.getParameter("comment_table");

    if (filterstring != null )
	{
	    filter = (Integer.valueOf(request.getParameter("filter"))).intValue();
	}
    else 
	filter =0;
    
    if (displaystring != null )
	{
	    display = (Integer.valueOf(request.getParameter("display"))).intValue();
	}
    else
	display=0;
    
    if (storyId == null)
	{
	    sp.printHTML("Viewing comment: You must provide a story identifier!<br>");
	    return;
	}

    if (commentIdstring == null)
	{
	    sp.printHTML("Viewing comment: You must provide a comment identifier!<br>");
	    return;
	}
    else 
	commentId=(Integer.valueOf(request.getParameter("commentId"))).intValue();

    if (comment_table == null)
	{
	   sp.printHTML("Viewing comment: You must provide a comment table!<br>");
      	}
    
    if (commentId == 0)
	
	parent = 0;
    else
	{
	    try
		{
		    stmt5 = conn.prepareStatement("SELECT parent FROM "+comment_table+" WHERE id="+commentId);
		    rs5=stmt5.executeQuery();
		    if (!rs5.first())
			{
			    sp.printHTML("<h3>ERROR: Sorry, but this comment does not exist.</h3><br>\n");
			    closeConnection();
			    return;
			}
		    parent= rs5.getInt("parent");
       		}
	    catch (Exception e)
		{ 
		    sp.printHTML("Failure at stmt5: " +e);
		    closeConnection();
		    return;
		}
	}
    sp.printHTMLheader("RUBBoS: Viewing comments");
    sp.printHTML("<center><form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment\" method=POST>\n"+
          "<input type=hidden name=commentId value="+commentId+">\n"+
          "<input type=hidden name=storyId value="+storyId+">\n"+
          "<input type=hidden name=comment_table value="+ comment_table+">\n"+
          "<B>Filter :</B>&nbsp&nbsp<SELECT name=filter>\n");

    try
	{
	    stmt = conn.prepareStatement("SELECT rating, COUNT(rating) AS count FROM "+comment_table+" WHERE story_id="+storyId+" GROUP BY rating ORDER BY rating");
	    rs = stmt.executeQuery();

	    i=-1;
	    if (rs.first())
		{
		    do 
			{
			    rating=rs.getInt("rating");
			    count= rs.getInt("count");
			    while ((i < 6) && (rating != i))
				{
				    if (i == filter)
					sp.printHTML("<OPTION selected value=\""+i+ "\">" +i+": 0 comment</OPTION>\n");
				    else
					sp.printHTML("<OPTION value=\""+i+"\">"+i+": 0 comment</OPTION>\n");
				    i++;
				}
			    if (rating == i)
				{
				    if (i == filter)
					sp.printHTML("<OPTION selected value=\""+i+"\">"+i+": "+count+ " comments</OPTION>\n");
				    else
					sp.printHTML("<OPTION value=\""+ i+"\">" +i+": "+ count+" comments</OPTION>\n");
				    i++;
				}
			} while (rs.next());
		}
    } 
    catch (Exception e)
    { 
      sp.printHTML("Failed to execute Query for View Comment: " +e);
      closeConnection();
      return;
    }
    
      
    

    while (i < 6)
	{
	    sp.printHTML("<OPTION value=\""+i+"\">"+i+": 0 comment</OPTION>\n");
	    i++;
	}

    sp.printHTML("</SELECT>&nbsp&nbsp&nbsp&nbsp<SELECT name=display>\n"+
          "<OPTION value=\"0\">Main threads</OPTION>\n");
    if (display == 1)
      sp.printHTML("<OPTION selected value=\"1\">Nested</OPTION>\n");
    else
      sp.printHTML("<OPTION value=\"1\">Nested</OPTION>\n");
    if (display == 2)
      sp.printHTML("<OPTION selected value=\"2\">All comments</OPTION>\n");
    else
      sp.printHTML("<OPTION value=\"2\">All comments</OPTION>\n");
    sp.printHTML("</SELECT>&nbsp&nbsp&nbsp&nbsp<input type=submit value=\"Refresh display\"></center><p>\n");      

    String subject, date;
    int id;
    boolean separator;
    try
	{
	    stmt2=conn.prepareStatement("SELECT * FROM "+comment_table+" WHERE story_id="+storyId+
					" AND parent=0"); //+ parent+
					//" AND rating>="+filter);
	    rs2=stmt2.executeQuery();

	    while (rs2.next())
		{
		    username = sp.getUserName(rs2.getInt("writer"), conn);
		    rating=rs2.getInt("rating");
		    parent=rs2.getInt("parent");
		    id=rs2.getInt("id");
		    subject=rs2.getString("subject");
		    date=rs2.getString("date");
		    childs=rs2.getInt("childs");
		    comment=rs2.getString("comment");
		    separator = false;
		    
		    if (rating >= filter)
			{
			    sp.printHTML("<br><hr><br>");
			    separator = true;
			    sp.printHTML("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\"><TR><TD><FONT size=\"4\" color=\"#000000\"><B><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="+comment_table+"&storyId="+storyId+"&commentId="+id+"&filter="+filter+"&display="+ display+ "\">"+subject+"</a></B>&nbsp</FONT> (Score:"+rating+")</TABLE>\n");
			    sp.printHTML("<TABLE><TR><TD><B>Posted by "+username+" on "+date+"</B><p>\n");
			    sp.printHTML("<TR><TD>"+comment);
			    sp.printHTML("<TR><TD><p>[ <a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.PostComment?comment_table="+comment_table+ "&storyId="+storyId+"&parent="+id+ "\">Reply to this</a>&nbsp|&nbsp"+
					 "<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="+comment_table+"&storyId="+storyId+"&commentId="+ parent+"&filter="+filter+"&display="+ display+ "\">Parent</a>"+
					 "&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ModerateComment?comment_table="+comment_table+ "&commentId="+id+"\">Moderate</a> ]</TABLE>\n");
			}
		    if ((display > 0) &&(childs > 0))
			display_follow_up(id, 1, display, filter, conn, comment_table, separator);
		}
	    
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
