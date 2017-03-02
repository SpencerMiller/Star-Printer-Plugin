"use strict";

var exec = require('cordova/exec');

module.exports = {
    findDevices: function() {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, 'OmniPrinter', 'findDevices');
        });

    },

    status: function(device) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, 'OmniPrinter', 'status', [device]);
        });
    },

    print: function(device, content) {
        return new Promise(function(resolve, reject) {
            exec(function(devices) { resolve = JSON.parse(devices); }, reject, 'OmniPrinter', 'print', [device, content]);
        });
    },
};