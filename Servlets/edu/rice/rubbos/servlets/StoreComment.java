package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.URLEncoder;

public class StoreComment extends RubbosHttpServlet
{
  private ServletPrinter sp = null;
  private PreparedStatement stmt = null, stmt2=null, stmt3=null, stmt4=null;
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
    sp = new ServletPrinter(response, "StoreComment");
        
    conn = getConnection();
  
    String nickname, password, storyId, parent, userIdstring, subject, body, comment_table;
    int page = 0, nbOfStories = 0, userId;
    ResultSet rs = null, rs2=null, rs3=null, rs4=null;
    
   
    nickname = request.getParameter("nickname");
    password= request.getParameter("password");
    storyId= request.getParameter("storyId");
    parent=request.getParameter("parent");
    subject=request.getParameter("subject");
    body=request.getParameter("body");
    comment_table=request.getParameter("comment_table");

    if (nickname == null)
	{
	    nickname=request.getParameter("nickname");
	}

    if (password == null)
	{
	    password=request.getParameter("password");
	}

    if (storyId == null)
	{
	    sp.printHTML("StoreComment, You must provide a story identifier!<br>");
	    return;
	}

    if (parent == null)
	{
	     sp.printHTML("StoreComment, You must provide a follow up identifier!<br>");
	     return;
	}

     if (subject == null)
	{
	     sp.printHTML("StoreComment, You must provide a comment subject!<br>");
	     return;
	}

     if (body == null)
	{
	     sp.printHTML("StoreComment, <h3>You must provide a comment body!<br></h3>");
	     return;
	}

     if (comment_table == null)
	{
	     sp.printHTML("Viewing comment, You must provide a comment table!<br>");
	     return;
	}
     
    sp.printHTMLheader("RUBBoS: Comment submission result");

    sp.printHTML("<center><h2>Comment submission result:</h2></center><p>\n");

    // Authenticate the user
    userIdstring= sp.authenticate(nickname, password, conn);
    userId= (Integer.valueOf(userIdstring)).intValue();

    

    if (userId == 0)
	sp.printHTML("Comment posted by the 'Anonymous Coward'<br>\n");
    else
	sp.printHTML("Comment posted by user #"+userId+"<br>\n");

    // Add comment to database
   
    try 
	{
	    stmt=conn.prepareStatement("INSERT INTO "+comment_table+" VALUES (NULL, "+userId+", "+storyId+", "+parent+", 0, 0, NOW(), \""+ subject+"\", \""+body+"\")");
	    rs=stmt.executeQuery();

	    stmt2=conn.prepareStatement("UPDATE "+comment_table+" SET childs=childs+1 WHERE id="+parent);
	    rs2=stmt2.executeQuery();
	} 
    catch (Exception e) 
	{
	    sp.printHTML("Exception getting categories: " + e +"<br>");
	    closeConnection();
	}

    sp.printHTML("Your comment has been successfully stored in the "+comment_table+" database table<br>\n");
    sp.printHTMLfooter();

  }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
    doGet(request, response);
  }

}
