<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreComment.php";
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

    $comment_table = getSessionPostGetParam('comment_table');
    if (!isset($comment_table))
	{
      printError($scriptName, $startTime, "Moderating comment", "You must provide a comment table!");
      exit();
    }

    $commentId = getSessionPostGetParam('commentId');
    if (!isset($commentId))
	{
      printError($scriptName, $startTime, "Moderating comment", "You must provide a comment identifier!");
      exit();
    }

    $rating = getSessionPostGetParam('rating');
    if (!isset($rating))
	{
      printError($scriptName, $startTime, "Moderating comment", "You must provide a rating!");
      exit();
    }
      
    getDatabaseLink($link);

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
      printHTMLheader("RUBBoS: Moderation");
      print("<p><center><h2>Sorry, but this feature is only accessible by users with an author access.</h2></center><p>\n");
    }
    else
    {
      printHTMLheader("RUBBoS: Comment moderation result");

      print("<center><h2>Comment moderation result:</h2></center><p>\n");

//      mysql_query("LOCK TABLES users WRITE, comments WRITE", $link) or die("ERROR: Failed to acquire locks on users and comments tables.");
      $result = mysql_query("SELECT writer,rating FROM $comment_table WHERE id=$commentId", $link);
	  if (!$result)
	  {
		error_log("[".__FILE__."]: Query 'SELECT writer,rating FROM $comment_table WHERE id=$commentId' failed: " . mysql_error($link));
		die("ERROR: Query failed for comment table '$comment_table' and comment '$commentId': " . mysql_error($link));
	  }
      if (mysql_num_rows($result) == 0)
      {
//        mysql_query("UNLOCK TABLES", $link) or die("ERROR: Failed to unlock users and comments tables.");
        die("<h3>ERROR: Sorry, but this comment '$commentId' does not exist.</h3><br>\n");
      }
      $row = mysql_fetch_array($result);
      if ((($row["rating"] == -1) && ($rating == -1)) || (($row["rating"] == 5) && ($rating == 1)))
        print("Comment rating is already to its maximum, updating only user's rating.");
      else
      {
        // Update ratings
        if ($rating != 0)
        {
          $result = mysql_query("UPDATE users SET rating=rating+$rating WHERE id=".$row["writer"]);
		  if (!$result)
		  {
			error_log("[".__FILE__."] Unable to update user's rating 'UPDATE users SET rating=rating+$rating WHERE id=".$row["writer"]."': " . mysql_error($link));
			die("ERROR: Unable to update user's rating for writer '".$row["writer"]."': " . mysql_error($link));
		  }
          $result = mysql_query("UPDATE $comment_table SET rating=rating+$rating WHERE id=$commentId");
		  if (!$result)
		  {
			error_log("[".__FILE__."] Unable to update comment's rating 'UPDATE $comment_table SET rating=rating+$rating WHERE id=$commentId': " . mysql_error($link));
			die("ERROR: Unable to update comment's rating for comment table '$comment_table' and comment '$commentId': " . mysql_error($link));
		  }
        }
      }

      $comment_result = mysql_query("SELECT rating FROM $comment_table WHERE id=$commentId", $link);
	  if (!$comment_result)
	  {
		error_log("[".__FILE__."] Comment rating query 'SELECT rating FROM $comment_table WHERE id=$commentId' failed: " . mysql_error($link));
		die("ERROR: Comment rating query failed for comment table '$comment_table' and comment '$commentId': " . mysql_error($link));
	  }
      $comment_row = mysql_fetch_array($comment_result);
      $user_result = mysql_query("SELECT rating FROM users WHERE id=".$row["writer"], $link);
	  if (!$user_result)
	  {
		error_log("[".__FILE__."] Authentification query 'SELECT rating FROM users WHERE id=".$row["writer"]."' failed: " . mysql_error($link));
		die("ERROR: Authentification query failed for user '".$row["writer"]."': " . mysql_error($link));
	  }
      if (mysql_num_rows($user_result) == 0)
        print("<h3>ERROR: Sorry, but this user does not exist.</h3><br>\n");
      else
        $user_row = mysql_fetch_array($user_result);

//      mysql_query("UNLOCK TABLES", $link) or die("ERROR: Failed to unlock users and comments tables.");

      // Update moderator log
      $now = date("Y:m:d H:i:s");
      $result = mysql_query("INSERT INTO moderator_log VALUES (NULL, $userId, $commentId, $rating, '$now')", $link);
	  if (!$result)
	  {
		error_log("[".__FILE__."] Failed to insert new rating in moderator_log 'INSERT INTO moderator_log VALUES (NULL, $userId, $commentId, $rating, '$now')': " . mysql_error($link));
		die("ERROR: Failed to insert new rating in moderator_log for user '$userid' and comment '$commentId': " . mysql_error($link));
	  }
      
      print("New comment rating is :".$comment_row["rating"]."<br>\n");
      print("New user rating is :".$user_row["rating"]."<br>\n");
      print("<center><h2>Your moderation has been successfully stored.</h2></center>\n");
    }

    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
