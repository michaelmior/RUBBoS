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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides the method to initialize connection to the database. All the
 * servlets inherit from this class
 */
public abstract class RubbosHttpServlet extends HttpServlet
{

  private Connection conn[]      = null;
  private int        poolSize;
  private int        currentConn = 0;

  public abstract int getPoolSize(); // Get the pool size for this class

  /** Load the driver and get a connection to the database */
  public void init() throws ServletException
  {
    InputStream in = null;
    poolSize = getPoolSize();
    try
    {
      // Get the properties for the database connection
      Properties dbProperties = new Properties();
      in = new FileInputStream(Config.DatabaseProperties);
      dbProperties.load(in);
      // load the driver
      Class.forName(dbProperties.getProperty("datasource.classname"));
      conn = new Connection[poolSize];
      for (int i = 0; i < poolSize; i++)
        // Get a connection to the database
        conn[i] = DriverManager.getConnection(dbProperties
            .getProperty("datasource.url"), dbProperties
            .getProperty("datasource.username"), dbProperties
            .getProperty("datasource.password"));
    }
    catch (FileNotFoundException f)
    {
      throw new UnavailableException("Couldn't find file mysql.properties: "
          + f + "<br>");
    }
    catch (IOException io)
    {
      throw new UnavailableException("Cannot open read mysql.properties: " + io
          + "<br>");
    }
    catch (ClassNotFoundException c)
    {
      throw new UnavailableException("Couldn't load database driver: " + c
          + "<br>");
    }
    catch (SQLException s)
    {
      throw new UnavailableException("Couldn't get database connection: " + s
          + "<br>");
    }
    finally
    {
      try
      {
        if (in != null)
          in.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public Connection getConnection()
  {
    currentConn = (currentConn + 1) % poolSize;
    return conn[currentConn];
  }

  public void destroy()
  {
    try
    {
      for (int i = 0; i < poolSize; i++)
        if (conn[i] != null)
          conn[i].close(); // release connection
    }
    catch (Exception ignore)
    {
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {

  }

}