#!/bin/bash
NDK=/home/ndk/android-ndk-r15c
ADDI_CFLAGS="-fPIE -pie"
ADDI_LDFLAGS="-fPIE -pie"
CPU=""
PREFIX=""
x264=""
HOST=""
CROSS_PREFIX=""
SYSROOT=""
ARCH=""
GCC_LIB=""

configure()
{
    CPU=$1
    PREFIX=$(pwd)/android/$CPU
    x264=$(pwd)/x264/android/$CPU
    HOST=""
    CROSS_PREFIX=""
    SYSROOT=""
    ARCH=""
    GCC_LIB=""
    if [ "$CPU" == "armv7-a" ]
    then
        ARCH="arm"
        HOST=arm-linux
        SYSROOT=$NDK/platforms/android-21/arch-arm
        CROSS_PREFIX=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-
	GCC_LIB=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/lib/gcc/arm-linux-androideabi/4.9.x
    else
        ARCH="aarch64"
        HOST=aarch64-linux
        SYSROOT=$NDK/platforms/android-21/arch-arm64
        CROSS_PREFIX=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/bin/aarch64-linux-android-
	GCC_LIB=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/lib/gcc/aarch64-linux-android/4.9.x
    fi
    ./configure \
    --prefix=$PREFIX \
    --disable-encoders \
    --disable-decoders \
    --disable-hwaccels \
    --disable-indevs \
    --disable-outdevs \
    --disable-devices \
    --disable-postproc \
    --disable-avdevice \
    --disable-ffprobe \
    --disable-programs \
    --enable-static \
    --disable-doc \
    --disable-symver \
    --disable-ffplay \
    --disable-network \
    --disable-doc \
    --disable-w32threads \
    --disable-os2threads \
    --disable-stripping \
    --enable-neon \
    --enable-small \
    --disable-shared \
    --enable-libx264 \
    --enable-gpl \
    --enable-pic \
    --enable-jni \
    --enable-pthreads \
    --enable-mediacodec \
    --enable-ffmpeg \
    --enable-encoder=aac \
    --enable-encoder=gif \
    --enable-encoder=mjpeg \
    --enable-encoder=libx264 \
    --enable-encoder=vorbis \
    --enable-encoder=mpeg4 \
    --enable-encoder=png \
    --enable-encoder=srt \
    --enable-encoder=subrip \
    --enable-encoder=yuv4 \
    --enable-encoder=text \
    --enable-decoder=aac \
    --enable-decoder=aac_latm \
    --enable-decoder=ac3 \
    --enable-decoder=pgssub \
    --enable-decoder=opus \
    --enable-decoder=dca \
    --enable-decoder=mp3 \
    --enable-decoder=pcm_s16le \
    --enable-decoder=pcm_s16le_planar \
    --enable-decoder=pcm_s16be \
    --enable-decoder=pcm_s16be_planar \
    --enable-decoder=vorbis \
    --enable-decoder=flac \
    --enable-decoder=flv \
    --enable-decoder=png \
    --enable-decoder=mjpeg \
    --enable-decoder=srt \
    --enable-decoder=xsub \
    --enable-decoder=yuv4 \
    --enable-decoder=vp9 \
    --enable-decoder=vp9_mediacodec \
    --enable-decoder=h264_mediacodec \
    --enable-decoder=h264 \
    --enable-decoder=hevc_mediacodec \
    --enable-decoder=hevc \
    --enable-hwaccel=h264_mediacodec \
    --enable-hwaccel=vp9_mediacodec \
    --enable-cross-compile \
    --cross-prefix=$CROSS_PREFIX \
    --target-os=android \
    --arch=$ARCH \
    --sysroot=$SYSROOT \
    --extra-cflags="-I$x264/include $ADDI_CFLAGS" \
    --extra-ldflags="-L$x264/lib"
}

build_one()
{
${CROSS_PREFIX}ar d libavcodec/libavcodec.a inverse.o


echo "build one lib..."
${CROSS_PREFIX}ld -rpath-link=$SYSROOT/usr/lib \
        -L$SYSROOT/usr/lib \
        -L$PREFIX/lib \
        -L$x264/lib \
        -soname libffmpeg.so \
        -shared \
        -nostdlib \
        -z noexecstack \
        -Bsymbolic\
        --whole-archive \
        --no-undefined \
        -o $PREFIX/libffmpeg.so \
	libavcodec/libavcodec.a \
        libavformat/libavformat.a \
	libavfilter/libavfilter.a \
        libavutil/libavutil.a \
        libswscale/libswscale.a \
        libswresample/libswresample.a \
        $x264/lib/libx264.a \
        -lc -lm -lz -ldl -llog \
        --dynamic-linker=/system/bin/linker $GCC_LIB/libgcc.a

${CROSS_PREFIX}strip --strip-unneeded $PREFIX/libffmpeg.so

echo "build complete."
}

build()
{
    make clean
    cpu=$1
    echo "build $cpu"
    
    configure $cpu
    make -j4
    make install
    build_one
}

build arm64
build armv7-a
