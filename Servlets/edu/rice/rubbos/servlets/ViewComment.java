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
  
  /**
   * This function must throw an exception as there should be only
   * ONE close fo the connection and that close should be handled by
   * the caller 
   */
  public void display_follow_up(int cid, int level, int display, int filter,
                                Connection link, String comment_table, 
                                boolean separator, ServletPrinter sp,
                                Connection conn) throws Exception
  {
    ResultSet         follow;
    int               childs, story_id, rating, parent, id, i;
    String            subject, username, date, comment;
    PreparedStatement stmtfollow = null;

    try
    {
      stmtfollow = conn.prepareStatement("SELECT * FROM " + comment_table
          + " WHERE parent=" + cid);
      //+" AND rating>="+filter);
      follow = stmtfollow.executeQuery();

      while (follow.next())
      {
        story_id = follow.getInt("story_id");
        id = follow.getInt("id");
        subject = follow.getString("subject");
        username = sp.getUserName(follow.getInt("writer"), conn);
        date = follow.getString("date");
        rating = follow.getInt("rating");
        parent = follow.getInt("parent");
        comment = follow.getString("comment");
        childs = follow.getInt("childs");

        if (rating >= filter)
        {
          if (!separator)
          {
            sp.printHTML("<br><hr><br>");
            separator = true;
          }
          if (display == 1) // Preview nested comments
          {
            for (i = 0; i < level; i++)
              sp.printHTML(" &nbsp &nbsp &nbsp ");
            sp
                .printHTML("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                    + comment_table
                    + "&storyId="
                    + story_id
                    + "&commentId="
                    + id
                    + "&filter="
                    + filter
                    + "&display="
                    + display
                    + "\">"
                    + subject
                    + "</a> by "
                    + username
                    + " on "
                    + date
                    + "<br>\n");
          }
          else
          {
            sp.printHTML("<TABLE bgcolor=\"#CCCCFF\"><TR>");
            for (i = 0; i < level; i++)
              sp.printHTML("<TD>&nbsp&nbsp&nbsp");
            sp
                .printHTML("<TD><FONT size=\"4\" color=\"#000000\"><B><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                    + comment_table
                    + "&storyId="
                    + story_id
                    + "&commentId="
                    + id
                    + "&filter="
                    + filter
                    + "&display="
                    + display
                    + "\">"
                    + subject
                    + "</a></B>&nbsp</FONT> (Score:"
                    + rating
                    + ")</TABLE>\n");
            sp.printHTML("<TABLE>");
            for (i = 0; i < level; i++)
              sp.printHTML("<TD>&nbsp&nbsp&nbsp");
            sp.printHTML("<TD><B>Posted by " + username + " on " + date
                + "</B><p><TR>\n");
            for (i = 0; i < level; i++)
              sp.printHTML("<TD>&nbsp&nbsp&nbsp");
            sp.printHTML("<TD>" + comment + "<TR>");
            for (i = 0; i < level; i++)
              sp.printHTML("<TD>&nbsp&nbsp&nbsp");
            sp
                .printHTML("<TD><p>[ <a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.PostComment?comment_table="
                    + comment_table
                    + "&storyId="
                    + story_id
                    + "&parent="
                    + id
                    + "\">Reply to this</a>"
                    + "&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                    + comment_table
                    + "&storyId="
                    + story_id
                    + "&commentId="
                    + parent
                    + "&filter="
                    + filter
                    + "&display="
                    + display
                    + "\">Parent</a>&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ModerateComment?comment_table="
                    + comment_table
                    + "&commentId="
                    + id
                    + "\">Moderate</a> ]</TABLE><br>");
          }
        }
        if (childs > 0)
          display_follow_up(id, level + 1, display, filter, link,
                            comment_table, separator, sp, conn);
      }
    }
    catch (Exception e)
    {
      sp.printHTML("Failure at display_follow_up: " + e);      
      try 
      {
	  stmtfollow.close();
      } 
      catch (Exception ignore) 
      {
      }
      throw e;
    }
    stmtfollow.close();
  }

  /** Build the html page for the response */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {

    ServletPrinter    sp    = null;
    PreparedStatement stmt = null;
    Connection        conn  = null;

    String            categoryName, filterstring, username, categoryId, 
        comment = null, displaystring, storyId, commentIdstring, comment_table;
    int               parent = 0, childs, page = 0, filter = 0, display = 0, 
        commentId;
    int               i = 0, count, rating;
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
    sp
        .printHTML("<center><form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment\" method=POST>\n"
            + "<input type=hidden name=commentId value="
            + commentId
            + ">\n"
            + "<input type=hidden name=storyId value="
            + storyId
            + ">\n"
            + "<input type=hidden name=comment_table value="
            + comment_table
            + ">\n" + "<B>Filter :</B>&nbsp&nbsp<SELECT name=filter>\n");

    try
    {
      stmt = conn
          .prepareStatement("SELECT rating, COUNT(rating) AS count FROM "
              + comment_table + " WHERE story_id=" + storyId
              + " GROUP BY rating ORDER BY rating");
      rs = stmt.executeQuery();

      i = -1;
      if (rs.first())
      {
        do
        {
          rating = rs.getInt("rating");
          count = rs.getInt("count");
          while ((i < 6) && (rating != i))
          {
            if (i == filter)
              sp.printHTML("<OPTION selected value=\"" + i + "\">" + i
                  + ": 0 comment</OPTION>\n");
            else
              sp.printHTML("<OPTION value=\"" + i + "\">" + i
                  + ": 0 comment</OPTION>\n");
            i++;
          }
          if (rating == i)
          {
            if (i == filter)
              sp.printHTML("<OPTION selected value=\"" + i + "\">" + i + ": "
                  + count + " comments</OPTION>\n");
            else
              sp.printHTML("<OPTION value=\"" + i + "\">" + i + ": " + count
                  + " comments</OPTION>\n");
            i++;
          }
        }
        while (rs.next());
      }
      stmt.close();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for View Comment: " + e);
      closeConnection(stmt, conn);
      return;
    }

    while (i < 6)
    {
      sp.printHTML("<OPTION value=\"" + i + "\">" + i
          + ": 0 comment</OPTION>\n");
      i++;
    }

    sp.printHTML("</SELECT>&nbsp&nbsp&nbsp&nbsp<SELECT name=display>\n"
        + "<OPTION value=\"0\">Main threads</OPTION>\n");
    if (display == 1)
      sp.printHTML("<OPTION selected value=\"1\">Nested</OPTION>\n");
    else
      sp.printHTML("<OPTION value=\"1\">Nested</OPTION>\n");
    if (display == 2)
      sp.printHTML("<OPTION selected value=\"2\">All comments</OPTION>\n");
    else
      sp.printHTML("<OPTION value=\"2\">All comments</OPTION>\n");
    sp
        .printHTML("</SELECT>&nbsp&nbsp&nbsp&nbsp<input type=submit value=\"Refresh display\"></center><p>\n");

    String subject, date;
    int id;
    boolean separator;
    try
    {
      stmt = conn.prepareStatement("SELECT * FROM " + comment_table
          + " WHERE story_id=" + storyId + " AND parent=0"); //+ parent+
      //" AND rating>="+filter);
      rs = stmt.executeQuery();

      while (rs.next())
      {
        username = sp.getUserName(rs.getInt("writer"), conn);
        rating = rs.getInt("rating");
        parent = rs.getInt("parent");
        id = rs.getInt("id");
        subject = rs.getString("subject");
        date = rs.getString("date");
        childs = rs.getInt("childs");
        comment = rs.getString("comment");
        separator = false;

        if (rating >= filter)
        {
          sp.printHTML("<br><hr><br>");
          separator = true;
          sp
              .printHTML("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\"><TR><TD><FONT size=\"4\" color=\"#000000\"><B><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                  + comment_table
                  + "&storyId="
                  + storyId
                  + "&commentId="
                  + id
                  + "&filter="
                  + filter
                  + "&display="
                  + display
                  + "\">"
                  + subject
                  + "</a></B>&nbsp</FONT> (Score:"
                  + rating
                  + ")</TABLE>\n");
          sp.printHTML("<TABLE><TR><TD><B>Posted by " + username + " on "
              + date + "</B><p>\n");
          sp.printHTML("<TR><TD>" + comment);
          sp
              .printHTML("<TR><TD><p>[ <a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.PostComment?comment_table="
                  + comment_table
                  + "&storyId="
                  + storyId
                  + "&parent="
                  + id
                  + "\">Reply to this</a>&nbsp|&nbsp"
                  + "<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                  + comment_table
                  + "&storyId="
                  + storyId
                  + "&commentId="
                  + parent
                  + "&filter="
                  + filter
                  + "&display="
                  + display
                  + "\">Parent</a>"
                  + "&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ModerateComment?comment_table="
                  + comment_table
                  + "&commentId="
                  + id
                  + "\">Moderate</a> ]</TABLE>\n");
        }
        if ((display > 0) && (childs > 0))
          display_follow_up(id, 1, display, filter, conn, comment_table,
                            separator, sp, conn);
      }

    }
    catch (Exception e)
    {
      closeConnection(stmt, conn);
      sp.printHTML("Exception getting categories: " + e + "<br>");
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
