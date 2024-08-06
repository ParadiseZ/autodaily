package com.smart.autodaily.utils

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.smart.autodaily.BuildConfig
import com.smart.autodaily.IUserService
import com.smart.autodaily.service.UserService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import splitties.init.appCtx


object ShizukuUtil {
    var grant : Boolean = false
    var iUserService : IUserService?=null

    fun initShizuku(){
        //权限
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
        //服务启动监听
        Shizuku.addBinderReceivedListener(onBinderReceivedListener)
        //服务终止监听
        Shizuku.addBinderDeadListener(onBinderDeadListener)
    }

    fun removeShizuku(){
        //权限
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
        //服务启动监听
        Shizuku.removeBinderReceivedListener (onBinderReceivedListener)
        //服务终止监听
        Shizuku.removeBinderDeadListener(onBinderDeadListener)
    }

    //请求权限
    fun requestShizukuPermission(context: Context) {
        grant = checkPermission()
        if (grant) {
            return
        }
        if (Shizuku.isPreV11()) {
            context.toastOnUi("当前shizuku版本不支持动态申请")
            return
        }
        // 动态申请权限
        Shizuku.requestPermission(10001)
    }

    //权限检测
    private fun checkPermission(): Boolean {
        return  Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    //权限请求结果监听
    private val onRequestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult -> grant = grantResult == PackageManager.PERMISSION_GRANTED }

    //服务监听
    private val onBinderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            println("服务已绑定")
            // 绑定shizuku服务
        }

    //服务死亡监听
    private val onBinderDeadListener =
        Shizuku.OnBinderDeadListener {
            // 取消绑定shizuku服务
            iUserService = null
            Shizuku.unbindUserService(userServiceArgs, serviceConnection,false)
            println("服务已停止 ")
        }


    //服务对象构建
    val userServiceArgs: UserServiceArgs = UserServiceArgs(
        ComponentName(
            BuildConfig.APPLICATION_ID,
            UserService::class.java.name
        )
    )
        .daemon(false)
        .processNameSuffix("adb_service")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)


    // 本地服务连接shizuku服务
    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            if (iBinder.pingBinder()) {
                iUserService = IUserService.Stub.asInterface(iBinder)
                appCtx.toastOnUi("shizuku连接成功！")
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            //Toast.makeText(this@MainActivity, "服务连接断开", Toast.LENGTH_SHORT).show()
            iUserService = null
        }
    }
}