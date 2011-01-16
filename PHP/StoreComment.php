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
    }

    $password = $_POST['password'];
    if ($password == null)
    {
      $password = $_GET['password'];
    }

    $storyId = $_POST['storyId'];
    if ($storyId == null)
    {
      $storyId = $_GET['storyId'];
      if ($storyId == null)
      {
         printError($scriptName, $startTime, "StoreComment", "You must provide a story identifier!<br>");
         exit();
      }
    }

    $parent = $_POST['parent'];
    if ($parent == null)
    {
      $parent = $_GET['parent'];
      if ($parent == null)
      {
         printError($scriptName, $startTime, "StoreComment", "You must provide a follow up identifier!<br>");
         exit();
      }
    }

    $subject = $_POST['subject'];
    if ($subject == null)
    {
      $subject = $_GET['subject'];
      if ($subject == null)
      {
         printError($scriptName, $startTime, "StoreComment", "You must provide a comment subject!<br>");
         exit();
      }
    }

    $body = $_POST['body'];
    if ($body == null)
    {
      $body = $_GET['body'];
      if ($body == null)
      {
         printError($scriptName, $startTime, "StoreComment", "<h3>You must provide a comment body!<br></h3>");
         exit();
      }
    }
      
    $comment_table = $_POST['comment_table'];
    if ($comment_table == null)
    {
      $comment_table = $_GET['comment_table'];
      if ($comment_table == null)
      {
         printError($scriptName, $startTime, "Viewing comment", "You must provide a comment table!<br>");
         exit();
      }
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
    $result = mysql_query("INSERT INTO $comment_table VALUES (NULL, $userId, $storyId, $parent, 0, 0, '$now', \"$subject\", \"$body\")", $link) or die("ERROR: Failed to insert new comment in database.");
    $result = mysql_query("UPDATE $comment_table SET childs=childs+1 WHERE id=$parent", $link) or die("ERROR: Failed to update parent childs in database.");

    print("Your comment has been successfully stored in the $table database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
