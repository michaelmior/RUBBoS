<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreComment.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $nickname = getSessionPostGetParam('nickname');

    $password = getSessionPostGetParam('password');

    $storyId = getSessionPostGetParam('storyId');
    if (!isset($storyId))
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a story identifier!");
      exit();
    }

    $parent = getSessionPostGetParam('parent');
    if (!isset($parent))
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a follow up identifier!");
      exit();
    }

    $subject = getSessionPostGetParam('subject');
    if (!isset($subject))
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a comment subject!");
      exit();
    }

    $body = getSessionPostGetParam('body');
    if (!isset($body))
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a comment body!");
      exit();
    }
      
    $comment_table = getSessionPostGetParam('comment_table');
    if (!isset($comment_table))
	{
      printError($scriptName, $startTime, "Viewing comment", "You must provide a comment table!");
      exit();
    }

    getDatabaseLink($link);

    printHTMLheader("RUBBoS: Comment submission result");

    print("<center><h2>Comment submission result:</h2></center><p>\n");

    // Authenticate the user
    $userId = authenticate($nickname, $password, $link);
    if ($userId == 0)
      print("Comment posted by the 'Anonymous Coward'<br>\n");
    else
      print("Comment posted by user #$userId<br>\n");

    // Add comment to database
    $now = date("Y:m:d H:i:s");
    $result = mysql_query("INSERT INTO $comment_table VALUES (NULL, $userId, $storyId, $parent, 0, 0, '$now', \"$subject\", \"$body\")", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to insert new comment in database 'INSERT INTO $comment_table VALUES (NULL, $userId, $storyId, $parent, 0, 0, '$now', \"$subject\", \"$body\")': " . mysql_error($link));
		die("ERROR: Failed to insert new comment in database for comment table '$comment_table', user '$userId', story '$storyId' and parent '$parent': " . mysql_error($link));
	}
    $result = mysql_query("UPDATE $comment_table SET childs=childs+1 WHERE id=$parent", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to update parent childs in database 'UPDATE $comment_table SET childs=childs+1 WHERE id=$parent': " . mysql_error($link));
		die("ERROR: Failed to update parent childs in database for comment table '$comment_table' and parent '$parent': " . mysql_error($link));
	}

    print("Your comment has been successfully stored in the $comment_table database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
