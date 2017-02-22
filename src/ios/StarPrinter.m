/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import "StarIO/SMPort.h"

#import "StarPrinter.h"

@implementation StarPrinter

- (void)getPrinters:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSArray* portArray = [SMPort searchPrinter];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsMultipart:portArray];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
