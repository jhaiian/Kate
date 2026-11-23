cmake_minimum_required(VERSION 3.22.1)
project(kateaudio LANGUAGES CXX)

find_package(oboe REQUIRED CONFIG)

add_library(kateaudio SHARED kate_audio.cpp)
target_compile_features(kateaudio PRIVATE cxx_std_17)
target_compile_options(kateaudio PRIVATE -O2 -Wall)
target_link_libraries(kateaudio PRIVATE oboe::oboe log)
target_link_options(kateaudio PRIVATE "-Wl,-z,max-page-size=16384")
