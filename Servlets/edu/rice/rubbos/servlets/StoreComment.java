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

public class StoreComment extends RubbosHttpServlet
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

    ServletPrinter    sp   = null;
    PreparedStatement stmt = null;
    Connection        conn = null;

    String nickname, password, storyId, parent, userIdstring, subject, body;
    String comment_table;
    int page = 0, nbOfStories = 0, userId;
    int updateResult;

    sp = new ServletPrinter(response, "StoreComment");

    nickname = request.getParameter("nickname");
    password = request.getParameter("password");
    storyId = request.getParameter("storyId");
    parent = request.getParameter("parent");
    subject = request.getParameter("subject");
    body = request.getParameter("body");
    comment_table = request.getParameter("comment_table");

    if (nickname == null)
    {
      nickname = request.getParameter("nickname");
    }

    if (password == null)
    {
      password = request.getParameter("password");
    }

    if (storyId == null)
    {
      sp.printHTML("StoreComment, You must provide a story identifier!<br>");
      return;
    }

    if (parent == null)
    {
      sp
          .printHTML("StoreComment, You must provide a follow up identifier!<br>");
      return;
    }

    if (subject == null)
    {
      sp.printHTML("StoreComment, You must provide a comment subject!<br>");
      return;
    }

    if (body == null)
    {
      sp
          .printHTML("StoreComment, <h3>You must provide a comment body!<br></h3>");
      return;
    }

    if (comment_table == null)
    {
      sp.printHTML("Viewing comment, You must provide a comment table!<br>");
      return;
    }

    sp.printHTMLheader("RUBBoS: Comment submission result");

    sp.printHTML("<center><h2>Comment submission result:</h2></center><p>\n");

    conn = getConnection();

    // Authenticate the user
    userIdstring = sp.authenticate(nickname, password, conn);
    userId = (Integer.valueOf(userIdstring)).intValue();

    if (userId == 0)
      sp.printHTML("Comment posted by the 'Anonymous Coward'<br>\n");
    else
      sp.printHTML("Comment posted by user #" + userId + "<br>\n");

    // Add comment to database

    try
    {
      stmt = conn.prepareStatement("INSERT INTO " + comment_table
          + " VALUES (NULL, " + userId + ", " + storyId + ", " + parent
          + ", 0, 0, NOW(), \"" + subject + "\", \"" + body + "\")");
      updateResult = stmt.executeUpdate();

      stmt.close();

      stmt = conn.prepareStatement("UPDATE " + comment_table
          + " SET childs=childs+1 WHERE id=" + parent);
      updateResult = stmt.executeUpdate();
    }
    catch (Exception e)
    {
      closeConnection(stmt, conn);
      sp.printHTML("Exception getting categories: " + e + "<br>");
      return;
    }

    closeConnection(stmt, conn);

    sp.printHTML("Your comment has been successfully stored in the "
        + comment_table + " database table<br>\n");
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doGet(request, response);
  }

}
