package com.smart.autodaily.utils

import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.data.entity.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException

object DownloadManager {
    suspend fun download(scriptId: Int, file: File, fileType: String): Flow<DownloadState> {
        return flow {
            val response = RemoteApi.searchDownRetrofit.downloadModel(scriptId, fileType).execute()
            if (response.isSuccessful) {
                saveToFile(response.body()!!, file) {
                    emit(DownloadState.InProgress(it))
                }
                emit(DownloadState.Success(file))
            } else {
                emit(DownloadState.Error(IOException(response.toString())))
            }
        }.catch {
            emit(DownloadState.Error(it))
        }.flowOn(Dispatchers.IO)
    }
    /*suspend fun download(url: String, file: File): Flow<DownloadState> {
        return flow {
            val response = RemoteApi.searchDownRetrofit.downloadModel(scriptId, fileType).execute()
            if (response.isSuccessful) {
                saveToFile(response.body()!!, file) {
                    emit(DownloadState.InProgress(it))
                }
                emit(DownloadState.Success(file))
            } else {
                emit(DownloadState.Error(IOException(response.toString())))
            }
        }.catch {
            emit(DownloadState.Error(it))
        }.flowOn(Dispatchers.IO)
    }*/

    private inline fun saveToFile(responseBody: ResponseBody, file: File, progressListener: (Int) -> Unit) {
        val total = responseBody.contentLength()
        var bytesCopied = 0
        var emittedProgress = 0
        file.outputStream().use { output ->
            val input = responseBody.byteStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = input.read(buffer)
            while (bytes >= 0) {
                output.write(buffer, 0, bytes)
                bytesCopied += bytes
                bytes = input.read(buffer)
                val progress = (bytesCopied * 100 / total).toInt()
                if (progress - emittedProgress > 0) {
                    progressListener(progress)
                    emittedProgress = progress
                }
            }
        }
    }
}