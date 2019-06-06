function chapterInit(data) {
       pipe = "|";
       idsR = /img_[^\.^;]+\.src\s*=\s*"([^"]+)/gm;
       idData = ""
       do {
           m = idsR.exec(data);
           if (m) {
               alert(m);
               idData = idData + pipe + m[1] ;
           }
       } while (m);
       return idData;
   }
