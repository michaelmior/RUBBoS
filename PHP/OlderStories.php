<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "OlderStories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $day = getSessionPostGetParam('day');
      
    $month = getSessionPostGetParam('month');
      
    $year = getSessionPostGetParam('year');
      
    $page = getSessionPostGetParam('page', 0);
      
    $nbOfStories = getSessionPostGetParam('nbOfStories', 25);

    printHTMLheader("RUBBoS Older Stories");

    // Display the date chooser
    print("<form action=\"/PHP/OlderStories.php\" method=POST>\n");
    print("<center><B>Date (day/month/year):</B><SELECT name=day>\n");
    for ($i = 1 ; $i < 32 ; $i++)
      print("<OPTION value=\"$i\">$i</OPTION>\n");      
    print("</SELECT>&nbsp/&nbsp<SELECT name=month>\n");
    for ($i = 1 ; $i < 13 ; $i++)
      print("<OPTION value=\"$i\">$i</OPTION>\n");      
    print("</SELECT>&nbsp/&nbsp<SELECT name=year>\n");
    for ($i = 2001 ; $i < 2013 ; $i++)
      print("<OPTION value=\"$i\">$i</OPTION>\n");      
    print("</SELECT><p><input type=submit value=\"Retrieve stories from this date!\"><p>\n");

    // Display the results
    if (is_null($day) || is_null($month) || is_null($year))
      print("<br><h2>Please select a date</h2><br>");
    else
    {
      print("<br><h2>Stories of the $day/$month/$year</h2></center><br>");

      getDatabaseLink($link);
      $before = $year."-".$month."-".$day." 0:0:0";
      $after = $year."-".$month."-".$day." 23:59:59";
      $result = mysql_query("SELECT * FROM stories WHERE date>='$before' AND date<='$after' ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link);
	  if (!$result)
	  {
		error_log("[".__FILE__."] Query 'SELECT * FROM stories WHERE date>='$before' AND date<='$after' ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories' failed: " . mysql_error($link));
		die("ERROR: Query failed for before date '$before', after date '$after', page '$page' and nbOfStories '$nbOfStories' from stories: " . mysql_error($link));
	  }
      if (mysql_num_rows($result) == 0)
        $result = mysql_query("SELECT * FROM old_stories WHERE date>='$before' AND date<='$after' ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link);
		if (!$result)
		{
			error_log("[".__FILE__."] Query 'SELECT * FROM old_stories WHERE date>='$before' AND date<='$after' ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories' failed: " . mysql_error($link));
			die("ERROR: Query failed for before date '$before', after date '$after', page '$page' and nbOfStories '$nbOfStories' from old_stories: " . mysql_error($link));
		}
      if (mysql_num_rows($result) == 0)
      {
        if ($page == 0)
          print("<h2>Sorry, but there are no story available for this date !</h2>");
        else
        {
          print("<h2>Sorry, but there is no more stories available for this date.</h2><br>\n");
          print("<p><CENTER>\n<a href=\"/PHP/OlderStories.php?day=$day&month=$month&year=$year&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
        }
        mysql_free_result($result);
        mysql_close($link);
        printHTMLfooter($scriptName, $startTime);
        exit();
      }

      // Print the story titles and author
      while ($row = mysql_fetch_array($result))
      {
        $username = getUserName($row["writer"], $link);
        print("<a href=\"/PHP/ViewStory.php?storyId=".$row["id"]."\">".$row["title"]."</a> by ".$username." on ".$row["date"]."<br>\n");
      }
      
      // Previous/Next links
      if ($page == 0)
        print("<p><CENTER>\n<a href=\"/PHP/OlderStories.php?day=$day&month=$month&year=$year&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n</CENTER>\n");
      else
        print("<p><CENTER>\n<a href=\"/PHP/OlderStories.php?day=$day&month=$month&year=$year&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n&nbsp&nbsp&nbsp".
              "<a href=\"/PHP/OlderStories.php?day=$day&month=$month&year=$year&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n\n</CENTER>\n");
      
      mysql_free_result($result);
      mysql_close($link);
    }

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
