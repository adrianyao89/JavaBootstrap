package cn.unstudy.plugin;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Adrian Yao on 2016/9/13.
 */
public final class FileToZip {

    private FileToZip(){}

    public static void zip(String src, String desc) throws IOException {
        File root = new File(src);
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(desc));
            zip(root, "", zos);
        } finally {
            if (zos != null) {
                zos.close();
            }
        }

    }

    private static void transferFileToZip(File f, String dest, ZipOutputStream zos) throws IOException {

        InputStream is = null;
        try {
            is = new FileInputStream(f);

            ZipEntry ze = new ZipEntry(dest);
            zos.putNextEntry(ze);
            int index = 0;
            byte[] buff = new byte[2048];
            while ((index = is.read(buff)) > 0) {
                zos.write(buff, 0, index);
            }

            zos.closeEntry();
        } finally {
            is.close();
        }



    }

    private static void transferDirToZip(String dest, ZipOutputStream zos) throws IOException {
        String name = dest.endsWith("/") ? dest : dest + "/";
        ZipEntry ze = new ZipEntry(name);
        zos.putNextEntry(ze);
        zos.closeEntry();
    }

    private static void zip(File f, String relatedPath, ZipOutputStream zos) throws IOException {
        String currentRelatedPath = relatedPath + "/" + f.getName();
        if (f.isFile()) {
            transferFileToZip(f, currentRelatedPath, zos);
        } else if (f.isDirectory()) {
            File[] fs = f.listFiles();

            if (fs != null && fs.length > 0) {
                for (File cf : f.listFiles()){
                    zip(cf, currentRelatedPath, zos);
                }
            } else {
             transferDirToZip(currentRelatedPath, zos);
            }
        }
    }

}