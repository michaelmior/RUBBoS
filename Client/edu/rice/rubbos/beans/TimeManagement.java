package edu.rice.rubbos.beans;

import java.util.GregorianCalendar;
import java.lang.String;

/** This class provides additionnal functions that the GregorianCalendar
 * class does not provide. It is mainly to compute time differences and
 * display the date in a database understandable format.
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class TimeManagement extends GregorianCalendar
{

  /**
   * Creates a new <code>TimeManagement</code> instance.
   *
   */
  public TimeManagement()
  {
  }


  /** Returns a string representation of the current date (when the method is called)
   * conforming to the following database format : 'YYYY-MM-DD hh:mm:ss'
   *
   * @return current date in database format
   *
   */
  public static String currentDateToString()
  {
    GregorianCalendar d = new GregorianCalendar();
    return dateToString(d);
  }


  /**
   * Returns a string representation of a date conforming to 
   * the following database format : 'YYYY-MM-DD hh:mm:ss'
   *
   * @param d a <code>GregorianCalendar</code> value
   * @return current date in database format
   *
   */
  public static String dateToString(GregorianCalendar d)
  {
    String result;
    int    year, month, day, hour, minute, second;

    year   = d.get(GregorianCalendar.YEAR);
    month  = d.get(GregorianCalendar.MONTH) + 1;
    day    = d.get(GregorianCalendar.DATE);
    hour   = d.get(GregorianCalendar.HOUR_OF_DAY);
    minute = d.get(GregorianCalendar.MINUTE);
    second = d.get(GregorianCalendar.SECOND);
    result = year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second;
    return result;
  }


  /** Returns the time elapsed between startDate and endDate in milliseconds
   *
   * @param startDate beginning date
   * @param endDate ending date
   *
   * @return elapsed time in milliseconds
   *
   */
  public static long diffTimeInMs(GregorianCalendar startDate, GregorianCalendar endDate)
  {
    long  year, month, day, hour, minute, second, millis;

    year   = endDate.get(GregorianCalendar.YEAR)-startDate.get(GregorianCalendar.YEAR);
    month  = endDate.get(GregorianCalendar.MONTH)-startDate.get(GregorianCalendar.MONTH);
    day    = endDate.get(GregorianCalendar.DATE)-startDate.get(GregorianCalendar.DATE);
    hour   = endDate.get(GregorianCalendar.HOUR_OF_DAY)-startDate.get(GregorianCalendar.HOUR_OF_DAY);
    minute = endDate.get(GregorianCalendar.MINUTE)-startDate.get(GregorianCalendar.MINUTE);
    second = endDate.get(GregorianCalendar.SECOND)-startDate.get(GregorianCalendar.SECOND);
    millis = endDate.get(GregorianCalendar.MILLISECOND)-startDate.get(GregorianCalendar.MILLISECOND);
 
    if (millis < 0)
    {
      millis = millis + 1000;
      second = second - 1;
    }
    if (second < 0)
    {
      second = second + 60;
      minute = minute - 1;
    }
    if (minute < 0)
    {
      minute = minute + 60;
      hour = hour - 1;
    }
    if (hour < 0)
    {
      hour = hour + 24;
      day = day - 1;
    }
    if (day < 0)
    {
      day = day + startDate.getActualMaximum(GregorianCalendar.DAY_OF_MONTH); // is the same as startDate.DATE
      month = month - 1;
    }
    if (month < 0)
    {
      month = month + 12;
      year = year - 1;
    }
    return millis+(second*1000)+(minute*60000)+(hour*3600000)+(day*24*3600000)+(month*30*24*3600000); // We approximate 30 days per month
  }


  /** Returns a string representation of the time elapsed between startDate
   * and endDate. Example of a returned string : "1 month 3 days 6 hours 33 minutes 4 seconds 234 milliseconds"
   *
   * @param startDate beginning date
   * @param endDate ending date
   *
   * @return string containing the time difference up to the millisecond
   *
   */
  public static String diffTime(GregorianCalendar startDate, GregorianCalendar endDate)
  {
    int    year, month, day, hour, minute, second, millis;
    String result = "";

    year   = endDate.get(GregorianCalendar.YEAR)-startDate.get(GregorianCalendar.YEAR);
    month  = endDate.get(GregorianCalendar.MONTH)-startDate.get(GregorianCalendar.MONTH);
    day    = endDate.get(GregorianCalendar.DATE)-startDate.get(GregorianCalendar.DATE);
    hour   = endDate.get(GregorianCalendar.HOUR_OF_DAY)-startDate.get(GregorianCalendar.HOUR_OF_DAY);
    minute = endDate.get(GregorianCalendar.MINUTE)-startDate.get(GregorianCalendar.MINUTE);
    second = endDate.get(GregorianCalendar.SECOND)-startDate.get(GregorianCalendar.SECOND);
    millis = endDate.get(GregorianCalendar.MILLISECOND)-startDate.get(GregorianCalendar.MILLISECOND);
 
    if (millis < 0)
    {
      millis = millis + 1000;
      second = second - 1;
    }
    if (second < 0)
    {
      second = second + 60;
      minute = minute - 1;
    }
    if (minute < 0)
    {
      minute = minute + 60;
      hour = hour - 1;
    }
    if (hour < 0)
    {
      hour = hour + 24;
      day = day - 1;
    }
    if (day < 0)
    {
      day = day + startDate.getActualMaximum(GregorianCalendar.DAY_OF_MONTH); // is the same as startDate.DATE
      month = month - 1;
    }
    if (month < 0)
    {
      month = month + 12;
      year = year - 1;
    }

    if (year > 0)
      result = year+" year";
    if (year > 1)
      result = result+"s ";
    else
      result = result+" ";
    if (month > 0)
      result = result+month+" month";
    if (month > 1)
      result = result+"s ";
    else
      result = result+" ";
    if (day > 0)
      result = result+day+" day";
    if (day > 1)
      result = result+"s ";
    else
      result = result+" ";
    if (hour > 0)
      result = result+hour+" hour";
    if (hour > 1)
      result = result+"s ";
    else
      result = result+" ";
    if (minute > 0)
      result = result+minute+" minute";
    if (minute > 1)
      result = result+"s ";
    else
      result = result+" ";
    if (second > 0)
      result = result+second+" second";
    if (second > 1)
      result = result+"s ";
    else
      result = result+" ";
    result = result+millis+" millisecond";
    if (millis > 1)
      result = result+"s ";
    else
      result = result+" ";
    return result;
  }


  /** Compute a new GregorianCalendar from a beginning date and a duration in days.
   *
   * @param startDate beginning date
   * @param durationInDays number of days to add to startDate.
   *
   * @return date corresponding to startDate+durationInDays
   *
   */
  public static GregorianCalendar addDays(GregorianCalendar startDate, int durationInDays)
  {
    GregorianCalendar date = (GregorianCalendar)startDate.clone();
    date.add(GregorianCalendar.DATE, durationInDays);
    return date;
  }
}
