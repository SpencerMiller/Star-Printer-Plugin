"use strict";

var exec = require('cordova/exec');

module.exports = {
    findDevices: function() {
        return new Promise(function(resolve, reject) {
            exec(function(devices) { resolve = JSON.parse(devices); }, reject, 'OmniPrinter', 'findDevices');
        });

    },

    print: function(device, content) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, 'OmniPrinter', 'print', [device, content]);
        });
    },
};