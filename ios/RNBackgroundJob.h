
#if __has_include("RCTBridgeModule.h")
  #import "RCTBridgeModule.h"
#else
  #import <React/RCTBridgeModule.h>
#endif

@interface RNBackgroundJob : NSObject <RCTBridgeModule>

@end
