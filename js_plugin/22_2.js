var S22 = {
chapterInit: 	function (cw, mw) {
			pipe = '|';
			var data = nav.get(mw, '');
			rTo = /"([^"\s\.]{40})"/gm;
			var token = '_token=' + rTo.exec(data)[1];
			r64 = /"([a-z0-9]{32})",/gm;
			var b64 = r64.exec(data)[1];
			data = nav.postM(cw + "/" + b64, 'Referer|' + mw, token);
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
			return "<div class=\"col-10 text-truncate\"[$s$S]+?(<.+?</a>)[$s$S]+?\"_.+?$('([^']+)";
		},
cre2: 		function(){
			return "<div class=\"col-4 col-md-6 text-truncate\">([^\\']+)<\\/span>[\\s\\S]+?\"_.+?\\('([^']+)[\\s\\S]";
		},
};
