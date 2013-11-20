var mapController = function(){
	
var map = null;
var goodies = [];	
var playerAvatars = [];
var avatarImages = {red: null, blue: null};
var goodieImages = {standard: null, big: null, special: null};
var specialActionImages = {doublePoints: null, invincible: null};
var playerAvatarWidth = 72;
var playerAvatarHeight = 95;
var positioningInterval;
	
this.initMap = function(mapElement) {
	    var locationCenter = new google.maps.LatLng(48.337050, 14.319600);
		var mapOptions = {zoom: 17,
						  center: locationCenter,
						  mapTypeId: google.maps.MapTypeId.ROADMAP}
	    
	    if(!isInitialized(mapElement)) {
	    	 map = new google.maps.Map(document.getElementById(mapElement), mapOptions);
		}
		getLocation();
		this.initMarkers();
}

function getLocation() {
  if (navigator.geolocation) {
  	positioningInterval = setInterval(function() {
	  	navigator.geolocation.getCurrentPosition(showPosition, geolocationError, {enableHighAccuracy: true});
  	}, 500);
  } else {
  		alert("Positionsbestimmung auf diesem Gerät nicht möglich");
  	}
}

function showPosition(position){
	accountData.setCurrentPosition(position);
	playerAvatars[0].marker.setPosition(new google.maps.LatLng(position.coords.latitude, position.coords.longitude));
}

function geolocationError(error){
}

function isInitialized(id) {
    return !! document.getElementById(id).firstChild;
}


this.stopPositioning = function() {
	clearInterval(positioningInterval);
}


this.initMarkers = function(){
	//load images from server
	loadImages(function() {
		//init avatar markers
		var playerInfo = [{username: accountData.getUsername(), userid: accountData.getUserID(), position: {latitude: accountData.getCurrentPosition().coords.latitude, longitude: accountData.getCurrentPosition().coords.longitude}}, {username: "Stefan Gföllner", userid: "1690871472", position: {latitude: 48.337428, longitude: 14.319928}}, {username: "Michael Hartlauer", userid: "1790871472", 								position: {latitude: 48.337328, longitude: 14.321237}}];
		initAvatarMarkers(playerInfo);
		
		//init goodie markers
		var goodieInfo = [{latitude: 48.337066, longitude: 14.318477, specialAction: null, points: 50}, {latitude: 48.337191, longitude: 14.318498, specialAction: null, points: 25}];
		initGoodieMarkers(goodieInfo);
	});
}

function initAvatarMarkers(playerInfo) {
	for (var i=0; i<playerInfo.length; i++) {
		var player = playerInfo[i];
		//create avatar
		if(gameData.isTeamRead(player.username)) {
			initAvatar("red", player);	
		}
		else initAvatar("blue", player);
   }
}

function initAvatar(team, player) {
	createPlayerAvatar("red", player.userid, player.username, 0, null, function(pngURL, canvas) {
		var playerMarker = new google.maps.Marker({
			    	position: new google.maps.LatLng(player.position.latitude, player.position.longitude),
					map: map,
					icon: pngURL
		});		
			
		//callout
		var infoWindowOpts = {
			  content: player.username
		};		
	
		var infoWindow = new google.maps.InfoWindow(infoWindowOpts);
		google.maps.event.addListener(playerMarker, 'click', function() {
			infoWindow.open(map, playerMarker);
		});
			
		playerAvatars.push({userID: player.userid, username: player.username, specialAction: null, marker: playerMarker, canvas: canvas});	
	
		//redraw once
			redrawPlayerAvatar(player.userid, player.username, 50);
			drawSpecialAction(player.userid, player.username, 100, "doublePoints");	
		//	removeSpecialAction(player.userid, player.username, 100);						
	});	
}

function initGoodieMarkers(goodieInfo) {
	for (var i=0; i<goodieInfo.length; i++) {
		var goodie = goodieInfo[i];
		var markerImg;
		var goodieImage;
		if (goodie.points === 25) {
			goodieImage = goodieImages.standard;
			markerImg = new google.maps.MarkerImage(goodieImage.src,
												    null, 
												    null, 
												    null, 
												    new google.maps.Size(10, 10));
		}else if (goodie.points === 50) {
			goodieImage = goodieImages.big;
			markerImg = new google.maps.MarkerImage(goodieImage.src,
												    null, 
												    null, 
												    null, 
												    new google.maps.Size(20, 20));
		}else if (goodie.specialAction) {
			goodieImage = goodieImages.special;
			markerImg = new google.maps.MarkerImage(goodieImage.src,
												    null, 
												    null, 
												    null, 
												    new google.maps.Size(20, 20));
		}
			
		var goodie = new google.maps.Marker({
    					position: new google.maps.LatLng(goodie.latitude, goodie.longitude),
						map: map,
						icon: markerImg
		});
		goodies.push(goodie);
	}
}


function loadImages(callback) {
	var loaded = 0;
	var loadedCallback = function() {
		loaded++;
	    if (loaded === 3) {
	    	callback();
	    }
	}
	//load avatar images
	loadAvatarImages(loadedCallback);
	//load goodie images
	loadGoodieImages(loadedCallback);
	//load special action images
	loadSpecialActionImages(loadedCallback);
}

function loadAvatarImages(callback) {
	var imgSrcs = ["img/redAvatar.png", "img/blueAvatar.png"];
	var loadCallback = function (imgs) {
    	avatarImages.red = imgs[0];
	    avatarImages.blue = imgs[1];
	    callback();
	};
	
	//error callback for image load
	var errorCallback = function() {
		console.log("Error in loading avatar images");
	}
	
	//load images from server
   loadImagesFromServer(imgSrcs, loadCallback, errorCallback);
}

function loadGoodieImages(callback) {
	var imgSrcs = ["img/standard.png", "img/big.png", "img/special.png"];
	var loadCallback = function (imgs) {
    	goodieImages.standard = imgs[0];
	    goodieImages.big = imgs[1];
	    goodieImages.special = imgs[2];
	    callback();
	};
	
	//error callback for image load
	var errorCallback = function() {
		console.log("Error in loading goodie images");
	}
	
	//load images from server
   loadImagesFromServer(imgSrcs, loadCallback, errorCallback);
}

function loadSpecialActionImages(callback) {
	var imgSrcs = ["img/invincible.png"];
	var loadCallback = function (imgs) {
    	specialActionImages.invincible = imgs[0];
    	callback();
	};
	
	//error callback for image load
	var errorCallback = function() {
		console.log("Error in loading specialAction images");
	}
	
	//load images from server
   loadImagesFromServer(imgSrcs, loadCallback, errorCallback);
}

function loadImagesFromServer(imgSrcs, loadedCallback, errorCallback) {
	var loaded = 0;
	var imgs = [];
	var loadCallback = function () {
    	loaded++;
	    if (loaded === imgSrcs.length) {
	    	loadedCallback(imgs);
	    }
	};
	 
	for (var i = 0; i < imgSrcs.length; i++) {
	   imgs[i] = new Image();
	   imgs[i].addEventListener('load', loadCallback, false);
	   imgs[i].onerror = errorCallback;		
	   imgs[i].src = imgSrcs[i];
	   imgs[i].crossOrigin = "Anonymous";
	}
}


function createPlayerAvatar(team, userID, username, points, userImg, loadedCallback) {
	var canvas, context;
	canvas = document.createElement("canvas");
    canvas.width = playerAvatarWidth;
    canvas.height = playerAvatarHeight;
    context = canvas.getContext("2d");
      
	var avatarImg;
    if (team === "red"){
	   	avatarImg = avatarImages.red;
    }else {
	   	avatarImg = avatarImages.blue;
    }
    drawPlayerAvatar(context, username, avatarImg, userImg, points, null);
    loadedCallback(canvas.toDataURL(), {canvas: canvas, markerImg: avatarImg, userImg: null});
}

function redrawPlayerAvatar(userID, username, points) {
	var playerAvatar = getPlayerAvatarByUserID(userID);
	//clear old canvas 
	var canvas = playerAvatar.canvas.canvas;
	var context = canvas.getContext("2d");
	context.clearRect(0, 0, playerAvatarWidth, playerAvatarHeight);
	
	//draw new canvas
	drawPlayerAvatar(context, username, playerAvatar.canvas.markerImg, playerAvatar.canvas.userImg, points, playerAvatar.specialAction);
	//set new canvas to marker
	playerAvatar.marker.setIcon(canvas.toDataURL());	
}

function drawSpecialAction(userID, username, points, specialAction) {
	var playerAvatar = getPlayerAvatarByUserID(userID);
	playerAvatar.specialAction = specialAction;
	redrawPlayerAvatar(userID, username, points);
}


function removeSpecialAction(userID, username, points) {
	var playerAvatar = getPlayerAvatarByUserID(userID);
	playerAvatar.specialAction = null;
	redrawPlayerAvatar(userID, username, points);
}

function drawPlayerAvatar(context, username, markerImg, userImg, points, actionType) {
	context.drawImage(markerImg, 0, 10, 62, 85);
	context.fillStyle = "rgb(255,255,255)";
	if (userImg) {
		context.drawImage(userImg, 5.0, 15, 52, 52);
	}else {
		context.textAlign = "center";
	 	context.font = "15px sans-serif";
	 	if (username.length > 6){
		 	username = username.substr(0, 6);
	 	}
		context.fillText(username, 31, 45); 
	}
	context.font = "10px sans-serif";
	context.textAlign = "center";
	context.fillRect (5.0,65,52,12);
	context.fillStyle = "rgb(0,0,0)";
	context.lineWidth = 1;
	context.strokeText(points+" Pkt.", 31, 75); 
		
	if (actionType) {
		context.beginPath();
		context.arc(55,16.5,15,0,2*Math.PI);
		context.lineWidth = 3;
		context.strokeStyle = "rgb(0,0,0)";
		context.stroke();
		context.fillStyle = 'white';
		context.fill();
		context.lineWidth = 1;
		if (actionType == "invincible") {
			context.drawImage(specialActionImages.invincible, 44, 6, 22, 22);
		}else {
			context.strokeText("x2", 55, 20);
		}
	}		
}


function getPlayerAvatarByUserID(userID) {
	returnVal = null;
	for (i=0; i<playerAvatars.length; i++){
		if (playerAvatars[i].userID == userID) {
			returnVal = playerAvatars[i];
			return returnVal;
		}
	}
}

this.removeMarkers = function(){
	for (i=0; i<goodies.length; i++){
		goodies[i].setMap(null);
	}
	goodies = [];
	
	for (i=0; i<playerAvatars.length; i++){
		playerAvatars[i].marker.setMap(null);
		
	}
	for (i=0; i<playerAvatars.length; i++){
		playerAvatars[i].canvas.canvas = null;
	}
	playerAvatars = [];
}
 
this.resize = function(mapElement){
	//resize map in relation to screen height
    var height = $(window).height() - 140;
    var width = $(window).width();
    //alert(height);
   
    $("#"+mapElement).height(height);
    $("#"+mapElement).width(width);
    
    google.maps.event.trigger($("#"+mapElement), 'resize'); 
}



	
	
}


