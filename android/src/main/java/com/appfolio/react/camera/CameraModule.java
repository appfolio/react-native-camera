/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 * Android video recording support by Marc Johnson (me@marc.mn) 4/2016
 */

package com.appfolio.react.camera;

import android.media.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.cameraview.CameraView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class CameraModule extends ReactContextBaseJavaModule {
    private static final String TAG = "AECameraModule";

    public static final int AE_CAMERA_ASPECT_FILL = 0;
    public static final int AE_CAMERA_ASPECT_FIT = 1;
    public static final int AE_CAMERA_ASPECT_STRETCH = 2;
    public static final int AE_CAMERA_CAPTURE_MODE_STILL = 0;
    public static final int AE_CAMERA_CAPTURE_MODE_VIDEO = 1;
    public static final int AE_CAMERA_CAPTURE_TARGET_MEMORY = 0;
    public static final int AE_CAMERA_CAPTURE_TARGET_DISK = 1;
    public static final int AE_CAMERA_CAPTURE_TARGET_CAMERA_ROLL = 2;
    public static final int AE_CAMERA_CAPTURE_TARGET_TEMP = 3;
    public static final int AE_CAMERA_ORIENTATION_AUTO = Integer.MAX_VALUE;
    public static final int AE_CAMERA_ORIENTATION_PORTRAIT = Surface.ROTATION_0;
    public static final int AE_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN = Surface.ROTATION_180;
    public static final int AE_CAMERA_ORIENTATION_LANDSCAPE_LEFT = Surface.ROTATION_90;
    public static final int AE_CAMERA_ORIENTATION_LANDSCAPE_RIGHT = Surface.ROTATION_270;
    public static final int AE_CAMERA_TYPE_FRONT = 1;
    public static final int AE_CAMERA_TYPE_BACK = 2;
    public static final int AE_CAMERA_FLASH_MODE_OFF = 0;
    public static final int AE_CAMERA_FLASH_MODE_ON = 1;
    public static final int AE_CAMERA_FLASH_MODE_AUTO = 2;
    public static final int AE_CAMERA_TORCH_MODE_OFF = 0;
    public static final int AE_CAMERA_TORCH_MODE_ON = 1;
    public static final int AE_CAMERA_TORCH_MODE_AUTO = 2;
    public static final String AE_CAMERA_CAPTURE_QUALITY_PREVIEW = "preview";
    public static final String AE_CAMERA_CAPTURE_QUALITY_HIGH = "high";
    public static final String AE_CAMERA_CAPTURE_QUALITY_MEDIUM = "medium";
    public static final String AE_CAMERA_CAPTURE_QUALITY_LOW = "low";
    public static final String AE_CAMERA_CAPTURE_QUALITY_1080P = "1080p";
    public static final String AE_CAMERA_CAPTURE_QUALITY_720P = "720p";
    public static final String AE_CAMERA_CAPTURE_QUALITY_480P = "480p";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static ReactApplicationContext _reactContext;

    private Boolean mSafeToCapture = true;

    public CameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
    }

    public static ReactApplicationContext getReactContextSingleton() {
        return _reactContext;
    }

    @Override
    public String getName() {
        return "AECameraModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("Aspect", getAspectConstants());
                put("BarCodeType", getBarCodeConstants());
                put("Type", getTypeConstants());
                put("CaptureQuality", getCaptureQualityConstants());
                put("CaptureMode", getCaptureModeConstants());
                put("CaptureTarget", getCaptureTargetConstants());
                put("Orientation", getOrientationConstants());
                put("FlashMode", getFlashModeConstants());
                put("TorchMode", getTorchModeConstants());
            }

            private Map<String, Object> getAspectConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("stretch", AE_CAMERA_ASPECT_STRETCH);
                        put("fit", AE_CAMERA_ASPECT_FIT);
                        put("fill", AE_CAMERA_ASPECT_FILL);
                    }
                });
            }

            private Map<String, Object> getBarCodeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        // @TODO add barcode types
                    }
                });
            }

            private Map<String, Object> getTypeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("front", AE_CAMERA_TYPE_FRONT);
                        put("back", AE_CAMERA_TYPE_BACK);
                    }
                });
            }

            private Map<String, Object> getCaptureQualityConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("low", AE_CAMERA_CAPTURE_QUALITY_LOW);
                        put("medium", AE_CAMERA_CAPTURE_QUALITY_MEDIUM);
                        put("high", AE_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("photo", AE_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("preview", AE_CAMERA_CAPTURE_QUALITY_PREVIEW);
                        put("480p", AE_CAMERA_CAPTURE_QUALITY_480P);
                        put("720p", AE_CAMERA_CAPTURE_QUALITY_720P);
                        put("1080p", AE_CAMERA_CAPTURE_QUALITY_1080P);
                    }
                });
            }

            private Map<String, Object> getCaptureModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("still", AE_CAMERA_CAPTURE_MODE_STILL);
                        put("video", AE_CAMERA_CAPTURE_MODE_VIDEO);
                    }
                });
            }

            private Map<String, Object> getCaptureTargetConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("memory", AE_CAMERA_CAPTURE_TARGET_MEMORY);
                        put("disk", AE_CAMERA_CAPTURE_TARGET_DISK);
                        put("cameraRoll", AE_CAMERA_CAPTURE_TARGET_CAMERA_ROLL);
                        put("temp", AE_CAMERA_CAPTURE_TARGET_TEMP);
                    }
                });
            }

            private Map<String, Object> getOrientationConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("auto", AE_CAMERA_ORIENTATION_AUTO);
                        put("landscapeLeft", AE_CAMERA_ORIENTATION_LANDSCAPE_LEFT);
                        put("landscapeRight", AE_CAMERA_ORIENTATION_LANDSCAPE_RIGHT);
                        put("portrait", AE_CAMERA_ORIENTATION_PORTRAIT);
                        put("portraitUpsideDown", AE_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN);
                    }
                });
            }

            private Map<String, Object> getFlashModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", AE_CAMERA_FLASH_MODE_OFF);
                        put("on", AE_CAMERA_FLASH_MODE_ON);
                        put("auto", AE_CAMERA_FLASH_MODE_AUTO);
                    }
                });
            }

            private Map<String, Object> getTorchModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", AE_CAMERA_TORCH_MODE_OFF);
                        put("on", AE_CAMERA_TORCH_MODE_ON);
                        put("auto", AE_CAMERA_TORCH_MODE_AUTO);
                    }
                });
            }
        });
    }

    public static byte[] convertFileToByteArray(File f)
    {
        byte[] byteArray = null;
        try
        {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }

    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) {
        CameraView cameraView = CameraViewManager.getCameraView();
        if (null == cameraView) {
            promise.reject("No camera found.");
            return;
        }

        if (options.hasKey("playSoundOnCapture") && options.getBoolean("playSoundOnCapture")) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }

        if (options.hasKey("quality")) {
            CameraViewManager.getInstance().setCaptureQuality(cameraView, options.getString("quality"));
        }

        cameraView.addCallback(new CameraView.Callback() {
            @Override
            public void onPictureTaken(CameraView cameraView, final byte[] data) {
                cameraView.removeCallback(this);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        processImage(new MutableImage(data), options, promise);
                    }
                });

                mSafeToCapture = true;
            }
        });

        if (mSafeToCapture) {
          try {
              cameraView.takePicture();
              mSafeToCapture = false;
          } catch(RuntimeException ex) {
              Log.e(TAG, "Couldn't capture photo.", ex);
          }
        }
    }

    /**
     * synchronized in order to prevent the user crashing the app by taking many photos and them all being processed
     * concurrently which would blow the memory (esp on smaller devices), and slow things down.
     */
    private synchronized void processImage(MutableImage mutableImage, ReadableMap options, Promise promise) {
        try {
            mutableImage.fixOrientation();
        } catch (MutableImage.ImageMutationFailedException e) {
            promise.reject("Error mirroring image", e);
            return;
        }

        boolean shouldMirror = options.hasKey("mirrorImage") && options.getBoolean("mirrorImage");
        if (shouldMirror) {
            try {
                mutableImage.mirrorImage();
            } catch (MutableImage.ImageMutationFailedException e) {
                promise.reject("Error mirroring image", e);
                return;
            }
        }

        int jpegQualityPercent = 80;
        if (options.hasKey("jpegQuality")) {
            jpegQualityPercent = options.getInt("jpegQuality");
        }

        switch (options.getInt("target")) {
            case AE_CAMERA_CAPTURE_TARGET_MEMORY:
                String encoded = mutableImage.toBase64(jpegQualityPercent);
                WritableMap response = new WritableNativeMap();
                response.putString("data", encoded);
                promise.resolve(response);
                break;
            case AE_CAMERA_CAPTURE_TARGET_CAMERA_ROLL: {
                File cameraRollFile = getOutputCameraRollFile(MEDIA_TYPE_IMAGE);
                if (cameraRollFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(cameraRollFile, options, jpegQualityPercent);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                addToMediaStore(cameraRollFile.getAbsolutePath());

                resolve(cameraRollFile, promise);

                break;
            }
            case AE_CAMERA_CAPTURE_TARGET_DISK: {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(pictureFile, options, 85);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                resolve(pictureFile, promise);

                break;
            }
            case AE_CAMERA_CAPTURE_TARGET_TEMP: {
                File tempFile = getTempMediaFile(MEDIA_TYPE_IMAGE);
                if (tempFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(tempFile, options, 85);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                resolve(tempFile, promise);

                break;
            }
        }
    }

    private File getOutputMediaFile(int type) {
        // Get environment directory type id from requested media type.
        String environmentDirectoryType;
        if (type == MEDIA_TYPE_IMAGE) {
            environmentDirectoryType = Environment.DIRECTORY_PICTURES;
        } else if (type == MEDIA_TYPE_VIDEO) {
            environmentDirectoryType = Environment.DIRECTORY_MOVIES;
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(environmentDirectoryType)
        );
    }

    private File getOutputCameraRollFile(int type) {
        return getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        );
    }

    private File getOutputFile(int type, File storageDir) {
        // Create the storage directory if it does not exist
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory:" + storageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        String fileName = String.format("%s", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        if (type == MEDIA_TYPE_IMAGE) {
            fileName = String.format("IMG_%s.jpg", fileName);
        } else if (type == MEDIA_TYPE_VIDEO) {
            fileName = String.format("VID_%s.mp4", fileName);
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return new File(String.format("%s%s%s", storageDir.getPath(), File.separator, fileName));
    }

    private File getTempMediaFile(int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputDir = _reactContext.getCacheDir();
            File outputFile;

            if (type == MEDIA_TYPE_IMAGE) {
                outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
            } else if (type == MEDIA_TYPE_VIDEO) {
                outputFile = File.createTempFile("VID_" + timeStamp, ".mp4", outputDir);
            } else {
                Log.e(TAG, "Unsupported media type:" + type);
                return null;
            }
            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private void addToMediaStore(String path) {
        MediaScannerConnection.scanFile(_reactContext, new String[] { path }, null, null);
    }

    private void resolve(final File imageFile, final Promise promise) {
        final WritableMap response = new WritableNativeMap();
        response.putString("path", Uri.fromFile(imageFile).toString());

        // borrowed from react-native CameraRollManager, it finds and returns the 'internal'
        // representation of the image uri that was just saved.
        // e.g. content://media/external/images/media/123
        MediaScannerConnection.scanFile(
                _reactContext,
                new String[]{imageFile.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (uri != null) {
                            response.putString("mediaUri", uri.toString());
                        }

                        promise.resolve(response);
                    }
                });
    }

}
