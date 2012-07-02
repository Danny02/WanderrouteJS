<?php

   	$json = $_POST['json'];
   	$path = $_POST['p'];

    $signs = file_get_contents($path);

    if (empty($signs)) {
        $signs = array();
    }

    $signs = json_decode($signs, true);

    $signs[] = $json;

    $signs = json_encode($signs);

    if ($json != null) {
       	file_put_contents($path, $signs);
    } else {
      	print_r('Couldnt write '.$path);
    }
?>
