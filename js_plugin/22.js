function chapterInit(data) {
	siteR = new RegExp("(https://[img1]*.tumangaonline.me/uploads/[^\"^']+)");
	path = siteR.exec(data)[0];
	idsR = new RegExp("<canvas id=\"([^\"]+)");
	idData = "";
	do{
		m = idsR.exec(data);
		if(m){
			idData = idData + m[1] + "|";
		}
	}while(m);
	ids = idData.split(\"|\");
	idsL = ids.length - 1;
	images = \"\";
	for(i = 0; i < idsL; i++) {
		rs = ids[i] + ".{0,100}?'([^']+.jpg)";
		fid = new RegExp(rs);
		mid = fid.exec(data);
		images = images + "|" + path + mid[1];
	}
	return images;
}