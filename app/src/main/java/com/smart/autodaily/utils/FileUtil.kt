package com.smart.autodaily.utils

import java.io.File

fun deleteFile(file : File){
    if(file.exists()){
        file.delete()
    }
}