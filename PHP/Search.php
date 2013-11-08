<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "Search.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	if (isset($_POST['type']))
	{
		$type = $_POST['type'];
	}
    elseif (isset($_GET['type']))
    {
      $type = $_GET['type'];
	}
	else
	{
      $type = 0;
    }

	if (isset($_POST['search']))
	{
    	$search = $_POST['search'];
	}
    elseif (isset($_GET['search']))
    {
      $search = $_GET['search'];
    }
	else
	{
	  $search = NULL;
	}

	if (isset($_POST['page']))
	{
    	$page = $_POST['page'];
	}
    elseif (isset($_GET['page']))
    {
      $page = $_GET['page'];
	}
	else
	{
      $page = 0;
    }
      
	if (isset($_POST['nbOfStories']))
	{
    	$nbOfStories = $_POST['nbOfStories'];
	}
    elseif (isset($_GET['nbOfStories']))
    {
      $nbOfStories = $_GET['nbOfStories'];
	}
	else
	{
      $nbOfStories = 25;
    }

    printHTMLheader("RUBBoS search");

    // Display the search form
    print("<form action=\"/PHP/Search.php\" method=GET>\n".
          "<center><table>\n".
          "<tr><td><b>Search</b><td><input type=text size=50 name=search value=$search>\n".
          "<tr><td><b>in</b><td><SELECT name=type>\n");
    if ($type == 0)
    {
      print("<OPTION selected value=\"0\">Stories</OPTION>\n");
      $table = "stories";
      $title = "Stories";
    }
    else
      print("<OPTION value=\"0\">Stories</OPTION>\n");
    if ($type == 1)
    {
      print("<OPTION selected value=\"1\">Comments</OPTION>\n");
      $table = "comments";
      $title = "Comments";
    }
    else
      print("<OPTION value=\"1\">Comments</OPTION>\n");
    if ($type == 2)
    {
      print("<OPTION selected value=\"2\">Authors</OPTION>\n");
      $table = "users";
      $title = "Stories with author";
    }
    else
      print("<OPTION value=\"2\">Authors</OPTION>\n");
    print("</SELECT></table><p><br>\n".
          "<input type=submit value=\"Search now!\"></center><p>\n");


    // Display the results
    if ($search == null)
      print("<br><center><h2>Please select a text to search for</h2></center><br>");
    else
    {
      print("<br><h2>$title matching <i>$search</i></h2></center><br>");

      getDatabaseLink($link);
      if ($type == 0)
      { // Look for stories
        $result = mysql_query("SELECT id, title, date, writer FROM stories WHERE title LIKE '$search%' "./*OR body LIKE '$search%%'*/" ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: Query failed");
        if (mysql_num_rows($result) == 0)
          $result = mysql_query("SELECT id, title, date, writer FROM old_stories WHERE title LIKE '$search%' "./*OR body LIKE '$search%%'*/" ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: Query failed");
        if (mysql_num_rows($result) == 0)
        {
          if ($page == 0)
            print("<h2>Sorry, but there is no story matching <i>$search</i> !</h2>");
          else
          {
            print("<h2>Sorry, but there are no more stories available matching <i>$search</i>.</h2><br>\n");
            print("<p><CENTER>\n<a href=\"/PHP/Search.php?search=".urlencode($search)."&type=$type&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
          }
          mysql_free_result($result);
          mysql_close($link);
          printHTMLfooter($scriptName, $startTime);
          exit();
        }
      }
      if ($type == 1)
      { // Look for comments
        $comment_table = "comments";
        $result = mysql_query("SELECT id,story_id,subject,writer,date FROM comments WHERE subject LIKE '$search%' "./*OR comment LIKE '$search%%'*/" GROUP BY story_id ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: Query failed");
        if (mysql_num_rows($result) == 0)
        {
          $result = mysql_query("SELECT id,story_id,subject,writer,date FROM old_comments WHERE subject LIKE '$search%' "./*OR comment LIKE '$search%%'*/" ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: Query failed");
          $comment_table = "old_comments";
        }
        if (mysql_num_rows($result) == 0)
        {
          if ($page == 0)
            print("<h2>Sorry, but there is no comment matching <i>$search</i> !</h2>");
          else
          {
            print("<h2>Sorry, but there are no more comments available matching <i>$search</i>.</h2><br>\n");
            print("<p><CENTER>\n<a href=\"/PHP/Search.php?search=".urlencode($search)."&type=$type&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
          }
          mysql_free_result($result);
          mysql_close($link);
          printHTMLfooter($scriptName, $startTime);
          exit();
        }
        else
        {
          // Print the comment subject and author
          while ($row = mysql_fetch_array($result))
            print("<a href=\"/PHP/ViewComment.php?comment_table=$comment_table&storyId=".$row["story_id"]."&commentId=".$row["id"]."&filter=0&display=0\">".$row["subject"]."</a> by ".getUserName($row["writer"], $link)." on ".$row["date"]."<br>\n");
        }
        
      }
      if ($type == 2)
      { // Look for stories of an author
        $result = mysql_query("SELECT stories.id, stories.title, stories.date, stories.writer FROM stories,users WHERE writer=users.id AND "./*(users.firstname LIKE '$search%%' OR users.lastname LIKE '$search%%' OR*/" users.nickname LIKE '$search%'"./*)*/" ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: stories query failed");
        if (mysql_num_rows($result) == 0)
          $result = mysql_query("SELECT old_stories.id, old_stories.title, old_stories.date, old_stories.writer FROM old_stories,users WHERE writer=users.id AND "./*(users.firstname LIKE '$search%%' OR users.lastname LIKE '$search%%' OR*/" users.nickname LIKE '$search%'"./*)*/" ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: old_stories query failed");
        if (mysql_num_rows($result) == 0)
        {
          if ($page == 0)
            print("<h2>Sorry, but there is no story with author matching <i>$search</i> !</h2>");
          else
          {
            print("<h2>Sorry, but there are no more stories available with author matching <i>$search</i>.</h2><br>\n");
            print("<p><CENTER>\n<a href=\"/PHP/Search.php?search=".urlencode($search)."&type=$type&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
          }
          mysql_free_result($result);
          mysql_close($link);
          printHTMLfooter($scriptName, $startTime);
          exit();
        }
      }

      if ($type != 1)
      {
        // Print the story titles and author
        while ($row = mysql_fetch_array($result))
        {
          $username = getUserName($row["writer"], $link);
          print("<a href=\"/PHP/ViewStory.php?storyId=".$row["id"]."\">".$row["title"]."</a> by ".$username." on ".$row["date"]."<br>\n");
        }
      }
          
      // Previous/Next links
      if ($page == 0)
        print("<p><CENTER>\n<a href=\"/PHP/Search.php?search=".urlencode($search)."&type=$type&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n</CENTER>\n");
      else
        print("<p><CENTER>\n<a href=\"/PHP/Search.php?search=".urlencode($search)."&type=$type&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n&nbsp&nbsp&nbsp".
              "<a href=\"/PHP/Search.php?category=$search=".urlencode($search)."&type=$type&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n\n</CENTER>\n");
      
      mysql_free_result($result);
      mysql_close($link);          
    } 
   
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
