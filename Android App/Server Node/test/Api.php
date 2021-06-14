<?php 

    //array to show the response
    $response = array();
    
    //uploads directory, we will upload all the files inside this folder
    $target_dir = "uploads/";

    //checking if we are having an api call, using the get parameters 'apicall'
    if(isset($_GET['apicall'])){
		
		switch($_GET['apicall']){
            
            //if the api call is for uploading the image 
            case 'upload':
                //error message and error flag
                $message = 'Params ';
                $is_error = false; 
                
                //validating the request to check if all the required parameters are available or not 
                if(!isset($_POST['desc'])){
                    $message .= "desc, ";
                    $is_error = true; 
                }

                if(!isset($_FILES['db']['name'])){
                    $message .= "db ";
                    $is_error = true; 
                }
                
                //in case we have an error in validation, displaying the error message 
                if($is_error){
                    $response['error'] = true; 
                    $response['message'] = $message . " required."; 
                }else{
                    //if validation succeeds 
                    //creating a target file with a unique name, so that for every upload we create a unique file in our server
                    $dirName = uniqid();

                    mkdir($target_dir . '/' . $dirName);

                    $target_file = $target_dir . '/' . $dirName;

                    foreach ($_FILES['db']['tmp_name'] as $key => $value) { 
                        $file_tmpname = $_FILES['db']['tmp_name'][$key]; 
                        $file_name = $_FILES['db']['name'][$key]; 
                        move_uploaded_file($file_tmpname, $target_dir . '/' . $dirName . '/'. $file_name);
                    }

                    $response['error'] = false; 
                    $response['message'] = 'Files Uploaded Successfully';
                }
            break;
			
			default: 
				$response['error'] = true; 
				$response['message'] = 'Invalid Operation Called';
		}
		
	}else{
		$response['error'] = true; 
		$response['message'] = 'Invalid API Call';
    }

    header('Content-Type: application/json');
    echo json_encode($response);