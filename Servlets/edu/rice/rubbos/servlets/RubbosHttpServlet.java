package edu.rice.rubbos.servlets;

import edu.rice.rubbos.*;
import java.io.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

/** Provides the method to initialize connection to the database. All the servlets inherit from this class */
public abstract class RubbosHttpServlet extends HttpServlet
{

  private Connection conn[] = null;
  private int poolSize;
  private int currentConn = 0;

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
      for (int i = 0 ; i < poolSize ; i++)
        // Get a connection to the database
        conn[i] = DriverManager.getConnection(dbProperties.getProperty("datasource.url"), dbProperties.getProperty("datasource.username"), dbProperties.getProperty("datasource.password"));
    }
    catch (FileNotFoundException f) 
    {
      throw new UnavailableException("Couldn't find file mysql.properties: " + f+"<br>");
    } 
    catch (IOException io) 
    {
      throw new UnavailableException("Cannot open read mysql.properties: " + io+"<br>");
    } 
    catch (ClassNotFoundException c) 
    {
      throw new UnavailableException("Couldn't load database driver: " + c+"<br>");
    }
    catch (SQLException s) 
    {
      throw new UnavailableException("Couldn't get database connection: " + s+"<br>");
    }
    finally
    {
      try
      {
        if(in != null) in.close();
      }
      catch(Exception e)
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
      for (int i = 0 ; i < poolSize ; i++)
        if (conn[i] != null) conn[i].close();	// release connection
    } 
    catch (Exception ignore) 
    {
    }
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {

  }

}
