/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import <StarIO/SMPort.h>

@interface StarPrinter : CDVPlugin {
  // Member variables go here.
}

- (void)getPrinters:(CDVInvokedUrlCommand*)command;
@end

@implementation StarPrinter

- (void)getPrinters:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSArray* portArray = [SMPort searchPrinter];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsMultipart:portArray];
    
    [self.commandDelegate sendPluginREsult:pluginResult callbackId:command.callbackId];
}

@end
