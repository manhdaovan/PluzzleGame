package com.manhdaovan.pluzzlegame.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";
    public static final int MODE_ALL = 1;
    public static final int MODE_FILE_ONLY = 2;
    public static final int MODE_DIR_ONLY = 3;

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Nullable
    public static String uriToMd5(ContentResolver contentResolver, Uri uri) {
        MessageDigest messageDigest = uriToMsgDigest(contentResolver, uri);
        if (messageDigest != null) {
            return bytesToHex(messageDigest.digest());
        }
        return null;
    }

    @Nullable
    public static MessageDigest uriToMsgDigest(ContentResolver contentResolver, Uri uri) {
        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            BufferedInputStream inputStream = new BufferedInputStream(contentResolver.openInputStream(uri));
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
            byte[] buffer = new byte[1024];
            while (digestInputStream.read(buffer, 0, buffer.length) != -1) ;

        } catch (Exception e) {
            messageDigest = null;
        }

        return messageDigest;
    }

    @Nullable
    public static File createFolder(Context context, String folderName) {
        try {
            File fileDir = context.getFilesDir();
            File newFolder = new File(fileDir.getAbsolutePath() + "/" + folderName);

            if (newFolder.exists() || newFolder.mkdir()) {
                alert(context, "Create folder: " + newFolder.toString());
                Log.e(TAG, "Create folder: " + newFolder.toString());
                return newFolder;
            } else {
                alert(context, "Cannot create folder: " + newFolder.toString());
                Log.e(TAG, "Cannot create folder: " + newFolder.toString());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<File> getDirs(File parent, int mode) {
        File[] allFiles = parent.listFiles();
        List<File> dirs = new ArrayList<>();

        for (File f : allFiles) {
            Log.e("DIRRR getAbsolutePath", "" + f.getAbsolutePath());
            Log.e("DIRRR isDirectory", "" + f.isDirectory());
//            Utils.getAllSubFilesAndFolders(f);
            switch (mode) {
                case MODE_ALL:
                    dirs.add(f);
                    break;
                case MODE_DIR_ONLY:
                    if (f.isDirectory()) dirs.add(f);
                    break;
                case MODE_FILE_ONLY:
                    if (f.isFile()) dirs.add(f);
                    break;
                default:
                    break;
            }
        }

        return dirs;
    }

    public static File saveFile(Context context, String targetFolder, String targetFileName, String sourceFile) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        File source = new File(sourceFile);
        File target = new File(targetFolder, targetFileName);

        try {
            if (!target.exists()) target.createNewFile();

            sourceChannel = new FileInputStream(source).getChannel();
            targetChannel = new FileOutputStream(target).getChannel();

            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

            Log.e(TAG, "saveFile OK: " + targetFolder + "/" + targetFileName);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveFile NOTTTT OK: " + targetFolder + "/" + targetFileName + ":::" + e.getMessage());
            return null;
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (targetChannel != null) {
                targetChannel.close();
            }
        }
    }

    public static File saveFile(Context context, File target, Bitmap bitmap) throws IOException {
        FileOutputStream fos = null;

        try {
            if (!target.exists()) target.createNewFile();

            fos = new FileOutputStream(target);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            Log.e(TAG, "save piece File OK: " + target.getAbsolutePath());
            return target;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "save piece NOTTTT OK: " + target.getAbsolutePath() + ":::" + e.getMessage());
            return null;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static List<File> getAllSubFilesAndFolders(File parent) {
        File[] allFiles = parent.listFiles();
        List<File> results = new ArrayList<>();

        if(parent.isFile() || allFiles == null) return results;

        for (File f : allFiles) {
            Log.e("DIRRR getAbsolutePath", "" + f.getAbsolutePath());
            Log.e("DIRRR isDirectory", "" + f.isDirectory());
            results.add(f);
        }

        return results;
    }

    public static List<Bitmap> sliceImg(String orgImg, int rows, int cols) {
        List<Bitmap> pieces = new ArrayList<>();
        Bitmap orgImgBitmap = BitmapFactory.decodeFile(orgImg);
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(orgImgBitmap, orgImgBitmap.getWidth(), orgImgBitmap.getHeight(), true);

        int pieceHeight = orgImgBitmap.getHeight() / rows;
        int pieceWidth = orgImgBitmap.getWidth() / cols;

        int yCo = 0;

        for (int x = 0; x < rows; x++) {

            int xCo = 0;

            for (int y = 0; y < cols; y++) {

//                pieces.add(Bitmap.createBitmap(scaledBitmap, xCo, yCo, pieceWidth, pieceHeight));
                pieces.add(Bitmap.createBitmap(orgImgBitmap, xCo, yCo, pieceWidth, pieceHeight));

                xCo += pieceWidth;

            }

            yCo += pieceHeight;

        }

        return pieces;
    }

    public static List<File> getAllPieces(File gameResource) {
        List<File> allFiles = getDirs(gameResource, MODE_FILE_ONLY);
        List<File> pieces = new ArrayList<>();

        for (File file : allFiles) {
            if (!skipPiece(file.getName())) pieces.add(file);
        }
        return pieces;
    }

    public static String buildPieceName(int index) {
        return Constants.DEFAULT_FILE_NAME + "_" + index + Constants.DEFAULT_FILE_MIME;
    }

    public static void alert(Context context, String msg) {
        Log.e(TAG, "ALERT ------ " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private static boolean skipPiece(String pieceName) {
        return pieceName.equals(Constants.defaultCroppedFileName()) || pieceName.equals(Constants.defaultFirstPieceName());
    }
}
