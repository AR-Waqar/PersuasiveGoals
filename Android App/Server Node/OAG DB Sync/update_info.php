<?php 

$device_id = "xyz";
$date = "18-08-2020";
$todays_step = "5500";
$todays_goal = "7500";

$user ="DB_User";
$password = "****^";
$host = "localhost";
$db_name = "DB_Name";

$con = mysqli_connect($host,$user,$password,$db_name);

// Check connection
if($con === false){
    echo("ERROR: Could not connect. " . mysqli_connect_error());
}

$sql = "INSERT INTO tag VALUES('$device_id','$date','$todays_step','$todays_goal')";
if(mysqli_query($con, $sql))
{
    echo "Success Data Insertion";
}
else 
{   
    echo "Error Data Insertion";
}
mysqli_close($con);

?>