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
      $users = new phpcassa\ColumnFamily($link, "Users");
      try {
        $row = $users->get($nickname);
        $userId = $row["nickname"];
        $access = $row["access"];
      } catch (cassandra\NotFoundException $e) {
        $userId = null;
        $access = 0;
      } catch (Exception $e) {
        die("ERROR: Authentification query failed");
      }
      if ($row['password'] != $password) die("ERROR: Authentification query failed");
    }

    $table = $category;
    if (!$userId)
      print("Story stored by the 'Anonymous Coward'<br>\n");
    else
    {
      if ($access == 0)
        print("Story submitted by regular user #$userId<br>\n");
      else
      {
        print("Story posted by author $userId<br>\n");
        $table = "!SUBMISSIONS!";
      }
    }

    // Add story to database
    $now = date("Y:m:d H:i:s");
    try {
      $timestamp = microtime(true) * 1e6;
      $story_id = uniqid();
      $stories = new phpcassa\ColumnFamily($link, "Stories");
      $stories->insert($story_id, array(
        "title" => $title,
        "body" => $body,
        "date" => $now,
        "writer" => $userId,
        "category" => $category,
        "timestamp" => $timestamp
      ));

      $stories = new phpcassa\ColumnFamily($link, "CategoryStories");
      $stories->insert($table, array(
        $timestamp => $story_id
      ));
    } catch(Exception $e) {
      die("ERROR: Failed to insert new story in database.");
    }

    print("Your story has been successfully stored in the $table database table<br>\n");

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
