package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class Search extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "Search");
    
    conn = getConnection();
  
    String testtype, search, testpage, testnbOfStories, table, title=null, comment_table;
    int page = 0, type, nbOfStories = 0;
    ResultSet rs = null;
    
    testtype =request.getParameter("type");
    testnbOfStories=request.getParameter("nbOfStories");
    search = request.getParameter("search");
    testpage= request.getParameter("page");
    
    if (testtype == null)
	{
	    type=0;
	}
    else 
	type=(Integer.valueOf(request.getParameter("type"))).intValue();
	

    if (testpage == null )
	{
	    page=0;
	}
    else
	page=(Integer.valueOf(request.getParameter("page"))).intValue();
    
    if (testnbOfStories != null )
	{
	    nbOfStories = 25;
	}
    
    if (search == null)
	{
	    search= request.getParameter("search");
     	}

    if (testnbOfStories==null)
	nbOfStories =25;

	
    sp.printHTMLheader("RUBBoS search");

    // Display the search form
    sp.printHTML("<form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search\" method=POST>\n"+
          "<center><table>\n"+
          "<tr><td><b>Search</b><td><input type=text size=50 name=search value="+search+">\n"+
          "<tr><td><b>in</b><td><SELECT name=type>\n");
    if (type == 0)
	{
	    sp.printHTML("<OPTION selected value=\"0\">Stories</OPTION>\n");
	    table = "stories";
	    title = "Stories";
	}
    else
	sp.printHTML("<OPTION value=\"0\">Stories</OPTION>\n");
    if (type == 1)
	{
	    sp.printHTML("<OPTION selected value=\"1\">Comments</OPTION>\n");
	    table = "comments";
	    title = "Comments";
	}
    else
	sp.printHTML("<OPTION value=\"1\">Comments</OPTION>\n");
    if (type == 2)
	{
	    sp.printHTML("<OPTION selected value=\"2\">Authors</OPTION>\n");
	    table = "users";
	    title = "Stories with author";
	}
    else
	sp.printHTML("<OPTION value=\"2\">Authors</OPTION>\n");
    sp.printHTML("</SELECT></table><p><br>\n"+
          "<input type=submit value=\"Search now!\"></center><p>\n");

      // Display the results
    if (search == null)
      sp.printHTML("<br><center><h2>Please select a text to search for</h2></center><br>");
    else
    {
      sp.printHTML("<br><h2>"+title+" matching <i>"+search+"</i></h2></center><br>");
      
      if (type == 0)
	  {
       	      try
		  {
		      stmt = conn.prepareStatement("SELECT id, title, date, writer FROM stories WHERE title LIKE '"+search+"%' "+/*OR body LIKE '$search%%'*/" ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
		      rs = stmt.executeQuery();
		  } 
	      catch (Exception e)
		  { 
		      sp.printHTML("Failed to execute Query for BrowseStoriesByCategory: " +e);
		      closeConnection();
		      return;
		  }
	      try 
		  {
		      if (!rs.first()) 
			  {
			      stmt= conn.prepareStatement("SELECT id, title, date, writer FROM old_stories WHERE title LIKE '"+search+"%' "+/*OR body LIKE '$search%%'*/" ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
			      rs=stmt.executeQuery();
			  }
		      if (!rs.first())
		
			  {
			      if (page == 0)
				  sp.printHTML("<h2>Sorry, but there is no story matching <i>"+search+"</i> !</h2>");
			      else
				  {
				      sp.printHTML("<h2>Sorry, but there are no more stories available matching <i>"+search+"</i>.</h2><br>\n");
				      sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search?search="+ URLEncoder.encode(search)+"&type="+type+"&page="+(page-1)+"&nbOfStories="+nbOfStories+"\">Previous page</a>\n</CENTER>\n");
				  }
			   
			      sp.printHTMLfooter();
			      return;
			  }
		  }
 
	      catch (Exception e) 
    {
      sp.printHTML("Exception searching type 0: " + e +"<br>");
      closeConnection();
    }
	  }

      if (type == 1)
	  { // Look for comments
	      comment_table = "comments";
	      try
		  {
		      stmt= conn.prepareStatement("SELECT id,story_id,subject,writer,date FROM comments WHERE subject LIKE '"+search+"%' "+/*OR comment LIKE '$search%%'*/" GROUP BY story_id ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
		      rs = stmt.executeQuery();
		      if (!rs.first())
			  {
			      stmt= conn.prepareStatement("SELECT id,story_id,subject,writer,date FROM old_comments WHERE subject LIKE '"+search+"%' "+/*OR comment LIKE '$search%%'*/" ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
			      rs= stmt.executeQuery();
		      
			      comment_table = "old_comments";
			  }
		      if (!rs.first())
			  {
			      if (page == 0)
				  sp.printHTML("<h2>Sorry, but there is no comment matching <i>"+search+"</i> !</h2>");
			      else
				  {
				      sp.printHTML("<h2>Sorry, but there are no more comments available matching <i>"+search+"</i>.</h2><br>\n");
				      sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search?search="+URLEncoder.encode(search)+"&type="+type+"&page="+(page-1)+"&nbOfStories="+nbOfStories+"\">Previous page</a>\n</CENTER>\n");
				  }
		      
			      sp.printHTMLfooter();
			      return;
			  }
		      else
			  {
		      
			      // Print the comment subject and author
			      do
				  {
				      String story_id=rs.getString("story_id");
				      String id=rs.getString("id");
				      String subject=rs.getString("subject");
				      String username=sp.getUserName(rs.getInt("writer"), conn);
				      String date=rs.getString("date");

				      sp.printHTML("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="+comment_table+"&storyId="+story_id+"&commentId="+id+"&filter=0&display=0\">"+subject+"</a> by "+username+" on "+date+"<br>\n");
				  } while (rs.next());
			  }
		  }
	      catch (Exception e4)
		  {
		      sp.printHTML(e4+ "Exception in type==1");
		      closeConnection();
		  }
	  }

      if (type == 2)
	  { // Look for stories of an author
	      try
		  {
		      stmt = conn.prepareStatement("SELECT stories.id, stories.title, stories.date, stories.writer FROM stories,users WHERE writer=users.id AND "+/*(users.firstname LIKE '$search%%' OR users.lastname LIKE '$search%%' OR*/" users.nickname LIKE '"+search+"%'"+/*)*/" ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
		      rs = stmt.executeQuery();
		      if (!rs.first())
			  stmt = conn.prepareStatement("SELECT old_stories.id, old_stories.title, old_stories.date, old_stories.writer FROM old_stories,users WHERE writer=users.id AND "+/*(users.firstname LIKE '$search%%' OR users.lastname LIKE '$search%%' OR*/" users.nickname LIKE '"+search+"%'"+/*)*/" ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
		      rs = stmt.executeQuery();

		      if (!rs.first())
			  {
			      if (page == 0)
				  sp.printHTML("<h2>Sorry, but there is no story with author matching <i>"+search+"</i> !</h2>");
			      else
				  {
				      sp.printHTML("<h2>Sorry, but there are no more stories available with author matching <i>$search</i>.</h2><br>\n");
				      sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search?search="+URLEncoder.encode(search)+"&type="+type+"&page="+(page-1)+"&nbOfStories="+nbOfStories+"\">Previous page</a>\n</CENTER>\n");
				  }
		     
			      sp.printHTMLfooter();
			      return;
			  }
		  }
	      catch (Exception e6)
		  {
		      sp.printHTML(e6 +"Exception in type==2");
		      closeConnection();
		  }
	  }

      try 
	  {
	      if (type != 1)
		  {
	    
		  
		      // Print the story titles and author
		      do
			  {
			      String id=rs.getString("id");
			      String date=rs.getString("date");
			      title=rs.getString("title");

			      String username=sp.getUserName(rs.getInt("writer"), conn);
			      sp.printHTML("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewStory?storyId="+id+"\">"+title+"</a> by "+username+" on "+date+"<br>\n");
			  } while (rs.next());
		  }


	      if (page == 0)
		  sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search?search="+URLEncoder.encode(search)+"&type="+type+"&page="+(page+1)+"&nbOfStories="+nbOfStories+"\">Next page</a>\n</CENTER>\n");
	      else
		  sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search?search="+URLEncoder.encode(search)+"&type="+type+"&page="+(page-1)+"&nbOfStories="+nbOfStories+"\">Previous page</a>\n&nbsp&nbsp&nbsp"+
			       "<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.Search?category="+search+"="+ URLEncoder.encode(search) +"&type="+type+"&page="+(page+1)+"&nbOfStories="+nbOfStories+"\">Next page</a>\n\n</CENTER>\n");
	  }
      catch (Exception e7)
	  {
	      sp.printHTML(e7+ "Exception in type!=1");
	  }
    }
    
      sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
