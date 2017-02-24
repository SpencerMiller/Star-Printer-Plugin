/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import "StarIO/SMPort.h"

#import "StarPrinter.h"

@implementation StarPrinter

- (void)findDevices:(CDVInvokedUrlCommand*)command
{
  // Wrap method loic in a delegate to seperate thread
  [self.commandDelegate runInBackground:^{
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
        
        NSString *printerJSON = [NSString stringWithFormat:@"{ \"mac\":\"%@\", \"model\":\"%@\", \"port\":\"%@\" }", mac, model, port];
        
        [jsonArray addObject:printerJSON];
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsMultipart:jsonArray];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void) print:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult *pluginResult = nil;

    NSString *device = [command.arguments objectAtIndex:0];
    unsigned char printCommands = [command.arguments objectAtIndex:1];
    int numberOfBytes = sizeof(printCommands);

    uint bytesWritten = 0;
    SMPort *port = nil;
    NSError *error = NULL;
    BOOL isMacAddress = NO;
    BOOL isPort = NO;
    
    NSRegularExpression *regexMac = [NSRegularExpression regularExpressionWithPattern:@"^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$" options:NSRegularExpressionCaseInsensitive error:&error];
    NSUInteger numberOfMatches = [regexMac numberOfMatchesInString:device options:0 range:NSMakeRange(0, [device length])];
    if (numberOfMatches > 0) 
    {
        isMacAddress = YES;
        NSLog(@"Is Mac Address:  %d", isMacAddress);
    }
    
    NSRegularExpression *regexPort = [NSRegularExpression regularExpressionWithPattern:@"BT:.*" options:NSRegularExpressionCaseInsensitive error:&error];
    numberOfMatches = [regexPort numberOfMatchesInString:device options:0 range:NSMakeRange(0, [device length])];
    if (numberOfMatches > 0) 
    {
        isPort = YES;
        NSLog(@"Is Port:  %d", isPort);
    }
    
    if (!isMacAddress || !isPort) 
    {
        NSString *message = @"Invalid device:  ";
        NSLog(@"%@ %@.", message, device);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    }
    
    // IOS 6 devices use the MAC Address to obtain the port.  Though it appears all printing will work via MAC address
    if (isMacAddress)
    {
        NSString *portWithMAC = [NSString stringWithFormat:@"BT:%@", device];
        @try
        {
            port = [SMPort getPort:portWithMAC :@"" :1000];
        }
        @catch (NSException *exception) 
        {
            NSLog(@"Error with printer port - %@.", exception.description);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    }
    else if (isPort)
    {
        @try
        {
            port = [SMPort getPort:device :@"" :1000];
        }
        @catch (NSException *exception) 
        {
            NSLog(@"Error with printer port - %@.", exception.description);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    }

    // Print
    @try
    {
        while (bytesWritten < numberOfBytes)
        {
            bytesWritten += [port writePort:printCommands :bytesWritten : numberOfBytes - bytesWritten];
        }
        
    }
    @catch (NSException *exception) 
    {
        NSLog(@"Error with printer port - %@.", exception.description);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
    }
    @finally
    {
        [SMPort releasePort:port];
         pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }
}

@end
