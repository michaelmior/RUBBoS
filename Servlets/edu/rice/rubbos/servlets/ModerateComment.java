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

public class ModerateComment extends RubbosHttpServlet
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

    sp = new ServletPrinter(response, "ModerateComment");

    String comment_table, commentId;

    ResultSet rs = null;

    comment_table = request.getParameter("comment_table");
    commentId = request.getParameter("commentId");

    if (comment_table == null)
    {
      sp.printHTML("Moderating comment, You must provide a comment table!<br>");
      return;
    }

    if (commentId == null)
    {
      sp
          .printHTML("Moderating comment, You must provide a comment identifier!<br>");
      return;
    }

    sp.printHTMLheader("RUBBoS: Comment moderation");

    conn = getConnection();

    try
    {
      stmt = conn.prepareStatement("SELECT * FROM " + comment_table
          + " WHERE id=" + commentId);
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for ModerateComment: " + e);
      closeConnection(stmt, conn);
      return;
    }

    try
    {
      if (!rs.first())
      {
        sp
            .printHTML("<h3>ERROR: Sorry, but this comment does not exist.</h3><br>\n");
        closeConnection(stmt, conn);
        return;
      }
    }
    catch (Exception e)
    {
      sp.printHTML("Exception moderating comments: " + e + "<br>");
      closeConnection(stmt, conn);
      return;
    }

    try
    {
      String storyId = rs.getString("story_id");
      sp
          .printHTML("<p><br><center><h2>Moderate a comment !</h2></center><br>\n<br><hr><br>");
      // XXX - Get rid of this getUserName()
      String username = sp.getUserName(rs.getInt("writer"), conn);
      sp
          .printHTML("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\"><TR><TD><FONT size=\"4\" color=\"#000000\"><center><B><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
              + comment_table
              + "&storyId="
              + storyId
              + "&commentId="
              + rs.getInt("id")
              + "\">"
              + rs.getString("subject")
              + "</a></B>&nbsp</FONT> (Score:"
              + rs.getString("rating")
              + ")</center></TABLE>\n");
      sp.printHTML("<TABLE><TR><TD><B>Posted by " + username + " on "
          + rs.getString("date") + "</B><p>\n");
      sp
          .printHTML("<TR><TD>"
              + rs.getString("comment")
              + "</TABLE><p><hr><p>\n"
              + "<form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.StoreModeratorLog\" method=POST>\n"
              + "<input type=hidden name=commentId value="
              + commentId
              + ">\n"
              + "<input type=hidden name=comment_table value="
              + comment_table
              + ">\n"
              + "<center><table>\n"
              + "<tr><td><b>Nickname</b><td><input type=text size=20 name=nickname>\n"
              + "<tr><td><b>Password</b><td><input type=text size=20 name=password>\n"
              + "<tr><td><b>Rating</b><td><SELECT name=rating>\n"
              + "<OPTION value=\"-1\">-1: Offtopic</OPTION>\n"
              + "<OPTION selected value=\"0\">0: Not rated</OPTION>\n"
              + "<OPTION value=\"1\">1: Interesting</OPTION>\n"
              + "</SELECT></table><p><br>\n"
              + "<input type=submit value=\"Moderate this comment now!\"></center><p>\n");
    }
    catch (Exception e2)
    {
      sp.printHTML("Exception moderating comments part 2: " + e2 + "<br>");
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
