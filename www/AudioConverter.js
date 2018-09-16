/* global cordova, module */

module.exports = {
  convert: function(options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AudioConverter', 'convert', [
      options
    ])
  }
}
