cmake_minimum_required(VERSION 3.1)
project(Sudoku)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall")

include_directories(../../../examples/sat/)

add_executable(sudokuTest
	SudokuSolver.cpp
	sudokuTest.cpp
	../../../examples/sat/sat.cpp
)

add_custom_target(run
    COMMAND sudokuTest
    COMMENT "Running sudokuTest"
)
