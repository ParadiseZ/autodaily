package com.smart.autodaily.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.smart.autodaily.BuildConfig
import com.smart.autodaily.IUserService
import com.smart.autodaily.service.UserService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import splitties.init.appCtx
import splitties.toast.toast


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
    fun requestShizukuPermission() {
        grant = checkPermission()
        println("检测权限结果：${grant}")
        if (grant) {
            return
        }

        if (Shizuku.isPreV11()) {
            //toast("当前shizuku版本不支持动态申请")
            //Toast.makeText(this, "当前shizuku版本不支持动态申请", Toast.LENGTH_SHORT).show()
            return
        }
        println("进行权限申请：${grant}")
        // 动态申请权限
        Shizuku.requestPermission(10001)
    }

    //权限检测
    private fun checkPermission(): Boolean {
        println("检测权限")
        /*if (iUserService == null){
            println("服务为空")
            return false
        }*/
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
            appCtx.toast("服务连接成功！")
            println("服务已连接")
            if (iBinder.pingBinder()) {
                iUserService = IUserService.Stub.asInterface(iBinder)
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            //Toast.makeText(this@MainActivity, "服务连接断开", Toast.LENGTH_SHORT).show()
            iUserService = null
        }
    }
}