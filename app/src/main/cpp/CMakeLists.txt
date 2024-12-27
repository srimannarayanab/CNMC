# Minimum required version of CMake
cmake_minimum_required(VERSION 3.4.1)

# Define the library
add_library(keys SHARED keys.c)

# Link necessary libraries (if needed) -- in this case, none specified,
# but you may need to add any specific libraries, e.g., Android system libraries