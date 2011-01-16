<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreComment.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $nickname = $_POST['nickname'];
    if ($nickname == null)
    {
      $nickname = $_GET['nickname'];
      if ($nickname == null)
      {
         printError($scriptName, $startTime, "Author", "You must provide a nick name!<br>");
         exit();
      }
    }

    $password = $_POST['password'];
    if ($password == null)
    {
      $password = $_GET['password'];
      if ($password == null)
      {
         printError($scriptName, $startTime, "Author", "You must provide a password!<br>");
         exit();
      }
    }

    $comment_table = $_POST['comment_table'];
    if ($comment_table == null)
    {
      $comment_table = $_GET['comment_table'];
      if ($comment_table == null)
      {
         printError($scriptName, $startTime, "Moderating comment", "You must provide a comment table!<br>");
         exit();
      }
    }

    $commentId = $_POST['commentId'];
    if ($commentId == null)
    {
      $commentId = $_GET['commentId'];
      if ($commentId == null)
      {
         printError($scriptName, $startTime, "Moderating comment", "You must provide a comment identifier!<br>");
         exit();
      }
    }

    $rating = $_POST['rating'];
    if ($rating == null)
    {
      $rating = $_GET['rating'];
      if ($rating == null)
      {
         printError($scriptName, $startTime, "Moderating comment", "You must provide a rating!<br>");
         exit();
      }
    }
      
    getDatabaseLink($link);

    // Authenticate the user
    $userId = 0;
    $access = 0;
    if (($nickname != null) && ($password != null))
    {
      $result = mysql_query("SELECT id,access FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"", $link) or die("ERROR: Authentification query failed");
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
      $result = mysql_query("SELECT writer,rating FROM $comment_table WHERE id=$commentId", $link) or die("ERROR: Query failed");
      if (mysql_num_rows($result) == 0)
      {
//        mysql_query("UNLOCK TABLES", $link) or die("ERROR: Failed to unlock users and comments tables.");
        die("<h3>ERROR: Sorry, but this comment does not exist.</h3><br>\n");
      }
      $row = mysql_fetch_array($result);
      if ((($row["rating"] == -1) && ($rating == -1)) || (($row["rating"] == 5) && ($rating == 1)))
        print("Comment rating is already to its maximum, updating only user's rating.");
      else
      {
        // Update ratings
        if ($rating != 0)
        {
          mysql_query("UPDATE users SET rating=rating+$rating WHERE id=".$row["writer"]) or die("ERROR: Unable to update user's rating\n");
          mysql_query("UPDATE $comment_table SET rating=rating+$rating WHERE id=$commentId") or die("ERROR: Unable to update comment's rating\n");
        }
      }

      $comment_result = mysql_query("SELECT rating FROM $comment_table WHERE id=$commentId", $link) or die("ERROR: Comment rating query failed");
      $comment_row = mysql_fetch_array($comment_result);
      $user_result = mysql_query("SELECT rating FROM users WHERE id=".$row["writer"], $link) or die("ERROR: Authentification query failed");
      if (mysql_num_rows($user_result) == 0)
        print("<h3>ERROR: Sorry, but this user does not exist.</h3><br>\n");
      else
        $user_row = mysql_fetch_array($user_result);

//      mysql_query("UNLOCK TABLES", $link) or die("ERROR: Failed to unlock users and comments tables.");

      // Update moderator log
      $now = date("Y:m:d H:i:s");
      $result = mysql_query("INSERT INTO moderator_log VALUES (NULL, $userId, $commentId, $rating, '$now')", $link) or die("ERROR: Failed to insert new rating in moderator_log.");
      
      print("New comment rating is :".$comment_row["rating"]."<br>\n");
      print("New user rating is :".$user_row["rating"]."<br>\n");
      print("<center><h2>Your moderation has been successfully stored.</h2></center>\n");
    }

    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
