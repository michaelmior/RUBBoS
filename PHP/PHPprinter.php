<?php
 
function getDatabaseLink(&$link)
{
  $link = mysql_pconnect("localhost", "cecchet", "");
  if (!$link)
  {
	error_log("[".__FILE__."] Could not connect to database: " . mysql_error());
	die("ERROR: Could not connect to database: " . mysql_error());
  }
  $result = mysql_select_db("rubbos", $link);
  if (!$result)
  {
	error_log("ERROR: Couldn't select RUBBoS database: " . mysql_error($link));
	die("ERROR: Couldn't select RUBBoS database: " . mysql_error($link));
  }
}

function getMicroTime()
{
  list($usec, $sec) = explode(" ", microtime());
  return ((float)$usec + (float)$sec);
}

function printHTMLheader($title)
{
  include("header.html");
  print("<title>$title</title>");
}

function printHTMLHighlighted($msg)
{
  print("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\">\n");
  print("<TR><TD align=\"center\" width=\"100%\"><FONT size=\"4\" color=\"#000000\"><B>$msg</B></FONT></TD></TR>\n");
  print("</TABLE><p>\n");
}

function printHTMLfooter($scriptName, $startTime)
{
  $endTime = getMicroTime();
  $totalTime = $endTime - $startTime;
  printf("<br><hr>RUBBoS (C) Rice University/INRIA<br><i>Page generated by $scriptName in %.3f seconds</i><br>\n", $totalTime);
  print("</body>\n");
  print("</html>\n");	
}

function printError($scriptName, $startTime, $title, $error)
{
  printHTMLheader("RUBBoS ERROR: $title");
  print("<h2>We cannot process your request due to the following error :</h2><br>\n");
  print($error);
  printHTMLfooter($scriptName, $startTime);      
}

function authenticate($nickname, $password, $link)
{
  $result = mysql_query("SELECT id FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"", $link);
  if (!$result)
  {
	error_log("[".__FILE__."] Authentification query 'SELECT id FROM users WHERE nickname=\"$nickname\" AND password=\"$password\"' failed: " . mysql_error($link));
	die("ERROR: Authentification query failed for nickname '$nickname': " . mysql_error($link));
  }
  if (mysql_num_rows($result) == 0)
    return 0; // 0 is the anonymous user
  $row = mysql_fetch_array($result);
  return $row["id"];
}


function getUserName($uid, $link)
{
  $user_query = mysql_query("SELECT nickname FROM users WHERE id=$uid", $link);
  if (!$user_query)
  {
	error_log("[".__FILE__."] getUserName query 'SELECT nickname FROM users WHERE id=$uid' failed: " . mysql_error($link));
	die("ERROR: getUserName query failed for user '$uid': " . mysql_error($link));
  }
  $user_row = mysql_fetch_array($user_query);
  return $user_row["nickname"];
}


function getSessionPostGetParam($name, $value=null)
{
  if (isset($_POST[$name]))
  {
    return $_POST[$name];
  }
  if (isset($_GET[$name]))
  {
    return $_GET[$name];
  }

  return $value;
}


function getSessionGetPostParam($name, $value=null)
{
  if (isset($_GET[$name]))
  {
    return $_GET[$name];
  }
  if (isset($_POST[$name]))
  {
    return $_POST[$name];
  }

  return $value;
}

?>
