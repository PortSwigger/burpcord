.PHONY: all build clean

all: build

build:
	cmd /c gradlew.bat jar

clean:
	cmd /c gradlew.bat clean
