/********* StarPrinter.m Cordova Plugin Implementation *******/

#import <Foundation/Foundation.h>

#import <Cordova/CDV.h>
#import "StarIO/SMPort.h"

#import "StarPrinter.h"

@implementation StarPrinter

- (void)findDevices:(CDVInvokedUrlCommand*)command
{
    // Wrap method logic in a delegate to spin off to seperate thread
    [self.commandDelegate runInBackground:^{
        CDVPluginResult *pluginResult = nil;
        
        NSArray *portArray = [SMPort searchPrinter];
        
        NSMutableString *jsonArray = [[NSMutableString alloc]init];
        
        if (portArray.count > 0)
        {
            for (int i = 0; i < portArray.count; i++)
            {
                PortInfo *portInfo = [portArray objectAtIndex:i];
                NSString *mac = portInfo.macAddress;
                NSString *model = portInfo.modelName;
                NSString *port = portInfo.portName;
            
                NSLog(@"MAC Address:  %@", mac);
                NSLog(@"Printer Name:  %@", model);
                NSLog(@"Port Name:  %@", port);
            
                if (i == 0)
                {
                    NSString *printerJSON = [NSString stringWithFormat:@"[{\"name\":\"%@\",\"mac\":\"%@\"}", model, port];
                    [jsonArray appendString:printerJSON];
                }
                else
                {
                    NSString *printerJSON = [NSString stringWithFormat:@",{\"name\":\"%@\",\"mac\":\"%@\"}", model, port];
                    [jsonArray appendString:printerJSON];
                }
            }
        
            NSString *jsonArrayEndChar = @"]";
            [jsonArray appendString:jsonArrayEndChar];
        }
        
        NSLog(@"Printer JSON:  %@", jsonArray);
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:jsonArray];
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) print:(CDVInvokedUrlCommand*)command
{
    // Run print command in a seperate thread
    [self.commandDelegate runInBackground:^{
        CDVPluginResult *pluginResult = nil;

        NSString *device = [command.arguments objectAtIndex:0];
        NSArray *printArray = [command.arguments objectAtIndex:1];

         /* Star printer needs 3 additional characters appended to the end of a print array to print correctly. */
        int length = (int)[printArray count] + 3;
        
        uint8_t printCommand[length];
        
        int index = 0;
        for (int i = 0; i < length - 3; i++)
        {
            printCommand[i] = [[printArray objectAtIndex:i] intValue];
            
            index ++;
        }
        
        // Ending print commands
        printCommand[index++] = 0x1B;
        printCommand[index++] = 0x64;
        printCommand[index] = 0x02;

        uint bytesWritten = 0;
        SMPort *port = nil;
        NSError *error = NULL;
        BOOL isMacAddress = NO;
        BOOL isPort = NO;
    
        // Port can be obtained with either a Mac Address or Port name.  The following logic determines which was passed in
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
    
        if (!isMacAddress && !isPort)
        {
            NSString *message = @"Invalid device:  ";
            NSLog(@"%@ %@.", message, device);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
        }
    
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

        // If a port was obtained the following will execute the print command
        @try
        {
            while (bytesWritten < length)
            {
                bytesWritten += [port writePort:printCommand :bytesWritten : length - bytesWritten];
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
    }];
}

@end
