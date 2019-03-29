function chapterInit(data) {
       pipe = "|";
       idsR = /<img[^>]+" src="([^"]+)/gm;
       idData = ""
       do {
           m = idsR.exec(data);
           if (m) {
               idData = idData + pipe + m[1] ;
           }
       } while (m);
       return idData;
   }