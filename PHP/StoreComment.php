<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreComment.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	if (isset($_POST['nickname']))
	{
    	$nickname = $_POST['nickname'];
	}
    elseif (isset($_GET['nickname']))
    {
      $nickname = $_GET['nickname'];
    }
	else
	{
	  $nickname = NULL;
	}

	if (isset($_POST['password']))
	{
    	$password = $_POST['password'];
	}
    elseif (isset($_GET['password']))
    {
      $password = $_GET['password'];
    }
	else
	{
	  $password = NULL;
	}

	if (isset($_POST['storyId']))
	{
    	$storyId = $_POST['storyId'];
	}
    elseif (isset($_GET['storyId']))
    {
      $storyId = $_GET['storyId'];
	}
	else
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a story identifier!<br>");
      exit();
    }

	if (isset($_POST['parent']))
	{
    	$parent = $_POST['parent'];
	}
    elseif (isset($_GET['parent']))
    {
      $parent = $_GET['parent'];
	}
	else
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a follow up identifier!<br>");
      exit();
    }

	if (isset($_POST['subject']))
	{
    	$subject = $_POST['subject'];
	}
    elseif (isset($_GET['subject']))
    {
      $subject = $_GET['subject'];
	}
	else
	{
      printError($scriptName, $startTime, "StoreComment", "You must provide a comment subject!<br>");
      exit();
    }

	if (isset($_POST['body']))
	{
    	$body = $_POST['body'];
	}
    elseif (isset($_GET['body']))
    {
      $body = $_GET['body'];
	}
	else
	{
      printError($scriptName, $startTime, "StoreComment", "<h3>You must provide a comment body!<br></h3>");
      exit();
    }
      
	if (isset($_POST['comment_table']))
	{
    	$comment_table = $_POST['comment_table'];
	}
    elseif (isset($_GET['comment_table']))
    {
      $comment_table = $_GET['comment_table'];
	}
	else
	{
      printError($scriptName, $startTime, "Viewing comment", "You must provide a comment table!<br>");
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
    $result = mysql_query("INSERT INTO $comment_table VALUES (NULL, $userId, $storyId, $parent, 0, 0, '$now', \"$subject\", \"$body\")", $link) or die("ERROR: Failed to insert new comment in database.");
    $result = mysql_query("UPDATE $comment_table SET childs=childs+1 WHERE id=$parent", $link) or die("ERROR: Failed to update parent childs in database.");

    print("Your comment has been successfully stored in the $table database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
