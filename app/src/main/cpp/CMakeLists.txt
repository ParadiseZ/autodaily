cmake_minimum_required(VERSION 3.22.1)
project("autodaily")
#设置ncnn
set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20241226-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)
#设置opencv
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-4.11.0-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc)
#关闭exception
#target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -fexceptions)
# 为库单独设置编译选项
#set_source_files_properties(clipper.cpp PROPERTIES COMPILE_FLAGS "-fexceptions")
set_source_files_properties(autodaily.cpp PROPERTIES COMPILE_FLAGS "-fexceptions")
set_source_files_properties(common.cpp PROPERTIES COMPILE_FLAGS "-fexceptions")
#为源文件单独设置编译
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
    # List C/C++ source files with relative paths to this CMakeLists.txt.
    autodaily.cpp yolo.cpp common.cpp CrnnNet.cpp
)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
    # List libraries link to the target library
    android
    jnigraphics
    ncnn
    ${OpenCV_LIBS}
    log
)
