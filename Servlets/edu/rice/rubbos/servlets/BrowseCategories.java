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
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BrowseCategories extends RubbosHttpServlet
{
  private ServletPrinter    sp   = null;
  private PreparedStatement stmt = null;
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

  /** Build the html page for the response */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    sp = new ServletPrinter(response, "BrowseCategories");
    sp.printHTMLheader("RUBBoS available categories");

    conn = getConnection();

    ResultSet rs = null;
    try
    {
      stmt = conn.prepareStatement("SELECT * FROM categories");
      rs = stmt.executeQuery();
    }
    catch (Exception e)
    {
      sp.printHTML("Failed to execute Query for BrowseCategories: " + e);
      closeConnection();
      return;
    }
    try
    {
      if (!rs.first())
      {
        sp
            .printHTML("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
        closeConnection();
        return;
      }
      else
        sp.printHTML("<h2>Currently available categories</h2><br>\n");

      int categoryId;
      String categoryName;

      do
      {
        categoryId = rs.getInt("id");
        categoryName = rs.getString("name");
        sp
            .printHTMLHighlighted("<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.BrowseStoriesByCategory?category="
                + categoryId
                + "&categoryName="
                + URLEncoder.encode(categoryName)
                + "\">"
                + categoryName
                + "</a><br>\n");
      }
      while (rs.next());
    }
    catch (Exception e)
    {
      sp.printHTML("Exception getting categories: " + e + "<br>");
      closeConnection();
    }

    sp.printHTMLfooter();

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doGet(request, response);
  }

}