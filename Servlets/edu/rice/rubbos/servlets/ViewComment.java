/**
 * RUBBoS: Rice University Bulletin Board System.
 * Copyright (C) 2001-2004 Rice University and French National Institute For 
 * Research In Computer Science And Control (INRIA).
 * Contact: jmob@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Niraj Tolia.
 */

package edu.rice.rubbos.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewComment extends RubbosHttpServlet
{

  public int getPoolSize()
  {
    return Config.BrowseCategoriesPoolSize;
  }

  private void closeConnection(PreparedStatement stmt, Connection conn)
  {
    try
    {
      if (stmt != null)
        stmt.close(); // close statement
    }
    catch (Exception ignore)
    {
    }

    try
    {
      if (conn != null)
          releaseConnection(conn);
    }
    catch (Exception ignore)
    {
    }

  }

  /** Build the html page for the response */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {

    ServletPrinter    sp    = null;
    PreparedStatement stmt = null;
    Connection        conn  = null;

    String filterstring, displaystring,	storyId, commentIdstring, 
    	comment_table;
    int parent = 0, filter = 0, display = 0, commentId;
    ResultSet         rs = null;

    sp = new ServletPrinter(response, "ViewComment");

    filterstring = request.getParameter("filter");
    storyId = request.getParameter("storyId");
    displaystring = request.getParameter("display");
    commentIdstring = request.getParameter("commentId");
    comment_table = request.getParameter("comment_table");

    if (filterstring != null)
    {
      filter = (Integer.valueOf(request.getParameter("filter"))).intValue();
    }
    else
      filter = 0;

    if (displaystring != null)
    {
      display = (Integer.valueOf(request.getParameter("display"))).intValue();
    }
    else
      display = 0;

    if (storyId == null)
    {
      sp.printHTML("Viewing comment: You must provide a story identifier!<br>");
      return;
    }

    if (commentIdstring == null)
    {
      sp
          .printHTML("Viewing comment: You must provide a comment identifier!<br>");
      return;
    }
    else
      commentId = (Integer.valueOf(request.getParameter("commentId")))
          .intValue();

    if (comment_table == null)
    {
      sp.printHTML("Viewing comment: You must provide a comment table!<br>");
      return;
    }

    conn = getConnection();

    if (commentId == 0)
      parent = 0;
    else
    {
      try
      {
        stmt = conn.prepareStatement("SELECT parent FROM " + comment_table
            + " WHERE id=" + commentId);
        rs = stmt.executeQuery();
        if (!rs.first())
        {
          sp
              .printHTML("<h3>ERROR: Sorry, but this comment does not exist.</h3><br>\n");
          closeConnection(stmt, conn);
          return;
        }
        parent = rs.getInt("parent");
        stmt.close();
      }
      catch (Exception e)
      {
        sp.printHTML("Failure at 'SELECT parent' stmt: " + e);
        closeConnection(stmt, conn);
        return;
      }
    }

    sp.printHTMLheader("RUBBoS: Viewing comments");
    
    try
    {
      Comment.displayFilterChooser(conn, sp, commentId, storyId, comment_table,
          display, filter);
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for ViewComment: " + e);
      closeConnection(stmt, conn);
      return;
    }
    
    try
    {
      Comment.fetchAndDisplay(sp, conn, comment_table, filter, commentId, display, storyId, parent);
    } 
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for ViewComment: " + e);
      closeConnection(stmt, conn);
      return;
    } 

    closeConnection(stmt, conn);
    sp.printHTMLfooter();
    

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doGet(request, response);
  }

}
