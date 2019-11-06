function chapterInit(data) {
	pipe = '|';
	var web = nav.getRedirectWeb(data, 'Referer|' + data);
	var id = web.split('/')[4];
	var src = nav.get('https://tmofans.com/viewer/' + id + "/cascade", 'Referer|' + data);
	rImg = /(https:..img1.tmofans.com.uploads.[^"]+)/gm;
	oImg = "https://tmofans.com/viewer/" + id + "/cascade";
	do {
		i = rImg.exec(src);
		if (i) {
			oImg = oImg + pipe + i[1];
		}
	} while (i);
	return oImg;
}
