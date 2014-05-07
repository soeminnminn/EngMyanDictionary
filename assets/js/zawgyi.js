//
document.ready = function(callback) {
	var readyBound = false;
	
	var fireReady = function() {
		if( typeof callback === 'function' ){
			callback();
		}
	};
	var bindReady = function() {
		if ( readyBound ) return;
		readyBound = true;
		
		// Mozilla, Opera and webkit nightlies currently support this event
		if ( document.addEventListener ) {
			// Use the handy event callback
			document.addEventListener( "DOMContentLoaded", function(){
				document.removeEventListener( "DOMContentLoaded", arguments.callee, false );
				fireReady();
			}, false );

		// If IE event model is used
		} else if ( document.attachEvent ) {
			// ensure firing before onload,
			// maybe late but safe also for iframes
			document.attachEvent("onreadystatechange", function(){
				if ( document.readyState === "complete" ) {
					document.detachEvent( "onreadystatechange", arguments.callee );
					fireReady();
				}
			});
		}

		if (window.event) {
			// A fallback to window.onload, that will always work
			window.event.add( window, "load", fireReady );
		}
	};

	bindReady();
};

document.ready(function() {
	var style=document.createElement('style');
	s.innerHTML = "@font-face {";
	s.innerHTML += "    font-family: 'zawgyi-one';";
	s.innerHTML += "	 src: url('file:///android_asset/fonts/zawgyi.ttf');";
	s.innerHTML += "	 src: url('file:///android_asset/fonts/zawgyi.woff') format('woff'), ";
	s.innerHTML += "			url('file:///android_asset/fonts/zawgyi.ttf') format('truetype'), ";
	s.innerHTML += "			url('file:///android_asset/fonts/zawgyi.svg#webfont') format('svg');";
	s.innerHTML += "    font-weight: normal;";
	s.innerHTML += "    font-style: normal;";
	s.innerHTML += "}";
	s.innerHTML += "body,div,h1,h2,h3,input,textarea {";
	s.innerHTML += "	font-family: 'zawgyi-one'!important;";
	s.innerHTML += "	-webkit-font-smoothing: antialiased!important;";
	s.innerHTML += "	text-rendering:optimizeLegibility;";
	s.innerHTML += "}";
	document.getElementsByTagName('head')[0].appendChild(s);
	
    var text = document.body.innerHTML;
    for (i = 0; i < 0x9F; i++) {
        text = text.replace(String.fromCharCode(0x1000 + i), String.fromCharCode(0xFA00 + i));
    }
    document.body.innerHTML = text;
});
