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
 * Contributor(s): ______________________.
 */

package edu.rice.rubbos.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Builds the html page with the list of all categories and provides links to
 * browse all items in a category or items in a category for a given region
 */
public class ViewStory extends RubbosHttpServlet
{
  private ServletPrinter    sp   = null;
  private PreparedStatement stmt = null, stmt2 = null, stmt3 = null,
      stmt4 = null, stmt5 = null;
  private Connection        conn = null;

  public int getPoolSize()
  {
    return Config.BrowseCategoriesPoolSize;
  }

  private void closeConnection()
  {
    try
    {
      if (stmt != null)
        stmt.close(); // close statement
    }
    catch (Exception ignore)
    {
    }
  }

  public void display_follow_up(int cid, int level, int display, int filter,
      Connection link, String comment_table)
  {
    int i, childs;
    int story_id, id, writer;
    String subject, date;
    ResultSet rs2;

    try
    {

      stmt2 = conn
          .prepareStatement("SELECT id,subject,writer,date,story_id,childs FROM "
              + comment_table + " WHERE parent=" + cid);
      rs2 = stmt2.executeQuery();

      if (rs2.first())
      {
        do
        {
          for (i = 0; i < level; i++)
            sp.printHTML("&nbsp&nbsp&nbsp");

          date = rs2.getString("date");
          story_id = rs2.getInt("story_id");
          id = rs2.getInt("id");
          subject = rs2.getString("subject");
          writer = rs2.getInt("writer");
          childs = rs2.getInt("childs");

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
                  + subject + "</a> by " + writer + " on " + date + "<br>\n");
          if (childs > 0)
            display_follow_up(id, level + 1, display, filter, link,
                comment_table);
        }
        while (rs2.next());
      }
    }
    catch (Exception e3)
    {
      sp.printHTML(e3 + ": Exception in method display_follow_up");
    }

  }

  /** Build the html page for the response */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {

    sp = new ServletPrinter(response, "ViewStory");

    String categoryName, nickname, title = null, body = null, category, table;
    String password = null, date = null, username = null;
    int userId, access, storyId = 0;
    ResultSet rs = null, rs3 = null, rs5, count_result;
    String comment_table = null;
    String storyIdtest = request.getParameter("storyId");

    conn = getConnection();

    if (storyIdtest == null)
    {
      sp.printHTML("You must provide a story identifier!<br>");
      return;
    }

    if (storyIdtest != null)
    {
      storyId = (Integer.valueOf(request.getParameter("storyId"))).intValue();
    }

    try
    {
      stmt = conn.prepareStatement("SELECT * FROM stories WHERE id=" + storyId);
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("ERROR: ViewStory query failed" + e);
      closeConnection();
      return;
    }

    try
    {
      if (!rs.first())
      {
        stmt = conn.prepareStatement("SELECT * FROM old_stories WHERE id="
            + storyId);
        rs = stmt.executeQuery();
        comment_table = "old_comments";

      }
      else
      {
        comment_table = "comments";
      }

      if (!rs.first())
      {
        sp
            .printHTML("<h3>ERROR: Sorry, but this story does not exist.</h3><br>\n");
      }

      username = sp.getUserName(rs.getInt("writer"), conn);
      date = rs.getString("date");
      title = rs.getString("title");
      body = rs.getString("body");
    }

    catch (Exception e)
    {
      sp.printHTML("Exception viewing story " + e + "<br>");
      closeConnection();
    }

    sp.printHTMLheader("RUBBoS: Viewing story " + title);
    sp.printHTMLHighlighted(title);
    sp.printHTML("Posted by " + username + " on " + date + "<br>\n");
    sp.printHTML(body + "<br>\n");
    sp
        .printHTML("<p><center><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.PostComment?comment_table="
            + comment_table
            + "&storyId="
            + storyId
            + "&parent=0\">Post a comment on this story</a></center><p>");

    // Display filter chooser header
    sp.printHTML("<br><hr><br>");
    sp
        .printHTML("<center><form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment\" method=POST>\n"
            + "<input type=hidden name=commentId value=0>\n"
            + "<input type=hidden name=storyId value="
            + storyId
            + ">\n"
            + "<input type=hidden name=comment_table value="
            + comment_table
            + ">\n" + "<B>Filter :</B>&nbsp&nbsp<SELECT name=filter>\n");

    int i = -1, rating;
    String count;
    int filter, display;

    try
    {
      stmt4 = conn
          .prepareStatement("SELECT rating, COUNT(rating) AS count FROM "
              + comment_table + " WHERE story_id=" + storyId
              + " GROUP BY rating ORDER BY rating");
      count_result = stmt4.executeQuery();

      while (count_result.next())
      {
        rating = count_result.getInt("rating");
        count = count_result.getString("count");
        filter = 0;
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
    }

    catch (Exception e2)
    {
      sp.printHTML("count_result failed " + e2 + "<br>");
    }

    while (i < 6)
    {
      sp.printHTML("<OPTION value=\"" + i + "\">" + i
          + ": 0 comment</OPTION>\n");
      i++;
    }

    sp
        .printHTML("</SELECT>&nbsp&nbsp&nbsp&nbsp<SELECT name=display>\n"
            + "<OPTION value=\"0\">Main threads</OPTION>\n"
            + "<OPTION selected value=\"1\">Nested</OPTION>\n"
            + "<OPTION value=\"2\">All comments</OPTION>\n"
            + "</SELECT>&nbsp&nbsp&nbsp&nbsp<input type=submit value=\"Refresh display\"></center><p>\n");
    display = 1;
    filter = 0;

    try
    {
      stmt5 = conn.prepareStatement("SELECT * FROM " + comment_table
          + " WHERE story_id=" + storyId + " AND parent=0 AND rating>="
          + filter);
      rs5 = stmt5.executeQuery();
      String subject, comment, writer, link;
      int childs, parent, id;

      if (rs5.first())
      {
        do
        {
          username = sp.getUserName(rs5.getInt("writer"), conn);
          subject = rs5.getString("subject");
          rating = rs5.getInt("rating");
          date = rs5.getString("subject");
          comment = rs5.getString("comment");
          id = rs5.getInt("id");
          parent = rs5.getInt("parent");
          childs = rs5.getInt("childs");

          sp.printHTML("<br><hr><br>");
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
                  + "<a href=\"  /rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
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
          if (childs > 0)
            display_follow_up(id, 1, display, filter, conn, comment_table);
        }
        while (rs5.next());
      }

    }

    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for ViewStory: " + e);
      closeConnection();
      return;
    }

    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doGet(request, response);
  }

}
