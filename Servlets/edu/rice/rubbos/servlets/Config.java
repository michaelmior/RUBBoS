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

/**
 * This class contains the configuration for the servlets like the path of HTML
 * files, etc ...
 * 
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet </a> and <a
 *         href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite </a>
 * @version 1.0
 */

public class Config
{

  /**
   * Creates a new <code>Config</code> instance.
   */
  Config()
  {
  }

  public static final String HTMLFilesPath                 = "/home/margueri/RUBBoS/Servlet_HTML";
  public static final String DatabaseProperties            = "/home/margueri/RUBBoS/Servlets/mysql.properties";

  public static final int    AboutMePoolSize               = 10;
  public static final int    BrowseCategoriesPoolSize      = 6;
  public static final int    BrowseRegionsPoolSize         = 6;
  public static final int    BuyNowPoolSize                = 4;
  public static final int    PutBidPoolSize                = 8;
  public static final int    PutCommentPoolSize            = 2;
  public static final int    RegisterItemPoolSize          = 2;
  public static final int    RegisterUserPoolSize          = 2;
  public static final int    SearchItemsByCategoryPoolSize = 15;
  public static final int    SearchItemsByRegionPoolSize   = 20;
  public static final int    StoreBidPoolSize              = 8;
  public static final int    StoreBuyNowPoolSize           = 4;
  public static final int    StoreCommentPoolSize          = 2;
  public static final int    ViewBidHistoryPoolSize        = 4;
  public static final int    ViewItemPoolSize              = 20;
  public static final int    ViewUserInfoPoolSize          = 4;
}