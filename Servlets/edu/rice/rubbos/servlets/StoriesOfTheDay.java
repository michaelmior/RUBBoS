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

public class StoriesOfTheDay extends RubbosHttpServlet
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

    int               bodySizeLimit = 512;
    ResultSet         rs = null;

    sp = new ServletPrinter(response, "StoriesOfTheDay");
    sp.printHTMLheader("RUBBoS stories of the day");

    conn = getConnection();

    try
    {
      stmt = conn
          .prepareStatement("SELECT  stories.id, stories.title, stories.body,"
              + " stories.date, stories.category, users.nickname"
              + " FROM stories, users " + "WHERE stories.writer=users.id"
              + " ORDER BY date DESC LIMIT 10");
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for stories of the day: " + e);
      closeConnection(stmt, conn);
      return;
    }
    try
    {
      if (!rs.first())
      {
        sp
            .printHTML("<h2>Sorry, but there is no story available at this time.</h2><br>\n");
        closeConnection(stmt, conn);
        return;
      }

      int storyId;
      String storyTitle;
      String userName;
      String date;
      String body;
      do
      {
        sp.printHTML("<br><hr>\n");
        storyId = rs.getInt("id");
        storyTitle = rs.getString("title");
        sp
            .printHTMLHighlighted("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewStory?storyId="
                + storyId + "\">" + storyTitle + "</a>");

        userName = rs.getString("nickname");
        date = rs.getString("date");
        sp.printHTML("<B>Posted by " + userName + " on " + date + "</B><br>\n");
        body = rs.getString("body");
        if (body.length() > bodySizeLimit)
        {
          sp.printHTML(body.substring(0, bodySizeLimit));
          sp.printHTML("<br><B>...</B>");
        }
        else
          sp.printHTML(body);
        sp.printHTML("<br>\n");
      }
      while (rs.next());
    }
    catch (Exception e)
    {
      sp.printHTML("Exception getting stories of the day: " + e + "<br>");
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
