function printer(data) {
	siteR = /(https:\\/\\/[img1]*.tumangaonline.me\\/uploads\\/[^\\\\\\\"^']+)/g;
	path = siteR.exec(data)[0];
	idsR = /<canvas id=\\\"([^\\\"]+)/g;
	dataa = "";
	do{
		m = idsR.exec(data);
		if(m){
			dataa = dataa + m[1] + \"|\";
		}
	}while(m);
	ids = dataa.split(\"|\");
	idsL = ids.length - 1;
	images = \"\";
	for(i = 0; i < idsL; i++) {
		rs = ids[i] + \".{0,100}?'([^']+.jpg)\";
		fid = new RegExp(rs);
		mid = fid.exec(data);
		images = images + \"|\" + path + mid[1];
	}
	return images;
}