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
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BrowseStoriesByCategory extends RubbosHttpServlet
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

    sp = new ServletPrinter(response, "BrowseStoriesByCategory");

    String categoryName, username, categoryId, testpage, testnbOfStories;
    int page = 0, nbOfStories = 0;
    ResultSet rs = null;

    testpage = request.getParameter("page");
    testnbOfStories = request.getParameter("nbOfStories");

    if (testpage != null)
    {
      page = (Integer.valueOf(request.getParameter("page"))).intValue();
    }

    if (testnbOfStories != null)
    {
      nbOfStories = (Integer.valueOf(request.getParameter("nbOfStories")))
          .intValue();
    }

    categoryId = request.getParameter("category");
    categoryName = request.getParameter("categoryName");

    if (categoryName == null)
    {
      sp.printHTML("Browse Stories By Category"
          + "You must provide a category name!<br>");
      return;
    }

    if (categoryId == null)
    {
      sp.printHTML("Browse Stories By Category"
          + "You must provide a category identifier!<br>");
      return;
    }

    if (page == 0)
    {
      page = 0;
    }

    if (nbOfStories == 0)
    {
      nbOfStories = 25;

    }
    sp.printHTMLheader("RUBBoS Browse Stories By Category");
    sp.printHTML("<br><h2>Stories in category " + categoryName + "</h2><br>");

    conn = getConnection();

    try
    {
      stmt = conn.prepareStatement("SELECT * FROM stories WHERE category= "
          + categoryId + " ORDER BY date DESC LIMIT " + page * nbOfStories
          + "," + nbOfStories);
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for BrowseStoriesByCategory: " + e);
      closeConnection(stmt, conn);
      return;
    }
    try
    {
      if (!rs.first())
      {
        if (page == 0)
        {
          sp
              .printHTML("<h2>Sorry, but there is no story available in this category !</h2>");
        }
        else
        {
          sp
              .printHTML("<h2>Sorry, but there are no more stories available at this time.</h2><br>\n");
          sp
              .printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.BrowseStoriesByCategory?category="
                  + categoryId
                  + "&categoryName="
                  + URLEncoder.encode(categoryName)
                  + "&page="
                  + (page - 1)
                  + "&nbOfStories=nbOfStories\">Previous page</a>\n</CENTER>\n");
        }
        sp.printHTMLfooter();
        closeConnection(stmt, conn);
        return;
      }
      
      do
      {
        String title = rs.getString("title");
        String date = rs.getString("date");
        username = rs.getString("writer");
        int id = rs.getInt("id");

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
      while (rs.next());
    }
    catch (Exception e)
    {
      sp.printHTML("Exception getting categories: " + e + "<br>");
    }
        
    closeConnection(stmt, conn);


    if (page == 0)
      sp
          .printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.BrowseStoriesByCategory?category="
              + categoryId
              + "&categoryName="
              + URLEncoder.encode(categoryName)
              + "&page="
              + (page + 1)
              + "&nbOfStories="+nbOfStories+"\">Next page</a>\n</CENTER>\n");
    else
      sp
          .printHTML("<p><CENTER>\n<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.BrowseStoriesByCategory?category="
              + categoryId
              + "&categoryName="
              + URLEncoder.encode(categoryName)
              + "&page="
              + (page - 1)
              + "&nbOfStories="+nbOfStories+"\">Previous page</a>\n&nbsp&nbsp&nbsp"
              + "<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.BrowseStoriesByCategory?category="
              + categoryId
              + "&categoryName="
              + URLEncoder.encode(categoryName)
              + "&page="
              + (page + 1)
              + "&nbOfStories="+nbOfStories+"\">Next page</a>\n\n</CENTER>\n");

    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doGet(request, response);
  }

}
