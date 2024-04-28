package com.example.ocrexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.interfaces.Detector;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executor;

public class ColorDetector implements Detector<ColorDetector.ImageData> {
    private static final int COLOR_PICKER_POINTS_COUNT = 250;
    private ImageData imageData;
    Task<ImageData> task = new Task<ImageData>() {
        @NonNull
        @Override
        public Task<ImageData> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
            return null;
        }

        @NonNull
        @Override
        public Task<ImageData> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
            return null;
        }

        @NonNull
        @Override
        public Task<ImageData> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
            return null;
        }

        @NonNull
        @Override
        public Task<ImageData> addOnSuccessListener(@NonNull OnSuccessListener<? super ImageData> onSuccessListener) {
            return null;
        }

        @NonNull
        @Override
        public Task<ImageData> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super ImageData> onSuccessListener) {
            return null;
        }

        @NonNull
        @Override
        public Task<ImageData> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super ImageData> onSuccessListener) {
            onSuccessListener.onSuccess(imageData);
            return this;
        }

        @NonNull
        @Override
        public Task<ImageData> addOnCompleteListener(@NonNull Executor var1, @NonNull OnCompleteListener<ImageData> var2) {
            var2.onComplete(this);
            return this;
        }

        @Nullable
        @Override
        public Exception getException() {
            return null;
        }

        @Override
        public ImageData getResult() {
            return imageData;
        }

        @Override
        public <X extends Throwable> ImageData getResult(@NonNull Class<X> aClass) throws X {
            return imageData;
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public boolean isComplete() {
            return true;
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }
    };

    @Override
    public int getDetectorType() {
        return 0;
    }

    @NonNull
    @Override
    public Task<ImageData> process(@NonNull Bitmap bitmap, int i) {
        return null;
    }

    @NonNull
    @Override
    public Task<ImageData> process(@NonNull Image image, int i) {
        return null;
    }

    @NonNull
    @Override
    public Task<ImageData> process(@NonNull Image image, int i, @NonNull Matrix matrix) {
        this.imageData = new ImageData(image, i, matrix);
        return task;
    }

    @NonNull
    @Override
    public Task<ImageData> process(@NonNull ByteBuffer byteBuffer, int i, int i1, int i2, int i3) {
        return null;
    }

    @Override
    public void close() {

    }

    public static class ImageData {
        byte[] yBuffer;
        byte[] uBuffer;
        byte[] vBuffer;
        int imageWidth;
        int imageHeight;
        int yRowStride;
        int yPixelStride;
        int uvRowStride;
        int uvPixelStride;

        int rotation;

        Matrix invMatrix;

        public ImageData(Image image, int rot, Matrix m) {
            this.rotation = rot;
            this.invMatrix = new Matrix();
            m.invert(invMatrix);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            image.getPlanes()[0].getBuffer().position(0);
            yBuffer = new byte[image.getPlanes()[0].getBuffer().remaining()];
            image.getPlanes()[0].getBuffer().get(yBuffer);

            image.getPlanes()[1].getBuffer().position(0);
            uBuffer = new byte[image.getPlanes()[1].getBuffer().remaining()];
            image.getPlanes()[1].getBuffer().get(uBuffer);

            image.getPlanes()[2].getBuffer().position(0);
            vBuffer = new byte[image.getPlanes()[2].getBuffer().remaining()];
            image.getPlanes()[2].getBuffer().get(vBuffer);
            yRowStride = image.getPlanes()[0].getRowStride();
            yPixelStride = image.getPlanes()[0].getPixelStride();
            uvRowStride = image.getPlanes()[1].getRowStride();
            uvPixelStride = image.getPlanes()[1].getPixelStride();
        }


        public int[][] getDominantColors(Rect rect) {
            int[][] pixelArray = new int[COLOR_PICKER_POINTS_COUNT][];
            for (int ii = 0; ii < COLOR_PICKER_POINTS_COUNT; ii++) {
                int xx = (int) ((rect.left - 20) + (Math.random() * ((rect.right - rect.left) + 40)));
                int yy = (int) ((rect.top - 20) + (Math.random() * ((rect.bottom - rect.top) + 40)));

                int px = getColor(xx, yy);
                int blue = px & 255;
                int green = (px >> 8) & 255;
                int red = (px >> 16) & 255;
                pixelArray[ii] = new int[]{red, green, blue};
            }

            int ch = 0;
            int maxChannelRange = 0;
            int avgChannel = 0;
            for (int chanel = 0; chanel < 3; chanel++) {
                int max = 0;
                int min = 255;
                int avg = 0;
                for (int i = 0; i < pixelArray.length; i++) {
                    if (pixelArray[i][chanel] > max) {
                        max = pixelArray[i][chanel];
                    }
                    if (pixelArray[i][chanel] < min) {
                        min = pixelArray[i][chanel];
                    }
                    avg = avg + pixelArray[i][chanel];
                }
                if (max - min > maxChannelRange) {
                    maxChannelRange = max - min;
                    ch = chanel;
                    avgChannel = avg / pixelArray.length;
                }
            }

            final int chch = ch;
            Arrays.sort(pixelArray, new Comparator<int[]>() {
                @Override
                public int compare(int[] ints, int[] t1) {
                    return ints[chch] - t1[chch];
                }
            });

            int[] primc = new int[]{0, 0, 0};
            int primcnt = 0;
            int[] secc = new int[]{0, 0, 0};
            int seccnt = 0;
            for (int i = 0; i < pixelArray.length; i++) {
                if (pixelArray[i][ch] < avgChannel) {
                    primc[0] = primc[0] + pixelArray[i][0];
                    primc[1] = primc[1] + pixelArray[i][1];
                    primc[2] = primc[2] + pixelArray[i][2];
                    primcnt++;
                } else {
                    secc[0] = secc[0] + pixelArray[i][0];
                    secc[1] = secc[1] + pixelArray[i][1];
                    secc[2] = secc[2] + pixelArray[i][2];
                    seccnt++;
                }
            }
            primc[0] = primc[0] / primcnt;
            primc[1] = primc[1] / primcnt;
            primc[2] = primc[2] / primcnt;
            secc[0] = secc[0] / seccnt;
            secc[1] = secc[1] / seccnt;
            secc[2] = secc[2] / seccnt;

            if (primcnt > seccnt) {
                return new int[][]{primc, secc};
            } else {
                return new int[][]{secc, primc};
            }
        }

        public int getColor(int x, int y) {
            float[] point = new float[]{x, y};
            invMatrix.mapPoints(point);

            if (rotation == 0) {
                x = (int) point[0];
                y = (int) point[1];
            } else if (rotation == 90) {
                y = imageHeight - (int) point[0];
                x = (int) point[1];
            } else if (rotation == 180) {
                x = imageWidth - (int) point[0];
                y = imageHeight - (int) point[1];

            } else if (rotation == 270) {
                y = (int) point[0];
                x = imageWidth - (int) point[1];
            }


            if (x >= imageWidth)
                x = imageWidth;
            if (y >= imageHeight)
                y = imageHeight - 1;
            if (x < 0)
                x = 0;
            if (y < 0)
                y = 0;
            int r, g, b;
            int yValue, uValue, vValue;
            int yIndex = (y * yRowStride) + (x * yPixelStride);
            // Y plane should have positive values belonging to [0...255]

            if (yIndex >= yBuffer.length) {
                yIndex = yBuffer.length - 1;
            }
            yValue = (yBuffer[yIndex] & 0xff);

            int uvx = x / 2;
            int uvy = y / 2;
            // U/V Values are subsampled i.e. each pixel in U/V chanel in a
            // YUV_420 image act as chroma value for 4 neighbouring pixels
            int uvIndex = (uvy * uvRowStride) + (uvx * uvPixelStride);

            // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
            // [0, 255] range they are scaled up and centered to 128.
            // Operation below brings U/V values to [-128, 127].
            uValue = (uBuffer[uvIndex] & 0xff) - 128;
            vValue = (vBuffer[uvIndex] & 0xff) - 128;

            // Compute RGB values.
            r = (int) (yValue + 1.370705f * vValue);
            g = (int) (yValue - (0.698001f * vValue) - (0.337633f * uValue));
            b = (int) (yValue + 1.732446f * uValue);
            if (r < 0)
                r = 0;
            if (r > 255)
                r = 255;
            if (g < 0)
                g = 0;
            if (g > 255)
                g = 255;
            if (b < 0)
                b = 0;
            if (b > 255)
                b = 255;

            // Use 255 for alpha value, no transparency. ARGB values are
            // positioned in each byte of a single 4 byte integer
            // [AAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB]

            return (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }
    }
}
