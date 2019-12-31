var S22 = {
chapterInit: 	function (cw, mw) {
			pipe = '|';
			cid = cw.substr(cw.lastIndexOf("/")+1);
			var data = nav.get(mw, '');
			rTo = /csjf-token" content="([^"]+)/gm;
			var token = 'X-CSJF-TOKEN|' + rTo.exec(data)[1];
			rTo = /csrf-token" content="([^"]+)/gm;
			token = token + '|X-CSRF-TOKEN|' +rTo.exec(data)[1];
			rUr = /l:\s*['"]([^'"]+)[^\}]+:[^;,}]+/gm;
			rUr.exec(data);
			var ur = rUr.exec(data)[1];
			tDta = /data:\{[\s\S]+?\}/gm;
			data = tDta.exec(data);
			rSd = /["']([^"']+)["']:([^,\}]+)/gm;
			sd = rSd.exec(data);
			var pt = sd[1];
			sd = rSd.exec(data);
			pt = pt + '|' + cid + '|' + sd[1] + '|' + sd[2].trim();
			data = nav.post(ur, 'Referer|' + mw + '|X-Requested-With|XMLHttpRequest|'+ token, pt);
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