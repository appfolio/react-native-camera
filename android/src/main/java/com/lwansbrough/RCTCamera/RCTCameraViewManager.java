package com.lwansbrough.RCTCamera;

import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.*;
import com.facebook.react.uimanager.annotations.ReactProp;

import com.google.android.cameraview.CameraView;
import com.google.android.cameraview.Size;

import java.util.ArrayDeque;
import java.util.SortedSet;

public class RCTCameraViewManager extends ViewGroupManager<CameraView> implements LifecycleEventListener {
    private static final String REACT_CLASS = "RCTCamera";

    private static final Size RESOLUTION_480P = new Size(853, 480); // 480p shoots for a 16:9 HD aspect ratio, but can otherwise fall back/down to any other supported camera sizes, such as 800x480 or 720x480, if (any) present. See getSupportedPictureSizes/getSupportedVideoSizes below.
    private static final Size RESOLUTION_720P = new Size(1280, 720);
    private static final Size RESOLUTION_1080P = new Size(1920, 1080);

    private static CameraView theCameraView;
    private static RCTCameraViewManager instance;
    private static boolean cameraIsOpening = false;
    private static int lastFlashMode = CameraView.FLASH_AUTO;

    public static CameraView getCameraView() {
        return theCameraView;
    }

    public static RCTCameraViewManager getInstance() {
        return instance;
    }

    private static ArrayDeque<Runnable> onCameraOpenedCallbacks = new ArrayDeque<Runnable>();
    private static boolean cameraRequiresRestart = false;

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
                if (cameraRequiresRestart) {
                    onCameraOpenedCallbacks.addFirst(new OnCameraOpenedCallback(cameraView) {
                        public void run() {
                            this.getCameraView().stop();
                        }
                    });
                    onCameraOpenedCallbacks.addLast(new OnCameraOpenedCallback(cameraView) {
                        public void run() {
                            this.getCameraView().start();
                        }
                    });
                    cameraRequiresRestart = false;
                }

                for (Runnable runnable : onCameraOpenedCallbacks) {
                    runnable.run();
                }
                onCameraOpenedCallbacks.clear();
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

    @ReactProp(name = "type")
    public void setType(CameraView view, int type) {
        final int facing;
        if (type == RCTCameraModule.RCT_CAMERA_TYPE_FRONT) {
            facing = CameraView.FACING_FRONT;
        } else if (type == RCTCameraModule.RCT_CAMERA_TYPE_BACK) {
            facing = CameraView.FACING_BACK;
        } else {
            throw new JSApplicationIllegalArgumentException("Invalid camera type: " + type);
        }
        addOnCameraOpenedCallback(new OnCameraOpenedCallback(view) {
            public void run() {
                CameraView cameraView = this.getCameraView();
                cameraView.setFacing(facing);
            }
        }, true);
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
                pictureSize = supportedSizes.toArray(new Size[0])[supportedSizes.size() / 2];
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
            final Size thePictureSize = pictureSize;
            addOnCameraOpenedCallback(new OnCameraOpenedCallback(theCameraView) {
                public void run() {
                    this.getCameraView().setPictureSize(thePictureSize);
                }
            });
        }
    }

    @ReactProp(name = "torchMode")
    public void setTorchMode(CameraView view, int mode) {
        final int flashMode;
        switch (mode) {
            case RCTCameraModule.RCT_CAMERA_TORCH_MODE_ON:
                flashMode = CameraView.FLASH_TORCH;
                break;
            case RCTCameraModule.RCT_CAMERA_TORCH_MODE_OFF:
            case RCTCameraModule.RCT_CAMERA_TORCH_MODE_AUTO:
                flashMode = lastFlashMode;
                break;
            default:
                flashMode = CameraView.FLASH_AUTO;
                break;
        }

        addOnCameraOpenedCallback(new OnCameraOpenedCallback(view) {
            public void run() {
                this.getCameraView().setFlash(flashMode);
            }
        }, false);
    }

    @ReactProp(name = "flashMode")
    public void setFlashMode(CameraView view, int mode) {
        final int flashMode;
        switch (mode) {
            case RCTCameraModule.RCT_CAMERA_FLASH_MODE_AUTO:
                flashMode = CameraView.FLASH_AUTO;
                break;
            case RCTCameraModule.RCT_CAMERA_FLASH_MODE_OFF:
                flashMode = CameraView.FLASH_OFF;
                break;
            case RCTCameraModule.RCT_CAMERA_FLASH_MODE_ON:
                flashMode = CameraView.FLASH_ON;
                break;
            default:
                flashMode = CameraView.FLASH_AUTO;
                break;
        }
        lastFlashMode = mode;

        addOnCameraOpenedCallback(new OnCameraOpenedCallback(view) {
            public void run() {
                this.getCameraView().setFlash(flashMode);
            }
        }, false);
    }

    private Size getBestSize(SortedSet<Size> supportedSizes, Size desiredSize) {
        int minimumDesiredArea = desiredSize.getWidth() * desiredSize.getHeight();
        for (Size size : supportedSizes) {
            int area = size.getWidth() * size.getHeight();
            if (area >= minimumDesiredArea) {
                return size;
            }
        }

        return supportedSizes.last();
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

    private abstract class OnCameraOpenedCallback implements Runnable {
        private CameraView mCameraView;

        public OnCameraOpenedCallback(CameraView cameraView) {
            this.mCameraView = cameraView;
        }

        public CameraView getCameraView() {
            return mCameraView;
        }

        abstract public void run();
    }

    private void addOnCameraOpenedCallback(OnCameraOpenedCallback callback) {
        addOnCameraOpenedCallback(callback, false);
    }

    private void addOnCameraOpenedCallback(OnCameraOpenedCallback callback, boolean requireRestart) {
        if (cameraIsOpening) {
            if (requireRestart) {
                cameraRequiresRestart = true;
            }
            onCameraOpenedCallbacks.addLast(callback);
        } else {
            callback.run();
        }
    }
}
