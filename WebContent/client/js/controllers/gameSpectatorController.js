var gameSpectatorController = (function() {

	var map = null;

	//page events
	function pageInit() {
		map = new mapController();
		map.resize("map_canvas_spectator");
		bindUIActions();
		bindServiceMessages();
	}

	function pageBeforeShow() {
	}

	function pageShow() {
		//alert("page show login");
		map.initMap("map_canvas_spectator");
	}

	function bindUIActions() {
		cancelButtonPressed();
	}

	function bindServiceMessages() {
		amplify.subscribe('messageType', function(data) {
			//call private function
		});
	}

	//control events
	function cancelButtonPressed() {
		$("#cancelButtonSpectatorGame").on("click", function(event, ui) {
			map.removeMarkers();
			map.stopPositioning();
		});
	}

	function resizeMap() {
		map.resize("map_canvas_spectator");
	}

	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow,
		resizeMap : resizeMap
	}
})();