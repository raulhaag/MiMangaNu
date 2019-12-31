var S22 = {
chapterInit: 	function (cw, mw) {
			pipe = '|';
			var data = '';
			cid = cw.substr(cw.lastIndexOf("/")+1);
			rTo = /"csrf-token" content="([^"]+)/gm
			mw = mw.replace('+', '-');
			var id = '';
			try{
				data = nav.get(mw, '');
				vto = rTo.exec(data)[1];
				met = '|' + vto + '|' + vto + '|X-CSRF-TOKEN|' + vto;
				rUr = /l:\s*['"]([^'"]+)[^\}]+:[^;,}]+/gm;
				rUr.exec(data);
				var ur = rUr.exec(data)[1];
				tDta = /data\s*:\s*\{[\s\S]+?\}/gm;
				data = tDta.exec(data);
				rSd = /["']([^"']+)["']:([^,\}]+)/gm;
				sd = rSd.exec(data);
				var pt = sd[1];
				sd = rSd.exec(data);
				pt = pt + '|' + cid + '|' + sd[1] + '|' + sd[2].trim();
				data = nav.post(ur, 'Referer|' + mw + '|X-Requested-With|XMLHttpRequest' + met, pt);
				rId =/\/viewer\/([^/]+)/gm;
				id = rId.exec(data)[1];
			}catch(error) {return error;
				pipe = '|';
				mw = mw.replace('tmofans.com','lectormanga.com');
				data = nav.get(mw, '');
				rTo = /-token" content="([^"]+)/gm;
				var token = '_token|' + rTo.exec(data)[1];
				tPo = /"name".+? '(_[^']+)[^"]+"value", ([^\)]+)/gm;
				rPo = tPo.exec(data);
				token = token + "|" + rPo[1] + "|" + cid ;
				rPo = tPo.exec(data);
				token = token + "|" + rPo[1] + "|" + rPo[2] ;
				rAc = /setAttribute\(["']action["'],\s*["'](https:\/\/lectormanga.com\/[^"']+)/gm;
				act = rAc.exec(data)[1];
				data = nav.post(act, 'Connection|keep-alive|Referer|' + mw, token);
				rId =/\/noticia\/([^\/]+)/gm;
				id = rId.exec(data)[1];
			}
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