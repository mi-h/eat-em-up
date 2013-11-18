var lobbyController = (function() {
	
	//page events
	function pageInit() {
		//alert("page init login");
		bindUIActions();
	}
	
	function pageBeforeShow() {
	}
	
	function pageShow() {
		//alert("page show login");
	}
	
	function bindUIActions(){
		checkboxClicked();
	}
		
	checkboxClicked = function(){
		$("#checkbox-1").on("click", function(event, ui) {
			if ($("#checkbox-1").prop("checked") === true){
				$("#checkbox-1").prop("checked",true).checkboxradio("refresh");
					setTimeout(function(){
					$("#checkbox-1").prop("checked",false).checkboxradio("refresh");
					$.mobile.changePage("#gamePage", { transition: "pop", changeHash: true });
				},2000);	
			
				//player is ready
				
			}else{
				//player is not ready anymore
				
			}				  
		});
	}
	
	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow
	}
})();