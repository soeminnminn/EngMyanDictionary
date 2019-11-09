/*
* @author Soe Minn Minn
*/
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
    var options = { "addfont": true, "drawfix": true, "applykeywords": false};
    var metaOptions = document.getElementsByName("Options");
    if (metaOptions.length > 0) {
        strOpts = metaOptions[0].getAttribute("content").replace(/'(\w+?)'/g, "\"$1\"");
        options = JSON.parse(strOpts);
    }
    var isChanged = options.drawfix || options.applykeywords;
	if (isChanged)  {
		var text = document.body.innerHTML;
		if (options.drawfix) {
			for (i = 0; i < 0x9F; i++) {
				text = text.replace(String.fromCharCode(0x1000 + i), String.fromCharCode(0xFA00 + i));
			}
		}
		if (options.applykeywords) {
			var metaKeywords = document.getElementsByName("Keywords");
			if (metaKeywords.length > 0) {
				var strKeywords = metaKeywords[0].getAttribute("content");
				if (strKeywords != null && strKeywords != '') {
					if (strKeywords.toLowerCase().indexOf('word') > -1) {
						text = text.replace(/([^A-Za-z\/\?=])(word)([^A-Za-z\/\?=])/gi, '$1<a href="' + window.location + '?word=word">$2</a>$3');
					}
					var keywords = strKeywords.split(',');
					for (i = 0; i < keywords.length; i++) {
						if (keywords[i].toLowerCase() == 'word') continue;
						regex = eval("/([^A-Za-z\\/\\?=])(" + keywords[i] + ")([^A-Za-z\\/\\?=])/gi");
						text = text.replace(regex, '$1<a href="' + window.location + '?word=' + keywords[i] + '">$2</a>$3');
					}
				}
			}
		}
		document.body.innerHTML = text;
	}
    if (options.addfont) {
        var style = document.createElement('style');
        style.innerHTML = "@font-face {";
        style.innerHTML += "    font-family: 'zawgyi-one';";
        style.innerHTML += "	 src: url('file:///android_asset/fonts/zawgyi.ttf');";
        style.innerHTML += "	 src: url('file:///android_asset/fonts/zawgyi.woff') format('woff'), ";
        style.innerHTML += "			url('file:///android_asset/fonts/zawgyi.ttf') format('truetype'), ";
        style.innerHTML += "			url('file:///android_asset/fonts/zawgyi.svg#webfont') format('svg');";
        style.innerHTML += "    font-weight: normal;";
        style.innerHTML += "    font-style: normal;";
        style.innerHTML += "}";
        style.innerHTML += "body,div,h1,h2,h3,input,textarea {";
        style.innerHTML += "	font-family: 'zawgyi-one'!important;";
        style.innerHTML += "	-webkit-font-smoothing: antialiased!important;";
        style.innerHTML += "	text-rendering:optimizeLegibility;";
        style.innerHTML += "}";
        document.getElementsByTagName('head')[0].appendChild(style);
    }
});
