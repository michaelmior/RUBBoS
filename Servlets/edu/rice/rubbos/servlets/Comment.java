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
 * Initial developer(s): Niraj Tolia.
 * Contributor(s): 
 */

package edu.rice.rubbos.servlets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;

// Package class
class Comment
{

  public int    id;
  public String    story_id;
  public int    parent;
  public int    childs;
  public int    rating;
  public String date;
  public String subject;
  public String comment;
  public String username;
  
  public Comment(int id, String story_id, int parent, int childs,
      int rating, String date, String subject, String comment,
      String username)
  {
    this.id = id;
    this.story_id = story_id;
    this.parent = parent;
    this.childs = childs;
    this.rating = rating;
    this.date = date;
    this.subject = subject;
    this.comment = comment;
    this.username = username;
  }

  public static void displayFilterChooser(Connection conn, ServletPrinter sp,
      int commentId, String storyId, String commentTable, int display,
      int filter) throws Exception
  {
    int i, count, rating;
    PreparedStatement stmt;
    ResultSet rs;

    sp
        .printHTML("<center><form action=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment\" method=POST>\n"
            + "<input type=hidden name=commentId value="
            + commentId
            + ">\n"
            + "<input type=hidden name=storyId value="
            + storyId
            + ">\n"
            + "<input type=hidden name=comment_table value="
            + commentTable
            + ">\n" + "<B>Filter :</B>&nbsp&nbsp<SELECT name=filter>\n");

    stmt = conn.prepareStatement("SELECT rating, COUNT(rating) AS count FROM "
        + commentTable + " WHERE story_id=" + storyId
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

  }
  
  private static void fetchLowerLevels(HashMap map, int level, Connection conn,
      int filter, int commentId, String commentTable, String storyId, int cid, int display)
      throws SQLException
  {
    ResultSet         follow;
    int               childs, rating, parent, id;
    String            subject, username, date, comment;
    PreparedStatement stmtfollow = null;
    String            tableName = "t"+level;
    String            displayCommentOption = "";
    Comment           commentWrapper;
    boolean           deeper = false;
    ArrayList         al;
    
    //  If display == 1, then we do not need the actual comment and
    // therefore lets not select it
    if (display != 1) 
    {
      displayCommentOption = "(CASE WHEN "+ tableName 
      + ".rating>=" + filter 
      + " THEN  "+ tableName + ".comment ELSE NULL END)"
      + " as comment, ";
    }
    
    String sqlString = "SELECT " 
      + tableName + ".id, " 
      + tableName + ".parent, " 
      + tableName + ".childs, " 
      + tableName + ".rating, " 
      + tableName + ".date, "
      /*
      + "(CASE WHEN "
      + tableName 
      + ".rating>=" + filter
      + " THEN "+ tableName + ".subject ELSE NULL END)"
      + " as subject, "

      + displayCommentOption
      */
      + tableName + ".subject, "
      + tableName + ".comment, "
      + " users.nickname FROM users, ";

    
    
    for (int i = 0; i <= level; i++)
    {
      sqlString += commentTable + " AS t" + i;
      if (level != i)
      {
        sqlString += ", ";
      }
      else
      {
        sqlString += " ";
      }
    }
    
    sqlString += "WHERE";
    
    for (int i = level; i >= 0; i--)
    {
      if (i != 0)
      {
        sqlString += " t" + i + ".parent = t" + (i - 1) + ".id AND";
      }
      else
      {
        sqlString += " t0.parent = " + cid;
        if (commentId != 0)
        {
          sqlString += " AND t0.id ="+commentId;
          
        }
      }
    }
    
    sqlString +=  " AND " + tableName
    + ".writer=users.id" + " AND " + tableName + ".story_id=" 
    + storyId;
    /* 
    + " AND (" + tableName + ".rating >=" + filter
    + " OR " + tableName + ".childs > 0)";
    */

    //System.out.println("\nsqlString is " + sqlString);
    
    stmtfollow = conn.prepareStatement(sqlString);
    follow = stmtfollow.executeQuery();
    
    while (follow.next())
    {
      id = follow.getInt("id");
      subject = follow.getString("subject");
      username = follow.getString("nickname");
      date = follow.getString("date");
      rating = follow.getInt("rating");
      parent = follow.getInt("parent");
      childs = follow.getInt("childs");
      if (display == 1)
      {
        comment = "";
      }
      else
      {
        comment = follow.getString("comment");
      }

      commentWrapper = new Comment(id, storyId, parent, childs, rating, date,
          subject, comment, username);
      
      if ((al = (ArrayList) map.get(new Integer(parent))) == null) 
      {
        al = new ArrayList();
        al.add(commentWrapper);
        map.put(new Integer(parent), al);
        // System.out.println("!! Added with new parent"+subject);
      } 
      else 
      {
        al.add(commentWrapper);
        // System.out.println("!! Added "+subject);
      }

      if (childs > 0)
      {
        deeper = true;
      }
      
    }
    stmtfollow.close();
    
    if (deeper) 
    { 
      fetchLowerLevels(map, level+1, conn, filter, commentId, commentTable, storyId, cid, display);
    }
  
  }
  
  private static HashMap fetchComments(Connection conn, int filter,
      int commentId, String commentTable, String storyId, int parent, int display)
  throws SQLException
  {
    String subject, date, username;
    int id;
    String sqlString, commentString = "";
    int rating, childs;
    String comment;
    HashMap map = new HashMap();
    PreparedStatement stmt;
    ResultSet rs;
    Comment commentWrapper;
    boolean deeper = false;
    
    if(commentId != 0) 
    {
      commentString = " AND " + commentTable + ".id=" + commentId ;
    }

    sqlString = "SELECT " 
      + commentTable + ".id, " 
      + commentTable + ".parent, " 
      + commentTable + ".childs, " 
      + commentTable + ".rating, " 
      + commentTable + ".date, "
      /*
      + "(CASE WHEN "
      + commentTable + ".rating >="
      + filter +" THEN subject ELSE NULL END)"
      + " as subject, "
      
      + "(CASE WHEN "
      + commentTable + ".rating >="
      + filter + " THEN comment ELSE NULL END)"
      + " as comment, "
      */
      + commentTable + ".subject, "
      + commentTable + ".comment, "
      
      
      + "users.nickname FROM " 
      + commentTable + ", users WHERE " + commentTable + ".story_id=" 
      + storyId + " AND " + commentTable + ".parent="+ parent 
      + commentString + " AND " + commentTable
      + ".writer=users.id";
      /*
      + " AND (" + commentTable + ".rating >=" + filter
      + " OR " + commentTable + ".childs > 0)";
      */
    // System.out.println("\nsqlString is " + sqlString);
    
    stmt = conn.prepareStatement(sqlString);
    rs = stmt.executeQuery();
    ArrayList al;
    
    while (rs.next())
    {
      username = rs.getString("nickname");
      rating = rs.getInt("rating");
      parent = rs.getInt("parent");
      id = rs.getInt("id");
      subject = rs.getString("subject");
      date = rs.getString("date");
      childs = rs.getInt("childs");
      comment = rs.getString("comment");
      commentWrapper = new Comment(id, storyId, parent, childs, rating, date, subject, comment, username);
      if ((al = (ArrayList) map.get(new Integer(parent))) == null) 
      {
        al = new ArrayList();
        al.add(commentWrapper);
        map.put(new Integer(parent), al);
      } 
      else 
      {
        al.add(commentWrapper);
      }
      // System.out.println("Added "+subject);
      if (childs > 0)
      {
        deeper = true;
      }
      
    }
    
    stmt.close();
    
    if(deeper) 
    { 
      fetchLowerLevels(map, 1, conn, filter, commentId, commentTable, storyId,
          parent, display);
    }
  
    return map;
    
  }
  
  private static void displayLowerLevelComments(ServletPrinter sp, HashMap map,
      int cid, int filter, int display, String commentTable, int level,
      boolean separator)
  {
    ArrayList al;
    String username, date, subject, comment, storyId;
    int rating, parent, id, childs;
    int i;

    al = (ArrayList) map.get(new Integer(cid));
    
    if (al == null) 
    {
      return;
    }
    
    for(int ci = 0; ci < al.size(); ci++)
    {
      Comment commentWrapper = (Comment) al.get(ci);
      username = commentWrapper.username;
      rating = commentWrapper.rating;
      parent = commentWrapper.parent;
      id = commentWrapper.id;
      storyId = commentWrapper.story_id;
      subject = commentWrapper.subject;
      date = commentWrapper.date;
      childs = commentWrapper.childs;
      comment = commentWrapper.comment;
      
      // System.out.println("!! Displaying " + subject + "at level "+ level);
      
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
                  + commentTable
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
                  + commentTable
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
                  + commentTable
                  + "&storyId="
                  + storyId
                  + "&parent="
                  + id
                  + "\">Reply to this</a>"
                  + "&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                  + commentTable
                  + "&storyId="
                  + storyId
                  + "&commentId="
                  + parent
                  + "&filter="
                  + filter
                  + "&display="
                  + display
                  + "\">Parent</a>&nbsp|&nbsp<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ModerateComment?comment_table="
                  + commentTable
                  + "&commentId="
                  + id
                  + "\">Moderate</a> ]</TABLE><br>");
        }
      }
      if (childs > 0)
      {
        displayLowerLevelComments(sp, map, id, filter, display, commentTable, level + 1, separator);
      }
    }
  }
  
  private static void displayComments(ServletPrinter sp, HashMap map, int cid, int filter, int display, String commentTable)
  {
    ArrayList al;
    String username, date, subject, comment, storyId;
    int rating, parent, id, childs;
    boolean separator;
    
    al = (ArrayList) map.get(new Integer(cid));

    if (al == null)
    {
      return;
    }
    
    for(int i = 0; i < al.size(); i++)
    {
      Comment commentWrapper = (Comment) al.get(i);
      username = commentWrapper.username;
      rating = commentWrapper.rating;
      parent = commentWrapper.parent;
      id = commentWrapper.id;
      storyId = commentWrapper.story_id;
      subject = commentWrapper.subject;
      date = commentWrapper.date;
      childs = commentWrapper.childs;
      comment = commentWrapper.comment;
      separator = false;

      // System.out.println("! Displaying " + subject);
      
      if (rating >= filter)
      {
        sp.printHTML("<br><hr><br>");
        separator = true;
        sp
            .printHTML("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\"><TR><TD><FONT size=\"4\" color=\"#000000\"><B><a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                + commentTable
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
                + commentTable
                + "&storyId="
                + storyId
                + "&parent="
                + id
                + "\">Reply to this</a>&nbsp|&nbsp"
                + "<a href=\"/rubbos/servlet/edu.rice.rubbos.servlets.ViewComment?comment_table="
                + commentTable
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
                + commentTable
                + "&commentId="
                + id
                + "\">Moderate</a> ]</TABLE>\n");
      }
      if ((display > 0) && (childs > 0))
      {
        displayLowerLevelComments(sp, map, id, filter, display, commentTable, 1, separator);
      }     
    
      
    }
    
  }
  
  public static void fetchAndDisplay(ServletPrinter sp, Connection conn,
      String commentTable, int filter, int commentId, int display,
      String storyId, int parent) throws Exception
  {
    HashMap map = fetchComments(conn, filter, commentId, commentTable, storyId,
        parent, display);
    displayComments(sp, map, parent, filter, display, commentTable);
  }
}