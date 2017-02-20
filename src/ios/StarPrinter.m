/********* star-printer-plugin.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import <StarIO/SMPort.h>

@interface star-printer-plugin : CDVPlugin {
  // Member variables go here.
}

- (void)getPrinters:(CDVInvokedUrlCommand*)command;
@end

@implementation star-printer-plugin

- (void)getPrinters:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSArray* portArray = [SMPort searchPrinter];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsMultipart:portArray];
    
    [self.commandDelegate sendPluginREsult:pluginResult callbackId:command.callbackId];
}

@end
