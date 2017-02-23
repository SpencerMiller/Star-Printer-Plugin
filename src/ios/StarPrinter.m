/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import "StarIO/SMPort.h"

#import "StarPrinter.h"

@implementation StarPrinter

- (void)findDevices:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Inside findDevices()");
    CDVPluginResult *pluginResult = nil;
    
        NSArray *portArray = [SMPort searchPrinter];
    
    NSMutableArray *jsonArray = [[NSMutableArray alloc]init];
    
    for (int i = 0; i < portArray.count; i++)
    {
        PortInfo *portInfo = [portArray objectAtIndex:i];
        NSString *mac = portInfo.macAddress;
        NSString *model = portInfo.modelName;
        NSString *port = portInfo.portName;
        
        NSLog(@"MAC Address:  %@", mac);
        NSLog(@"Printer Name:  %@", model);
        NSLog(@"Port Name:  %@", port);
        
        NSDictionary *printer = @{
                                  @"mac" : mac,
                                  @"model" : model,
                                  @"ort" : port,
                                  };
        
        [jsonArray addObject:printer];
        
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsMultipart:jsonArray];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
