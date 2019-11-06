function chapterInit(data) {
	pipe = '|';
	var web = nav.getRedirectWeb(data, 'Referer|' + data);
	var id = web.split('/')[4];
	var imgBase = 'https://img1.tmofans.com/uploads/' + id + '/';
	var token = nav.get('https://tmofans.com/viewer/' + id + "/cascade", 'Referer|' + data);
	rTok = /<meta name="csrf-token" content="([^"]+)\">/gm;
	rE = /e\('([^']+)','([^']+)','([^']+)'\)/gm;
	t = rTok.exec(token);
	if(!t){
	  return '';
	}
	e = rE.exec(token);
	if(!e){
	  return '';
	}
	headers = "Referer|https://tmofans.com/viewer/" + id + "/cascade";
	headers = headers + "|X-CSRF-TOKEN|" + t[1];
	imgweb = "https://tmofans.com/upload_images/" + id + "/" + e[2] +"/all";
	imgs = nav.post(imgweb, headers, '');
	rImg = /"([^"^,^\]]+)/gm;
	oImg = "https://tmofans.com/viewer/" + id + "/cascade";
	do {
		i = rImg.exec(imgs);
		if (i) {
			oImg = oImg + pipe + imgBase + i[1];
		}
	} while (i);
	return oImg;
}
