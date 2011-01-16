<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "SubmitStory.php";
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

    $title = $_POST['title'];
    if ($title == null)
    {
      $title = $_GET['title'];
      if ($title == null)
      {
         printError($scriptName, $startTime, "SubmitStory", "You must provide a story title!<br>");
         exit();
      }
    }

    $body = $_POST['body'];
    if ($body == null)
    {
      $body = $_GET['body'];
      if ($body == null)
      {
         printError($scriptName, $startTime, "SubmitStory", "<h3>You must provide a story body!<br></h3>");
         exit();
      }
    }
      
    $category = $_POST['category'];
    if ($category == null)
    {
      $category = $_GET['category'];
      if ($category == null)
      {
         printError($scriptName, $startTime, "SubmitStory", "<h3>You must provide a category !<br></h3>");
         exit();
      }
    }

    getDatabaseLink($link);

    printHTMLheader("RUBBoS: Story submission result");

    print("<center><h2>Story submission result:</h2></center><p>\n");

    // Authenticate the user
    $userId = 0;
    $access = 0;
    if (($nickname != null) && ($password != null))
    {
      $result = mysql_query("SELECT id,access FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"", $link) or die("ERROR: Authentication query failed");
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
    $result = mysql_query("INSERT INTO $table VALUES (NULL, \"$title\", \"$body\", '$now', $userId, $category)", $link) or die("ERROR: Failed to insert new story in database.");

    print("Your story has been successfully stored in the $table database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
