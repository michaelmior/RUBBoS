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

public class OlderStories extends RubbosHttpServlet
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

    ServletPrinter    sp = null;
    PreparedStatement stmt = null;
    Connection        conn = null;

    sp = new ServletPrinter(response, "OlderStories");

    String day, month, year, testpage, username, testnbOfStories;
    int page = 0, nbOfStories = 0, id;
    ResultSet rs = null;

    testpage = request.getParameter("page");
    testnbOfStories = request.getParameter("nbOfStories");
    day = request.getParameter("day");
    month = request.getParameter("month");
    year = request.getParameter("year");

    if (testpage != null)
    {
      page = (Integer.valueOf(request.getParameter("page"))).intValue();
    }

    if (testpage == null)
    {
      page = 0;
    }

    if (month == null)
    {
      month = request.getParameter("month");
    }

    if (day == null)
    {
      day = request.getParameter("day");
    }

    if (year == null)
    {
      year = request.getParameter("year");
    }

    if (testnbOfStories != null)
    {
      nbOfStories = (Integer.valueOf(request.getParameter("nbOfStories")))
          .intValue();
    }
    else
      nbOfStories = 25;

    sp.printHTMLheader("RUBBoS Older Stories");

    // Display the date chooser
    sp
        .printHTML("<form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories\" method=POST>\n");
    sp.printHTML("<center><B>Date (day/month/year):</B><SELECT name=day>\n");
    // XXX - Fix these values to depend on now()?
    for (int i = 1; i < 32; i++)
      sp.printHTML("<OPTION value=\"" + i + "\">" + i + "</OPTION>\n");
    sp.printHTML("</SELECT>&nbsp/&nbsp<SELECT name=month>\n");
    for (int i = 1; i < 13; i++)
      sp.printHTML("<OPTION value=\"" + i + "\">" + i + "</OPTION>\n");
    sp.printHTML("</SELECT>&nbsp/&nbsp<SELECT name=year>\n");
    for (int i = 2003; i < 2013; i++)
      sp.printHTML("<OPTION value=\"" + i + "\">" + i + "</OPTION>\n");
    sp
        .printHTML("</SELECT><p><input type=submit value=\"Retrieve stories from this date!\"><p>\n");

    if ((day == null) || (month == null) || (year == null))
      sp.printHTML("<br><h2>Please select a date</h2><br>");
    else
    {
      sp.printHTML("<br><h2>Stories of the " + day + "/" + month + "/" + year
          + "</h2></center><br>");

      String before, after;
      before = year + "-" + month + "-" + day + " 0:0:0";
      after = year + "-" + month + "-" + day + " 23:59:59";

      conn = getConnection();

      try
      {
        stmt = conn.prepareStatement("SELECT stories.id, stories.title, "
            + "stories.date, users.nickname "
            + "FROM stories, users WHERE date>='" + before + "' AND date<='"
            + after + "' AND users.id = stories.writer"
            + " ORDER BY date DESC LIMIT " + page * nbOfStories + ","
            + nbOfStories);
        rs = stmt.executeQuery();
        if (!rs.first())
        {
          stmt.close();
          stmt = conn
              .prepareStatement("SELECT old_stories.id, old_stories.title,"
                  + " old_stories.date, users.nickname "
                  + "FROM old_stories, users WHERE date>='" + before
                  + "' AND date<='" + after
                  + "' AND users.id = old_stories.writer"
                  + " ORDER BY date DESC LIMIT " + page * nbOfStories + ","
                  + nbOfStories);
          rs = stmt.executeQuery();
        }
        if (!rs.first())
        {
          if (page == 0)
            sp
                .printHTML("<h2>Sorry, but there are no story available for this date !</h2>");
          else
          {
            sp
                .printHTML("<h2>Sorry, but there is no more stories available for this date.</h2><br>\n");
            sp
                .printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?day="
                    + day
                    + "&month="
                    + month
                    + "&year="
                    + year
                    + "&page="
                    + (page - 1)
                    + "&nbOfStories="
                    + nbOfStories
                    + "\">Previous page</a>\n</CENTER>\n");
          }
          sp.printHTMLfooter();
          closeConnection(stmt, conn);
          return;
        }
      }
      catch (Exception e)
      {
        sp.printHTML("Exception getting older stories: " + e + "<br>");
        closeConnection(stmt, conn);
        return;
      }

      String title, date;
      // Print the story titles and author

      try
      {
        while (rs.next())
        {
          id = rs.getInt("id");
          title = rs.getString("title");
          username = rs.getString("nickname");
          date = rs.getString("date");
          sp
              .printHTML("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewStory?storyId="
                  + id
                  + "\">"
                  + title
                  + "</a> by "
                  + username
                  + " on "
                  + date
                  + "<br>\n");
        }
      }
      catch (Exception e2)
      {
        sp.printHTML("Exception getting strings: " + e2 + "<br>");
      }

      closeConnection(stmt, conn);

      if (page == 0)
        sp
            .printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?day="
                + day
                + "&month="
                + month
                + "&year="
                + year
                + "&page="
                + (page + 1)
                + "&nbOfStories="
                + nbOfStories
                + "\">Next page</a>\n</CENTER>\n");
      else
        sp
            .printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?day="
                + day
                + "&month="
                + month
                + "&year="
                + year
                + "&page="
                + (page - 1)
                + "&nbOfStories="
                + nbOfStories
                + "\">Previous page</a>\n&nbsp&nbsp&nbsp"
                + "<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.OlderStories?category="
                + day
                + "="
                + day
                + "&month="
                + month
                + "&year="
                + year
                + "&page="
                + (page + 1)
                + "&nbOfStories="
                + nbOfStories
                + "\">Next page</a>\n\n</CENTER>\n");
    }
    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doGet(request, response);
  }

}
