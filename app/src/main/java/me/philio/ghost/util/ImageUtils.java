/*
 * Copyright 2014 Phil Bayfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.philio.ghost.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.philio.ghost.model.Blog;

/**
 * Utils for handling image downloads/saving/scaling
 * <p/>
 * Created by phil on 10/12/2014.
 */
public class ImageUtils {

    /**
     * Work out the URL of an image
     *
     * @param blog
     * @param path
     * @return
     */
    public static String getUrl(Blog blog, String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        } else if (path.startsWith("//")) {
            return "https:" + path;
        }
        return blog.url + path;
    }

    /**
     * Get filename for a given path in a blog
     *
     * @param context
     * @param blog
     * @param path
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getFilename(Context context, Blog blog, String path) throws
            UnsupportedEncodingException, NoSuchAlgorithmException {
        return getFilename(context, blog.getId(), path);
    }

    /**
     * Get filename for a given path in a blog
     *
     * @param context
     * @param blogId
     * @param path
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getFilename(Context context, long blogId, String path) throws
            UnsupportedEncodingException, NoSuchAlgorithmException {
        // Generate a filename
        return context.getFilesDir().getAbsolutePath() + "/content/" + Long.toString(blogId) +
                "/" + sha1(path) + ".png";
    }

    /**
     * Create a SHA1 to use as local filename
     *
     * @param path
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String sha1(String path) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(path.getBytes("UTF-8"));
        byte[] bytes = messageDigest.digest();
        return bytesToString(bytes);
    }

    /**
     * Convert bytes to a hex string
     *
     * @param bytes
     * @return
     */
    private static String bytesToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(Character.forDigit((aByte >> 4) & 0xF, 16));
            builder.append(Character.forDigit((aByte & 0xF), 16));
        }
        return builder.toString();
    }

    /**
     * Make sure that a directory exists
     *
     * @param path Directory path
     */
    public static boolean ensureDirectory(String path) {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            boolean success = directory.mkdirs();
            if (!success) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a file exists
     *
     * @param path
     * @return
     */
    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * Decode and scale the image
     *
     * @param inputStream Input stream
     * @param filename    Filename to save as
     * @param maxWidth    Maximum width
     * @param maxHeight   Maximum height
     */
    public static void decodeScale(InputStream inputStream, String filename, int maxWidth, int maxHeight) throws FileNotFoundException {
        // Decode stream to bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (bitmap == null) {
            return;
        }

        // Calculate new size
        int newWidth = bitmap.getWidth();
        int newHeight = bitmap.getHeight();
        if (newWidth > maxWidth) {
            newWidth = maxWidth;
            newHeight = (int) (bitmap.getHeight() / ((float) bitmap.getWidth() / newWidth));
        }
        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (int) (bitmap.getWidth() / ((float) bitmap.getHeight() / newHeight));
        }

        // Create matrix to scale
        Matrix matrix = new Matrix();
        matrix.postScale((float) newWidth / bitmap.getWidth(), (float) newHeight / bitmap.getHeight());

        // Create new image
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)
                .compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(filename));
        bitmap.recycle();
    }

}
