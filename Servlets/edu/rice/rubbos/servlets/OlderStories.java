package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class OlderStories extends RubbosHttpServlet
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
    sp = new ServletPrinter(response, "OlderStories");
    
    
    conn = getConnection();
  
    String day, month, year, testpage, username, testnbOfStories;
    int page = 0, nbOfStories = 0, id;
    ResultSet rs2 = null;
    
    testpage =request.getParameter("page");
    testnbOfStories=request.getParameter("nbOfStories");
    day =request.getParameter("day");
    month =request.getParameter("month");
    year=request.getParameter("year");

    if (testpage != null )
	{
	    page = (Integer.valueOf(request.getParameter("page"))).intValue();
	}
    
    if (testpage == null)
	{
	    page=0;
	}

    if (month ==null)
	{
	    month=request.getParameter("month");
	}

    if (day ==null)
	{
	    day=request.getParameter("day");
	}

    if (year == null)
	{
	    year=request.getParameter("year");
	}

    if (testnbOfStories != null )
	{
	    nbOfStories = (Integer.valueOf(request.getParameter("nbOfStories"))).intValue();
	}
    else
	nbOfStories =25;

    sp.printHTMLheader("RUBBoS Older Stories");

    // Display the date chooser
    sp.printHTML("<form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories\" method=POST>\n");
    sp.printHTML("<center><B>Date (day/month/year):</B><SELECT name=day>\n");
    for (int i = 1 ; i < 32 ; i++)
      sp.printHTML("<OPTION value=\""+i+"\">"+i+"</OPTION>\n");      
    sp.printHTML("</SELECT>&nbsp/&nbsp<SELECT name=month>\n");
    for (int i = 1 ; i < 13 ; i++)
      sp.printHTML("<OPTION value=\""+i+"\">"+i+"</OPTION>\n");      
    sp.printHTML("</SELECT>&nbsp/&nbsp<SELECT name=year>\n");
    for (int i = 2000 ; i < 2013 ; i++)
      sp.printHTML("<OPTION value=\""+i+"\">"+i+"</OPTION>\n");      
    sp.printHTML("</SELECT><p><input type=submit value=\"Retrieve stories from this date!\"><p>\n");
    
     if ((day == null) || (month == null) || (year == null))
	 sp.printHTML("<br><h2>Please select a date</h2><br>");
    else
    {
      sp.printHTML("<br><h2>Stories of the "+day+"/"+month+"/"+year+"</h2></center><br>");

      String before, after;
      before = year+"-"+month+"-"+day+" 0:0:0";
      after = year+"-"+month+"-"+day+" 23:59:59";
      try 
	  {
	      stmt = conn.prepareStatement("SELECT * FROM stories WHERE date>='"+before+"' AND date<='"+after+"' ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
	      rs2 = stmt.executeQuery(); 
	      if (!rs2.first()) 
		  {
		      stmt2 = conn.prepareStatement("SELECT * FROM old_stories WHERE date>='"+before+"' AND date<='"+after+"' ORDER BY date DESC LIMIT "+page*nbOfStories+","+nbOfStories);
		      rs2= stmt2.executeQuery();
		  }
	      if (!rs2.first())
		  { 
		      if (page == 0)
			  sp.printHTML("<h2>Sorry, but there are no story available for this date !</h2>"); 
		      else 
			  {
			      sp.printHTML("<h2>Sorry, but there is no more stories available for this date.</h2><br>\n"); 
			      sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?day="+day+"&month="+month+"&year="+year+"&page="+(page-1)+"&nbOfStories="+nbOfStories+"\">Previous page</a>\n</CENTER>\n");
			  }
     		      sp.printHTMLfooter();
                      return;
		  }
	  }
      catch (Exception e)
	  {
	       sp.printHTML("Exception getting older stories: " + e +"<br>");
	       closeConnection();
	  }

      String  title, date;
      // Print the story titles and author
      
      try
	  {
	      while (rs2.next())	
		  {
		      id=rs2.getInt("id");
		      title=rs2.getString("title");
		      username = sp.getUserName(rs2.getInt("writer"), conn);
		      date= rs2.getString("date");
		      sp.printHTML("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewStory?storyId="+id+"\">"+ title+"</a> by "+username+" on "+ date+"<br>\n");
		  } 	
	  }
      catch (Exception e2)
	  {
	      sp.printHTML("Exception getting strings: " + e2 +"<br>");
	      closeConnection();
	  }
      if (page == 0)
	  sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?day="+day+"&month="+month+"&year="+year+"&page="+(page+1)+"&nbOfStories="+nbOfStories+"\">Next page</a>\n</CENTER>\n");
      else
	  sp.printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?day="+day+"&month="+month+"&year="+year+"&page="+(page-1)+"&nbOfStories="+nbOfStories+"\">Previous page</a>\n&nbsp&nbsp&nbsp"+
		"<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?category="+day+"="+day+"&month="+month+"&year="+year+"&page="+(page+1)+"&nbOfStories="+nbOfStories+"\">Next page</a>\n\n</CENTER>\n");
    }
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }

}
