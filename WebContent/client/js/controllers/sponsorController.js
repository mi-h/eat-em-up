var sponsorController = (function() {
	
	//page events
	function pageInit() {
		bindUIActions();
		bindServiceMessages();
	}
	
	function pageBeforeShow() {
		$("#totalPointsSponsor").text(accountData.getPoints());
	}
	
	function pageShow() {
	}
	
	function bindUIActions() {
	}
	
	function bindServiceMessages() {
	}
			
	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow
	}
})();