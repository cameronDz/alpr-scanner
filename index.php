<?php

header('Content-type: application/json');


function dbconn() {
  $mysqli = new mysqli("platescannerdb.cldyy1nyistq.us-east-1.rds.amazonaws.com","cs498","platescanner","PlateScannerDB","3306");
  if ($mysqli->connect_errno) return 0;
  return $mysqli;
}
$data = array();

if (isset($_POST['message_type'])) {
  switch ($_POST['message_type']) {
    case 'register':
      if ($mysqli = dbconn()) {
        $data = registerUser($mysqli, $_POST['user_name'], $_POST['user_password'], $_POST['gcm_user_id'], $_POST['email']);
      } else $data['error'] = 'Database connection error';
      break;
    case 'plate':
      if ($mysqli = dbconn()) {
        $data = registerPlate($mysqli, $_POST['plate_number'], $_POST['plate_state'], $_POST['user_id']);
      } else $data['error'] = 'Database connection error';
      break;
    case 'message':
      if ($mysqli = dbconn()) {
        $data = sendMessage($mysqli, $_POST['plate_number'], $_POST['plate_state'], $_POST['user_sender_id'], $_POST['message_sent_timestamp'], $_POST['message_sender_content'], $_POST['gps_lat'], $_POST['gps_lon']);
      } else $data['error'] = 'Database connection error';
      break;
    case 'read':
      if ($mysqli = dbconn()) {
        $data = messageRead($mysqli, $_POST['message_id']);
      } else $data['error'] = 'Database connection error';
      break;
    case 'reply':
      if ($mysqli = dbconn()) {
        $data = reply($mysqli, $_POST['message_reply_content'], $_POST['message_id']);
      } else $data['error'] = 'Database connection error';
      break;

   case 'login':
      if ($mysqli = dbconn()) {
        $data = login($mysqli, $_POST['user_name'], $_POST['user_password'], $_POST['gcm_user_id']);
      } else $data['error'] = 'Database connection error';
      break;

  }
}

echo json_encode($data);


# Register a user.
function registerUser($connection, $username, $password, $gcm_token, $email) {

  $json = array();

  $stmt = $connection->prepare('select (1) from PlateScannerDB.PlateScanner_Users where user_name = ? limit 1');
  $stmt->bind_param('s', $username);
  if ($stmt->execute()) {
    $user_check = $stmt->get_result();
    $stmt->close();
    if($user_check->fetch_assoc()){
      $json["error"] = "User already exists";
      $user_check->close();
    }
    else{
      // Insert user
      $stmt = $connection->prepare('Insert into PlateScannerDB.PlateScanner_Users (user_name, user_password, gcm_user_id, email) values (?, ?, ?, ?)');
      $stmt->bind_param('ssss',$username, $password, $gcm_token, $email);
      if ($stmt->execute()) {
        $json["output"] = "User created succesfully";
        $json["user_id"] = $connection->insert_id;
        gcmToken($connection, $gcm_token, $json["user_id"]);
      } else $json["error"] = "Database query error";
      $stmt->close();
    }
  } else $json["error"] = "Database query error";
  return $json;
}


# Registers a users plate.
function registerPlate($connection, $plate_number, $plate_state, $user_id) {

  $json = array();

  $stmt = $connection->prepare('select * from PlateScannerDB.License_Plates where plate_number = ? and plate_state = ?');
  $stmt->bind_param('ss', $plate_number, $plate_state);
  if ($stmt->execute()) {

    $result = $stmt->get_result();
    $stmt->close();
    $array = $result->fetch_assoc();

    if($array){
      if($array["user_id"]){
        $json["error"] = "Plate is already taken";
      }
      else{
        $stmt = $connection->prepare('Update PlateScannerDB.License_Plates set user_id = ? where plate_number = ? and plate_state = ?');
        $stmt->bind_param('sss', $user_id, $plate_number, $plate_state);
        if ($stmt->execute()) {

          $plate_id = $array["plate_id"];

          $gcmstmt = $connection->prepare('select gcm_token from PlateScannerDB.User_GCM_Tokens where user_id = ?');
          $gcmstmt->bind_param('s', $user_id);
          if ($gcmstmt->execute()) {
            $gcmresult = $gcmstmt->get_result();
            $gcmids = [];
            while ($gcmarray = $gcmresult->fetch_assoc()) array_push($gcmids, $gcmarray['gcm_token']);
            $msgstmt = $connection->prepare('select * from PlateScannerDB.Messages where plate_id = ?');
            $msgstmt->bind_param('s', $plate_id);
            if ($msgstmt->execute()) {
              $msgresult = $msgstmt->get_result();
              while ($msgarray = $msgresult->fetch_assoc()) {
                $gcmdata = [];
                $gcmdata['message_type'] = "receive";
                $gcmdata['message_id'] = $msgarray['message_id'];
                $gcmdata['plate_id'] = $msgarray['plate_id'];
                $gcmdata['timestamp'] = $msgarray['message_sent_timestamp'];
                $gcmdata['message'] = $msgarray['message_sender_content'];
                list($gcmdata['gps_lat'],$gcmdata['gps_lon']) = explode(", ", $msgarray['gps_location']);
                if ($msg = sendGCM($gcmdata,$gcmids,"New Message","You received a message.")) {
                  $json["error"] = $msg;
                  break;
                }
              }
            } else $json["error"] = "Database query error";
            $msgstmt->close();

            $json["output"] = "Succesfully registered an existing plate";
            $json["plate_id"] = $plate_id;
          } else $json["error"] = "Database query error";
          $gcmstmt->close();
        } else $json["error"] = "Database query error";
        $stmt->close();
      }
    }
    else{
      $stmt = $connection->prepare('Insert into PlateScannerDB.License_Plates (user_id, plate_number, plate_state) values (?, ?, ?)');
      $stmt->bind_param('sss', $user_id, $plate_number, $plate_state);
      if ($stmt->execute()) {
        $json["output"] = "Succesfully created a new plate";
        $json["plate_id"] = $connection->insert_id;
      } else $json["error"] = "Database query error";
      $stmt->close();
    }
    $result->close();
  } else $json["error"] = "Database query error";
  return $json;
}


#   Send message:
function sendMessage($connection, $plate_number, $plate_state, $user_sender_id, $message_sent_timestamp, $message_sender_content, $gps_lat, $gps_lon) {

  $json = array();

  $stmt = $connection->prepare('SELECT plate_id, user_id FROM PlateScannerDB.License_Plates WHERE plate_state = ? AND plate_number = ?');
  $stmt->bind_param('ss', $plate_state, $plate_number);
  if ($stmt->execute()) {
    $result = $stmt->get_result();
    $array = $result->fetch_assoc();
    $user_receiver_id = $array["user_id"];
    $plate_id = $array["plate_id"];

    $gps_location = $gps_lat . ", " . $gps_lon;

    if(!$plate_id){
      $stmt = $connection->prepare('Insert into PlateScannerDB.License_Plates (plate_number, plate_state) values (?, ?)');
      $stmt->bind_param('ss', $plate_number, $plate_state);
      if ($stmt->execute()) {
        $plate_id = $connection->insert_id;
        $json["output"] = "Plate not registered, but a user will receive the message when they register";
      } else $json["error"] = "Database query error... Paramaters are as follows: " . $plate_number . "---" . $plate_state . "---" . $user_send_id . "---" . $message_sent_timestamp . "---" . $message_sender_content . "---" . $gps_lat . "---" . $gps_lon;
    }
    else{
      $json["output"] = "Message sent successfully";
    }

    $stmt = $connection->prepare('Insert into PlateScannerDB.Messages (user_sender_id, user_receiver_id, plate_id, message_sent_timestamp, message_sender_content, gps_location) VALUES (?, ?, ?, ?, ?, ?)');
    $stmt->bind_param('ssssss', $user_sender_id, $user_receiver_id, $plate_id, $message_sent_timestamp, $message_sender_content, $gps_location);
    $stmt->execute();

    $json["message_id"] = $connection->insert_id;
    
    $gcmstmt = $connection->prepare('select gcm_token from PlateScannerDB.User_GCM_Tokens where user_id = ?');
    $gcmstmt->bind_param('s', $user_receiver_id);
    if ($gcmstmt->execute()) {
      $gcmresult = $gcmstmt->get_result();
      $gcmids = [];
      while ($gcmarray = $gcmresult->fetch_assoc()) array_push($gcmids, $gcmarray['gcm_token']);
    
      $gcmdata = [];
      $gcmdata['message_type'] = "receive";
      $gcmdata['message_id'] = $json["message_id"];
      $gcmdata['plate_id'] = $plate_id;
      $gcmdata['timestamp'] = $message_sent_timestamp;
      $gcmdata['message'] = $message_sender_content;
      $gcmdata['gps_lat'] = $gps_lat;
      $gcmdata['gps_lon'] = $gps_lon;
      if ($msg = sendGCM($gcmdata,$gcmids,"New Message","You received a message.")) $json["error"] = $msg;
    } else $json["error"] = "Database query error";
    $gcmstmt->close();
    
  } else $json["error"] = "Database query error";
  return $json;
}




# Marks a message as read.
function messageRead($connection, $message_id) {

  $json = array();

  $stmt = $connection->prepare('Update PlateScannerDB.Messages set message_received = 1 where message_id = ?');
  $stmt->bind_param('s', $message_id);
  if ($stmt->execute()) {
    $json["output"] = "The message was succesfully read";
    $msgstmt = $connection->prepare('select user_sender_id from PlateScannerDB.Messages where message_id = ?');
    $msgstmt->bind_param('s', $message_id);
    if ($msgstmt->execute()) {
      $msgresult = $msgstmt->get_result();
      $msgarray = $msgresult->fetch_assoc();
      $gcmstmt = $connection->prepare('select gcm_token from PlateScannerDB.User_GCM_Tokens where user_id = ?');
      $gcmstmt->bind_param('s', $msgarray['user_sender_id']);
      if ($gcmstmt->execute()) {
        $gcmresult = $gcmstmt->get_result();
        $gcmids = [];
        while ($gcmarray = $gcmresult->fetch_assoc()) array_push($gcmids, $gcmarray['gcm_token']);
        $gcmdata = [];
        $gcmdata['message_type'] = "send";
        $gcmdata['message_id'] = $message_id;
        if ($msg = sendGCM($gcmdata,$gcmids,"Message Read","Your message was read.")) $json["error"] = $msg;
      } else $json["error"] = "Database query error";
      $gcmstmt->close();
    } else $json["error"] = "Database query error";
    $msgstmt->close();
  } else $json["error"] = "Database query error";
  $stmt->close();
  return $json;
}



# Replies to a message.
function reply($connection, $reply_message, $message_id) {

  $json = array();

  $stmt = $connection->prepare('Update PlateScannerDB.Messages set message_reply_content = ? where message_id = ?');
  $stmt->bind_param('ss', $reply_message, $message_id);
  if ($stmt->execute()) {
    $json["output"] = "Message replied to succesfully";
    $msgstmt = $connection->prepare('select user_sender_id from PlateScannerDB.Messages where message_id = ?');
    $msgstmt->bind_param('s', $message_id);
    if ($msgstmt->execute()) {
      $msgresult = $msgstmt->get_result();
      $msgarray = $msgresult->fetch_assoc();
      $gcmstmt = $connection->prepare('select gcm_token from PlateScannerDB.User_GCM_Tokens where user_id = ?');
      $gcmstmt->bind_param('s', $msgarray['user_sender_id']);
      if ($gcmstmt->execute()) {
        $gcmresult = $gcmstmt->get_result();
        $gcmids = [];
        while ($gcmarray = $gcmresult->fetch_assoc()) array_push($gcmids, $gcmarray['gcm_token']);
        $gcmdata = [];
        $gcmdata['message_type'] = "get";
        $gcmdata['message_id'] = $message_id;
        $gcmdata['message_reply_content'] = $reply_message;
        if ($msg = sendGCM($gcmdata,$gcmids,"Message Reply","Your message was replied to.")) $json["error"] = $msg;
      } else $json["error"] = "Database query error";
      $gcmstmt->close();
    } else $json["error"] = "Database query error";
    $msgstmt->close();
  } else $json["error"] = "Database query error";
  $stmt->close();
  return $json;
}

# Logs a user in.
function login($connection, $username, $password, $gcm_user_id) {
	$json = array();
	
	$stmt = $connection->prepare('select * from PlateScannerDB.PlateScanner_Users where user_name = ? and user_password = ? limit 1');
	$stmt->bind_param('ss', $username, $password);
	$stmt->execute();
	$user_check = $stmt->get_result();
	
	if($array = $user_check->fetch_assoc()){
		$json["output"] = "Logged in successfully.";
		$json["user_id"] = $array["user_id"];
    gcmToken($connection, $gcm_user_id, $array['user_id']);
	}
	else{ 
		$json["error"] = "Wrong username / password combination.";
	}
	return $json;
}

function gcmToken($connection, $gcm_token, $user_id) {
		#	Check if token is taken
		$stmt = $connection->prepare('select * from PlateScannerDB.User_GCM_Tokens where gcm_token = ?');
		$stmt->bind_param('s', $gcm_token);
		$stmt->execute();
		
		$gcm_check = $stmt->get_result();
	
		if($gcm_check->fetch_assoc()){
			# 	Token in use
			#	Updates the gcm_token to the current user.
			$stmt = $connection->prepare('Update PlateScannerDB.User_GCM_Tokens set user_id = ? where gcm_token = ?');
			$stmt->bind_param('ss', $user_id, $gcm_token);
			$stmt->execute();
		}	
	else{
			# 	Token not taken
			#   GCM Token Insert	
			$stmt = $connection->prepare('Insert into PlateScannerDB.User_GCM_Tokens (user_id, gcm_token) VALUES (?, ?)');
			$stmt->bind_param('ss', $user_id, $gcm_token);
			$stmt->execute();
	}
}

# GCM
function sendGCM($data, $deviceRegistrationId, $title="", $body="") {
  
 // API access key from Google API's Console
define( 'API_ACCESS_KEY', 'AIzaSyD32wqp8jBMvCDBsykToMx8xbM8N-Trr_o' );
$registrationIds = array("fcdHB9VA30Y:APA91bEqMW3ePkjWWfqHKg1ze5wokE0dg4eYGcYF4OeRSD04hZ2wofY7AMtOB97tcUM7VgsL9KuMfr8Za0kW0svRYB4Qrpoz33ByBJFH7ryNlb1gquRc4dTvhbIlZyex71sbNEUKhcAX" );
// prep the bundle
$title = "login";
$msg = array
(
    'message'       => $data,
    'title'         => $title,
    'vibrate'       => 1,
    'sound'         => 1
);
$fields = array
(
    'registration_ids'  => $deviceRegistrationId,
    'data'              => $msg
);
$headers = array
(
    'Authorization: key=' . API_ACCESS_KEY,
    'Content-Type: application/json'
);
$ch = curl_init();
curl_setopt( $ch,CURLOPT_URL, 'https://android.googleapis.com/gcm/send' );
curl_setopt( $ch,CURLOPT_POST, true );
curl_setopt( $ch,CURLOPT_HTTPHEADER, $headers );
curl_setopt( $ch,CURLOPT_RETURNTRANSFER, true );
curl_setopt( $ch,CURLOPT_SSL_VERIFYPEER, false );
curl_setopt( $ch,CURLOPT_POSTFIELDS, json_encode( $fields ) );
$result = curl_exec($ch );
curl_close( $ch );
}

?>

