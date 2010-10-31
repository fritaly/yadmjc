(function($) {
	if( !wgClickTrackingIsThrottled ) {
		// creates 'track action' function to call the clicktracking API and send the ID
		$.trackAction = function ( id ) {
			$j.post( wgScriptPath + '/api.php', { 'action': 'clicktracking', 'eventid': id, 'token': wgTrackingToken } );
		};
	}

})(jQuery);
