package com.boredream.ktvkrcview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class KrcParse {

    public static List<KrcLine> parseKrcFile(String filePath) {
        List<KrcLine> krcLines = null;
        //解密解压
        try {
            krcLines = new ArrayList<>();
            String lines = getKrcText(filePath);
            String lineArray[] = lines.split("\r\n");

            if (lineArray.length <= 9) {
                // header内容为9行，小于代表无歌词
                return krcLines;
            }

            for (String line : lineArray) {
                KrcLine krcLine = parseKrcLine(line);
                if (null != krcLine) {
                    krcLines.add(krcLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return krcLines;
    }

    /**
     * 解析一行歌词
     */
    private static KrcLine parseKrcLine(String line) {
        KrcLine krcLine = null;
        try {
            if (line != null && line.matches("\\[.+\\].+")) {
                krcLine = new KrcLine();
                line = line.substring(1);
                String strArray[] = line.split("\\]", 2);
                String timeStr[] = strArray[0].split(",");
                krcLine.lineTime.startTime = Long.parseLong(timeStr[0]);
                krcLine.lineTime.duration = Long.parseLong(timeStr[1]);
                String lyricsStr[] = strArray[1].split("[<>]");
                for (int i = 1; i < lyricsStr.length; i += 2) {
                    String wordTime[] = lyricsStr[i].split(",");
                    if (wordTime.length < 2) {
                        continue;
                    }
                    long startT = Long.parseLong(wordTime[0]);
                    long spanT = Long.parseLong(wordTime[1]);

                    KrcWord krcWord = new KrcWord();
                    krcWord.wordTime = new KrcTime(startT, spanT);
                    krcWord.wordStr = lyricsStr[1 + i];
                    krcWord.wordStartIndex = krcLine.lineStr.length();

                    krcLine.wordTimes.add(krcWord);
                    krcLine.lineStr += krcWord.wordStr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return krcLine;
    }

    //解密krc数据
    private static String getKrcText(String filePath) throws IOException {
        //解密的key
        int[] miarry = {64, 71, 97, 119, 94, 50, 116, 71, 81, 54, 49, 45, 206, 210, 110, 105};
        File krcfile = new File(filePath);
        byte[] zip_byte = new byte[(int) krcfile.length()];
        FileInputStream fis = new FileInputStream(krcfile);
        byte[] top = new byte[4];
        fis.read(top);
        fis.read(zip_byte);
        int j = zip_byte.length;
        for (int k = 0; k < j; k++) {
            int l = k % 16;
            int tmp67_65 = k;
            byte[] tmp67_64 = zip_byte;
            tmp67_64[tmp67_65] = (byte) (tmp67_64[tmp67_65] ^ miarry[l]);
        }
        return new String(decompress(zip_byte), "utf-8");
    }

    private static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

}
