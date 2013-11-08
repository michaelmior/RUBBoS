<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "Author.php";
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

    getDatabaseLink($link);

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

    if (($userId == 0) || ($access == 0))
    {
      printHTMLheader("RUBBoS: Author page");
      print("<p><center><h2>Sorry, but this feature is only accessible by users with an author access.</h2></center><p>\n");
    }
    else
    {
      printHTMLheader("RUBBoS: Author page");
      print("<p><center><h2>Which administrative task do you want to do ?</h2></center>\n".
            "<p><p><a href=\"/PHP/ReviewStories.php?authorId=$userId\">Review submitted stories</a><br>\n");
    }

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
