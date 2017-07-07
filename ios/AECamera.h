#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"

@class AECameraManager;

@interface AECamera : UIView

- (id)initWithManager:(AECameraManager *)manager bridge:(RCTBridge *)bridge;

@end
