<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "Author.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
    $nickname = getSessionPostGetParam('nickname');
    if (!isset($nickname))
	{
      printError($scriptName, $startTime, "Author", "You must provide a nick name!");
      exit();
    }

    $password = getSessionPostGetParam('password');
    if (!isset($password))
	{
      printError($scriptName, $startTime, "Author", "You must provide a password!");
      exit();
    }

    getDatabaseLink($link);

    printHTMLheader("RUBBoS: Author page");

    // Authenticate the user
    $userId = 0;
    $access = 0;
    if (!is_null($nickname) && !is_null($password))
    {
      $result = mysql_query("SELECT id,access FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"", $link);
	  if (!$result)
	  {
		error_log("[".__FILE__."] Authentification query 'SELECT id,access FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"' failed: " . mysql_error($link));
		die("ERROR: Authentification query failed for nickname '$nickname': " . mysql_error($link));
	  }
      if (mysql_num_rows($result) != 0)
      {
        $row = mysql_fetch_array($result);
        $userId = $row["id"];
        $access = $row["access"];
      }
      mysql_free_result($result);
    }

    if (($userId == 0) || ($access == 0))
    {
      print("<p><center><h2>Sorry, but this feature is only accessible by users with an author access.</h2></center><p>\n");
    }
    else
    {
      print("<p><center><h2>Which administrative task do you want to do ?</h2></center>\n".
            "<p><p><a href=\"/PHP/ReviewStories.php?authorId=$userId\">Review submitted stories</a><br>\n");
    }

    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
