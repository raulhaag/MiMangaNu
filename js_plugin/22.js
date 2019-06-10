function chapterInit(data) {
  pipe = "|";
  idsR = /<img[^>]+src="([^"]+)" class="viewer-image"/gm;
  idData = ""
  do {
    m = idsR.exec(data);
    if (m) {
      idData = idData + pipe + m[1] ;
    }
  } while (m);
  if(idData == ""){
    idsR = /img_[^\.^;]+\.src\s*=\s*"([^"]+)/gm;
    do {
      m = idsR.exec(data);
      if (m) {
        idData = idData + pipe + m[1] ;
      }
    } while (m);
  }
  return idData;
 }
