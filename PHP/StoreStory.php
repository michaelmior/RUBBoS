<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "SubmitStory.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $nickname = getSessionPostGetParam('nickname');

    $password = getSessionPostGetParam('password');

    $title = getSessionPostGetParam('title');
    if (!isset($title))
	{
      printError($scriptName, $startTime, "SubmitStory", "You must provide a story title!");
      exit();
    }

    $body = getSessionPostGetParam('body');
    if (!isset($body))
	{
      printError($scriptName, $startTime, "SubmitStory", "You must provide a story body!");
      exit();
    }
      
    $category = getSessionPostGetParam('category');
    if (!isset($category))
	{
      printError($scriptName, $startTime, "SubmitStory", "You must provide a category!");
      exit();
    }

    getDatabaseLink($link);

    printHTMLheader("RUBBoS: Story submission result");

    print("<center><h2>Story submission result:</h2></center><p>\n");

    // Authenticate the user
    $userId = 0;
    $access = 0;
    if (!is_null($nickname) && !is_null($password))
    {
      $result = mysql_query("SELECT id,access FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"", $link);
	  if (!$result)
	  {
		error_log("[".__FILE__."] Authentication query 'SELECT id,access FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"' failed: " . mysql_error($link));
		die("ERROR: Authentication query failed for nickname '$nickname': " . mysql_error($link));
	  }
      if (mysql_num_rows($result) != 0)
      {
        $row = mysql_fetch_array($result);
        $userId = $row["id"];
        $access = $row["access"];
      }
      mysql_free_result($result);
    }

    $table = "submissions";
    if ($userId == 0)
      print("Story stored by the 'Anonymous Coward'<br>\n");
    else
    {
      if ($access == 0)
        print("Story submitted by regular user #$userId<br>\n");
      else
      {
        print("Story posted by author #$userId<br>\n");
        $table = "stories";
      }
    }

    // Add story to database
    $now = date("Y:m:d H:i:s");
    $result = mysql_query("INSERT INTO $table VALUES (NULL, \"$title\", \"$body\", '$now', $userId, $category)", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to insert new story in database 'INSERT INTO $table VALUES (NULL, \"$title\", \"$body\", '$now', $userId, $category)': " . mysql_error($link));
		die("ERROR: Failed to insert new story in database for user '$userId' and category '$category': " . mysql_error($link));
	}

    print("Your story has been successfully stored in the $table database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
