package com.lwansbrough.RCTCamera;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.*;
import com.facebook.react.uimanager.annotations.ReactProp;

import com.google.android.cameraview.CameraView;
import com.google.android.cameraview.Size;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;

public class RCTCameraViewManager extends ViewGroupManager<CameraView> implements LifecycleEventListener {
    private static final String REACT_CLASS = "RCTCamera";

    private static final Size RESOLUTION_480P = new Size(853, 480); // 480p shoots for a 16:9 HD aspect ratio, but can otherwise fall back/down to any other supported camera sizes, such as 800x480 or 720x480, if (any) present. See getSupportedPictureSizes/getSupportedVideoSizes below.
    private static final Size RESOLUTION_720P = new Size(1280, 720);
    private static final Size RESOLUTION_1080P = new Size(1920, 1080);

    private static CameraView theCameraView;
    private static RCTCameraViewManager instance;
    private static boolean cameraIsOpening = false;

    public static CameraView getCameraView() {
        return theCameraView;
    }

    public static RCTCameraViewManager getInstance() {
        return instance;
    }

    public RCTCameraViewManager() {
        instance = this;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public CameraView createViewInstance(ThemedReactContext context) {
        context.addLifecycleEventListener(this);
        theCameraView = new CameraView(context);
        cameraIsOpening = true;
        theCameraView.addCallback(new CameraView.Callback() {
            @Override
            public void onCameraOpened(CameraView cameraView) {
                super.onCameraOpened(cameraView);
                cameraIsOpening = false;
            }
        });
        theCameraView.start();
        return theCameraView;
    }

    @Override
    public void onDropViewInstance(CameraView view) {
        super.onDropViewInstance(view);
        ((ThemedReactContext) view.getContext()).removeLifecycleEventListener(this);
        view.stop();
        cameraIsOpening = false;
    }

    @Override
    public void onHostResume() {
        if (theCameraView != null && ! theCameraView.isCameraOpened() && ! cameraIsOpening) {
            theCameraView.start();
        }
    }

    @Override
    public void onHostPause() {
        if (theCameraView != null && theCameraView.isCameraOpened()) {
            theCameraView.stop();
        }
    }

    @Override
    public void onHostDestroy() {
        if (theCameraView != null && theCameraView.isCameraOpened()) {
            theCameraView.stop();
        }
    }

    @ReactProp(name = "aspect")
    public void setAspect(CameraView view, int aspect) {
//        view.setAspect(aspect);
    }

    @ReactProp(name = "captureMode")
    public void setCaptureMode(CameraView view, final int captureMode) {
        // Note that this in practice only performs any additional setup necessary for each mode;
        // the actual indication to capture a still or record a video when capture() is called is
        // still ultimately decided upon by what it in the options sent to capture().
//        view.setCaptureMode(captureMode);
    }

    @ReactProp(name = "captureTarget")
    public void setCaptureTarget(CameraView view, int captureTarget) {
        // No reason to handle this props value here since it's passed again to the RCTCameraModule capture method
    }

    @ReactProp(name = "type")
    public void setType(CameraView view, int type) {
//        view.setCameraType(type);
    }

    @ReactProp(name = "captureQuality")
    public void setCaptureQuality(CameraView view, String captureQuality) {
        if (theCameraView == null) {
            return;
        }

        Size pictureSize = null;
        SortedSet<Size> supportedSizes = theCameraView.getSupportedPictureSizes();
        switch (captureQuality) {
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_LOW:
                pictureSize = supportedSizes.first();
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_MEDIUM:
                // TODO should be middle
                pictureSize = supportedSizes.first();
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_HIGH:
                pictureSize = supportedSizes.last();
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_PREVIEW:
                Size optimalPreviewSize = getBestSize(supportedSizes, new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
                pictureSize = getClosestSize(supportedSizes, optimalPreviewSize);
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_480P:
                pictureSize = getBestSize(supportedSizes, RESOLUTION_480P);
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_720P:
                pictureSize = getBestSize(supportedSizes, RESOLUTION_720P);
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_1080P:
                pictureSize = getBestSize(supportedSizes, RESOLUTION_1080P);
                break;
        }

        if (pictureSize != null) {
            theCameraView.setPictureSize(pictureSize);
        }
    }

    @ReactProp(name = "torchMode")
    public void setTorchMode(CameraView view, int torchMode) {
//        view.setTorchMode(torchMode);
    }

    @ReactProp(name = "flashMode")
    public void setFlashMode(CameraView view, int flashMode) {
//        view.setFlashMode(flashMode);
    }

    @ReactProp(name = "orientation")
    public void setOrientation(CameraView view, int orientation) {
//        view.setOrientation(orientation);
    }

    @ReactProp(name = "captureAudio")
    public void setCaptureAudio(CameraView view, boolean captureAudio) {
        // TODO - implement video mode
    }

    @ReactProp(name = "barcodeScannerEnabled")
    public void setBarcodeScannerEnabled(CameraView view, boolean barcodeScannerEnabled) {
//        view.setBarcodeScannerEnabled(barcodeScannerEnabled);
    }

    @ReactProp(name = "barCodeTypes")
    public void setBarCodeTypes(CameraView view, ReadableArray barCodeTypes) {
        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
//        view.setBarCodeTypes(result);
    }

    public Size getBestSize(SortedSet<Size> supportedSizes, Size desiredSize) {
        int minimumDesiredArea = desiredSize.getWidth() * desiredSize.getHeight();
        for (Size size : supportedSizes) {
            int area = size.getWidth() * size.getHeight();
            if (area >= minimumDesiredArea) {
                return size;
            }
        }

        return supportedSizes.last();
    }

    private Size getSmallestSize(SortedSet<Size> supportedSizes) {
        Size smallestSize = null;
        for (Size size : supportedSizes) {
            if (smallestSize == null) {
                smallestSize = size;
                continue;
            }

            int resultArea = smallestSize.getWidth() * smallestSize.getHeight();
            int newArea = size.getWidth() * size.getHeight();

            if (newArea < resultArea) {
                smallestSize = size;
            }
        }

        return smallestSize;
    }

    private Size getClosestSize(SortedSet<Size> supportedSizes, Size matchSize) {
        Size closestSize = null;
        for (Size size : supportedSizes) {
            if (closestSize == null) {
                closestSize = size;
                continue;
            }

            int currentDelta = Math.abs(closestSize.getWidth() - matchSize.getWidth()) * Math.abs(closestSize.getHeight() - matchSize.getHeight());
            int newDelta = Math.abs(size.getWidth() - matchSize.getWidth()) * Math.abs(size.getHeight() - matchSize.getHeight());

            if (newDelta < currentDelta) {
                closestSize = size;
            }
        }
        return closestSize;
    }
}
