// https://stackoverflow.com/questions/31024214/add-an-attribute-at-the-application-tag-in-the-androidmanifest-from-a-cordova
module.exports = function(ctx) {
  var fs = ctx.requireCordovaModule('fs')
  var path = ctx.requireCordovaModule('path')
  var xml = ctx.requireCordovaModule('cordova-common').xmlHelpers

  var manifestPath = path.join(
    ctx.opts.projectRoot,
    'platforms/android/AndroidManifest.xml'
  )
  var doc = xml.parseElementtreeSync(manifestPath);
  if (doc.getroot().tag !== 'manifest') {
    throw new Error(
      manifestPath + ' has incorrect root node name (expected \'manifest\')'
    )
  }

  // add tools:replace in the application node
  doc.getroot().find('./application').attrib['android:name'] = 'com.audio.converter.App'

  // write the manifest file
  fs.writeFileSync(manifestPath, doc.write({ indent: 4 }), 'utf-8')
}
