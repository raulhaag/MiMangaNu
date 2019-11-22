var S22 = {
chapterInit: 	function (cw, mw) {
			pipe = '|';
			var data = nav.get(mw, '');
			rTo = /-token" content="([^"]+)/gm;
			var token = '_token|' + rTo.exec(data)[1];
			tPo = /te\("name", "([^"_]+)"[\s\S]+?\("[^"]+",'*([^)']+)/gm;
			r64 = /", 'https:\/\/tmofans.com\/([^\/]+\/[^\/]+)/gm;
			var b64 = r64.exec(data)[1];
			cw = cw.substr(cw.lastIndexOf("/")+1);
			data = nav.post("https://tmofans.com/" + b64 + "/" + cw + "/gtu", 'Connection|keep-alive|Referer|' + mw, token);
			rId =/\/viewer\/([^/]+)/gm;
			var id = rId.exec(data)[1];
			src = nav.get("https://tmofans.com/viewer/" + id + "/cascade", 'Referer|' + cw);
			rImg = /<img src="(https:\/\/img1.tmofans.com\/[^"]+)/gm;
			oImg = "https://tmofans.com/viewer/" + id + "/cascade";
			do {
				i = rImg.exec(src);
				if (i) {
					oImg = oImg + pipe + i[1];
				}
			} while (i);
			return oImg;
		},
cre1: 		function(){
			return "<div class=\"col-10 text-truncate\"[$s$S]+?(<.+?</a>)[$s$S]+?\"(\\d+)\" ";
		},
cre2: 		function(){
			return "<div class=\"col-4 col-md-6 text-truncate\">([^']+)</span>[$s$S]+?\"(\\d+)\" ";
		},
};