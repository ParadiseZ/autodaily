// IUserService.aidl
package com.smart.autodaily;

// Declare any non-default types here with import statements
//参考来自https://github.com/xxinPro/AdbShellUtils
interface IUserService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/
 /**
      * Shizuku服务端定义的销毁方法
      */
     void destroy() = 16777114;

     /**
      * 自定义的退出方法
      */
     void exit() = 1;

     android.graphics.Bitmap execCap(String command,int width,int height,int scale) = 2;

     void execVoidComand(String command) = 3;

     void close() = 4;
}