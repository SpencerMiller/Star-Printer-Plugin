/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import "StarIO/SMPort.h"

#import "StarPrinter.h"

@implementation StarPrinter

- (void)getPrinters:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Inside getPrinters()");
    CDVPluginResult *pluginResult = nil;
    
    NSArray *portArray = [SMPort searchPrinter];

    // Purly test logic to see if cordova is communicating with the method
    PortInfo *port = [portArray objectAtIndex:0];
    NSString *name = port.portName;
    NSString *address = port.macAddress;
    NSString *model = port.modelName;
    
    NSLog(@"Printer Name:  %@", model);
    NSLog(@"Port Name:  %@", name);
    NSLog(@"MAC Address:  %@", address);
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsMultipart:portArray];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
