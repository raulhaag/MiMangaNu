function chapterInit(data) {
    pipe = "|";
    siteR = /(https:\/\/[img1]*.tumangaonline.me\/uploads\/[^\\\"^']+)/g;
    path = siteR.exec(data)[0];
    idsR = /<canvas id=\"([^\"]+)/g;
    idData = ""
    do {
        m = idsR.exec(data);
        if (m) {
            idData = idData + m[1] + pipe;
        }
    } while (m);
    ids = idData.split(pipe);
    idsL = ids.length - 1;
    images = "";
    for (i = 0; i < idsL; i++) {
        rs = ids[i] + ".{0,100}?'([^']+.jpg)";
        fid = new RegExp(rs);
        mid = fid.exec(data);
        images = images + pipe + path + mid[1];
    }
    return images;
}