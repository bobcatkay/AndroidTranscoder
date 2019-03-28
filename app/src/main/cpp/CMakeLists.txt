cmake_minimum_required(VERSION 3.4.1)

#add libavcodec
add_library(libavcodec
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libavcodec
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libavcodec.so
)

#add libavfilter
add_library(libavfilter
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libavfilter
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libavfilter.so
)


#add libavformat
add_library(libavformat
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libavformat
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libavformat.so
)


#add libavutil
add_library(libavutil
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libavutil
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libavutil.so
)


#add libpostproc
add_library(libpostproc
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libpostproc
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libpostproc.so
)

#add libswresample
add_library(libswresample
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libswresample
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libswresample.so
)

#add libswscale
add_library(libswscale
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libswscale
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libswscale.so
)

include_directories(ffmpeg/)

add_library(
        ffmpeg-cmd
        SHARED
        ffmpeg/ffmpeg-cmd.cpp ffmpeg/ffmpeg.c ffmpeg/cmdutils.c ffmpeg/ffmpeg_filter.c ffmpeg/ffmpeg_hw.c ffmpeg/ffmpeg_opt.c
)
target_link_libraries( # Specifies the target library.
        ffmpeg-cmd

        libavcodec
        libswscale
        libswresample
        libpostproc
        libavutil
        libavformat
        libavfilter
        ${log-lib})

find_library( # Sets the name of the path variable.
        log-lib

        log)
