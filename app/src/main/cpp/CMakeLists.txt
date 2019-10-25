cmake_minimum_required(VERSION 3.4.1)

#add libavcodec
add_library(libffmpeg
        SHARED
        IMPORTED
        )

SET_TARGET_PROPERTIES(
        libffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/ffmpeg/prebuilt/${ANDROID_ABI}/libffmpeg.so
)

include_directories(ffmpeg/)

add_library(
        ffmpeg-cmd
        SHARED
        ffmpeg/ffmpeg-cmd.cpp ffmpeg/ffmpeg.c ffmpeg/cmdutils.c ffmpeg/ffmpeg_filter.c ffmpeg/ffmpeg_hw.c ffmpeg/ffmpeg_opt.c
)

find_library( # Sets the name of the path variable.
        log-lib

        log)

target_link_libraries( # Specifies the target library.
        ffmpeg-cmd

        libffmpeg
        ${log-lib})
