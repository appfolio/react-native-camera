#if __has_include(<React/RCTViewManager.h>)
#import <React/RCTViewManager.h>
#else
#import "RCTViewManager.h"
#endif

#import <AVFoundation/AVFoundation.h>

@class AECamera;

typedef NS_ENUM(NSInteger, AECameraAspect) {
  AECameraAspectFill = 0,
  AECameraAspectFit = 1,
  AECameraAspectStretch = 2
};

typedef NS_ENUM(NSInteger, AECameraCaptureSessionPreset) {
  AECameraCaptureSessionPresetLow = 0,
  AECameraCaptureSessionPresetMedium = 1,
  AECameraCaptureSessionPresetHigh = 2,
  AECameraCaptureSessionPresetPhoto = 3,
  AECameraCaptureSessionPreset480p = 4,
  AECameraCaptureSessionPreset720p = 5,
  AECameraCaptureSessionPreset1080p = 6
};

typedef NS_ENUM(NSInteger, AECameraCaptureMode) {
  AECameraCaptureModeStill = 0,
  AECameraCaptureModeVideo = 1
};

typedef NS_ENUM(NSInteger, AECameraCaptureTarget) {
  AECameraCaptureTargetMemory = 0,
  AECameraCaptureTargetDisk = 1,
  AECameraCaptureTargetTemp = 2,
  AECameraCaptureTargetCameraRoll = 3
};

typedef NS_ENUM(NSInteger, AECameraOrientation) {
  AECameraOrientationAuto = 0,
  AECameraOrientationLandscapeLeft = AVCaptureVideoOrientationLandscapeLeft,
  AECameraOrientationLandscapeRight = AVCaptureVideoOrientationLandscapeRight,
  AECameraOrientationPortrait = AVCaptureVideoOrientationPortrait,
  AECameraOrientationPortraitUpsideDown = AVCaptureVideoOrientationPortraitUpsideDown
};

typedef NS_ENUM(NSInteger, AECameraType) {
  AECameraTypeFront = AVCaptureDevicePositionFront,
  AECameraTypeBack = AVCaptureDevicePositionBack
};

typedef NS_ENUM(NSInteger, AECameraFlashMode) {
  AECameraFlashModeOff = AVCaptureFlashModeOff,
  AECameraFlashModeOn = AVCaptureFlashModeOn,
  AECameraFlashModeAuto = AVCaptureFlashModeAuto
};

typedef NS_ENUM(NSInteger, AECameraTorchMode) {
  AECameraTorchModeOff = AVCaptureTorchModeOff,
  AECameraTorchModeOn = AVCaptureTorchModeOn,
  AECameraTorchModeAuto = AVCaptureTorchModeAuto
};

@interface AECameraManager : RCTViewManager<AVCaptureMetadataOutputObjectsDelegate, AVCaptureFileOutputRecordingDelegate>

@property (nonatomic, strong) dispatch_queue_t sessionQueue;
@property (nonatomic, strong) AVCaptureSession *session;
@property (nonatomic, strong) AVCaptureDeviceInput *audioCaptureDeviceInput;
@property (nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property (nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic, strong) AVCaptureMovieFileOutput *movieFileOutput;
@property (nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic, strong) id runtimeErrorHandlingObserver;
@property (nonatomic, assign) NSInteger presetCamera;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, assign) NSInteger videoTarget;
@property (nonatomic, assign) NSInteger orientation;
@property (nonatomic, assign) BOOL mirrorImage;
@property (nonatomic, strong) NSArray* barCodeTypes;
@property (nonatomic, strong) RCTPromiseResolveBlock videoResolve;
@property (nonatomic, strong) RCTPromiseRejectBlock videoReject;
@property (nonatomic, strong) AECamera *camera;


- (void)changeOrientation:(NSInteger)orientation;
- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position;
- (void)capture:(NSDictionary*)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)getFOV:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)hasFlash:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)initializeCaptureSessionInput:(NSString*)type;
- (void)stopCapture;
- (void)startSession;
- (void)stopSession;
- (void)focusAtThePoint:(CGPoint) atPoint;
- (void)zoom:(CGFloat)velocity reactTag:(NSNumber *)reactTag;


@end
