<?
require dirname(__FILE__) . "/../vendor/autoload.php";

use phpcassa\ColumnFamily;
use phpcassa\SystemManager;
use phpcassa\Connection\ConnectionPool;
use phpcassa\Schema\StrategyClass;
use cassandra\NotFoundException;

$sys = new SystemManager("127.0.0.1");
try {
    $sys->describe_keyspace("RUBBoS");
    $sys->drop_keyspace("RUBBoS");
    die("Keyspace already exists");
} catch (NotFoundException $e) {
    // Good to go
}

$sys->create_keyspace("RUBBoS", array(
    "strategy_class" => StrategyClass::SIMPLE_STRATEGY,
    "strategy_options" => array('replication_factor' => '1')));

$sys->create_column_family("RUBBoS", "Users");
$sys->create_column_family("RUBBoS", "Categories");
$sys->create_column_family("RUBBoS", "Stories");
$sys->create_column_family("RUBBoS", "OldStories");
$sys->create_column_family("RUBBoS", "Comments");
$sys->create_column_family("RUBBoS", "OldComments");
$sys->create_column_family("RUBBoS", "CategoryStories", array(
    "comparator_type" => "LongType"));

$pool = new ConnectionPool("RUBBoS", array("127.0.0.1:9160"));

$categories = new ColumnFamily($pool, "Categories");
$categories->insert(0, array("name" => 'AMD'));
$categories->insert(1, array("name" => 'America Online'));
$categories->insert(2, array("name" => 'Amiga'));
$categories->insert(3, array("name" => 'Anime'));
$categories->insert(4, array("name" => 'Announcements'));
$categories->insert(5, array("name" => 'Apache'));
$categories->insert(6, array("name" => 'Apple'));
$categories->insert(7, array("name" => 'Be'));
$categories->insert(8, array("name" => 'Beanies'));
$categories->insert(9, array("name" => 'BSD'));
$categories->insert(10, array("name" => 'Bug'));
$categories->insert(11, array("name" => 'Caldera'));
$categories->insert(12, array("name" => 'CDA'));
$categories->insert(13, array("name" => 'Censorship'));
$categories->insert(14, array("name" => 'Christmas Cheer'));
$categories->insert(15, array("name" => 'Comdex'));
$categories->insert(16, array("name" => 'Compaq'));
$categories->insert(17, array("name" => 'Corel'));
$categories->insert(18, array("name" => 'Debian'));
$categories->insert(19, array("name" => 'Digital'));
$categories->insert(20, array("name" => 'Editorial'));
$categories->insert(21, array("name" => 'Education'));
$categories->insert(22, array("name" => 'Encryption'));
$categories->insert(23, array("name" => 'Enlightenment'));
$categories->insert(24, array("name" => 'ePlus'));
$categories->insert(25, array("name" => 'Games'));
$categories->insert(26, array("name" => 'GNOME'));
$categories->insert(27, array("name" => 'GNU is Not Unix'));
$categories->insert(28, array("name" => 'GNUStep'));
$categories->insert(29, array("name" => 'Graphics'));
$categories->insert(30, array("name" => 'Handhelds'));
$categories->insert(31, array("name" => 'Hardware'));
$categories->insert(32, array("name" => 'IBM'));
$categories->insert(33, array("name" => 'Intel'));
$categories->insert(34, array("name" => 'Internet Explorer'));
$categories->insert(35, array("name" => 'It\'s funny.  Laugh.'));
$categories->insert(36, array("name" => 'Java'));
$categories->insert(37, array("name" => 'KDE'));
$categories->insert(38, array("name" => 'Links'));
$categories->insert(39, array("name" => 'Linux'));
$categories->insert(40, array("name" => 'Linux Business'));
$categories->insert(41, array("name" => 'Linux Mandrake'));
$categories->insert(42, array("name" => 'Linuxcare'));
$categories->insert(43, array("name" => 'Microsoft'));
$categories->insert(44, array("name" => 'Movies'));
$categories->insert(45, array("name" => 'Mozilla'));
$categories->insert(46, array("name" => 'Music'));
$categories->insert(47, array("name" => 'Netscape'));
$categories->insert(48, array("name" => 'News'));
$categories->insert(49, array("name" => 'Patents'));
$categories->insert(50, array("name" => 'Perl'));
$categories->insert(51, array("name" => 'PHP'));
$categories->insert(52, array("name" => 'Privacy'));
$categories->insert(53, array("name" => 'Programming'));
$categories->insert(54, array("name" => 'Quake'));
$categories->insert(55, array("name" => 'Quickies'));
$categories->insert(56, array("name" => 'Red Hat Software'));
$categories->insert(57, array("name" => 'Science'));
$categories->insert(58, array("name" => 'Security'));
$categories->insert(59, array("name" => 'Silicon Graphics'));
$categories->insert(60, array("name" => 'Slashback'));
$categories->insert(61, array("name" => 'Slashdot.org'));
$categories->insert(62, array("name" => 'Space'));
$categories->insert(63, array("name" => 'Spam'));
$categories->insert(64, array("name" => 'Star Wars Prequels'));
$categories->insert(65, array("name" => 'Sun Microsystems'));
$categories->insert(66, array("name" => 'SuSE'));
$categories->insert(67, array("name" => 'Technology'));
$categories->insert(68, array("name" => 'Television'));
$categories->insert(69, array("name" => 'The Almighty Buck'));
$categories->insert(70, array("name" => 'The Courts'));
$categories->insert(71, array("name" => 'The Gimp'));
$categories->insert(72, array("name" => 'The Internet'));
$categories->insert(73, array("name" => 'The Media'));
$categories->insert(74, array("name" => 'Toys'));
$categories->insert(75, array("name" => 'Transmeta'));
$categories->insert(76, array("name" => 'TurboLinux'));
$categories->insert(77, array("name" => 'United States'));
$categories->insert(78, array("name" => 'Unix'));
$categories->insert(79, array("name" => 'Upgrades'));
$categories->insert(80, array("name" => 'User Journal'));
$categories->insert(81, array("name" => 'VA'));
$categories->insert(82, array("name" => 'Wine'));
$categories->insert(83, array("name" => 'X'));
$categories->insert(84, array("name" => 'Ximian'));
