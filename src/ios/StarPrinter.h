/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <StarIIO/SMPort.h>


@interface StarPrinter : CDVPlugin {
  // Member variables go here.
}

- (void)getPrinters:(CDVInvokedUrlCommand*)command;
@end

