function chapterInit(cw, mw) {
	pipe = '|';
	var data = nav.get(mw, '');
	rTo = /'value',"([^"]+)/gm;
	var token = '_token=' + rTo.exec(data)[1];
	r64 = /'(base64[^'|^\}]+)'/gm;
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
}
