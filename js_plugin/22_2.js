var S22 = {
chapterInit: 	function (cw, mw) {
			pipe = '|';
			cw = cw.substr(cw.lastIndexOf("/")+1);
			var data = nav.get(mw, '');
			rTo = /-token" content="([^"]+)/gm;
			var token = '_token|' + rTo.exec(data)[1];
			tPo = /"name".+? '(_[^']+)[^"]+"value", ([^\)]+)/gm;
			rPo = tPo.exec(data);
			token = token + "|" + rPo[1] + "|" + cw ;
			rPo = tPo.exec(data);
			token = token + "|" + rPo[1] + "|" + rPo[2] ;
			rAc = /setAttribute\(["']action["'],\s*["']([^"']+)/gm;
			act = rAc.exec(data)[1];
			data = nav.post(act, 'Connection|keep-alive|Referer|' + mw, token);
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
