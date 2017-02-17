var exec = require('cordova/exec');

exports.getPrinters = function(arg0, success, error) {
    exec(success, error, "StarPrinter", "getPrinters", [arg0]);
};
