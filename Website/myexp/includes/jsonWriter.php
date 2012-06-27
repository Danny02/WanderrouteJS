<?php

   	$json = $_POST['json'];
   	$path = $_POST['p'];

   	$json = json_encode($json);

	if ($json != null) {
       	file_put_contents($path, $json);
    } else {
      	print_r('Couldnt write '.$path);
    }
?>