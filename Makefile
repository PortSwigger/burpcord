.PHONY: all build clean

all: build

build:
	cmd /c gradlew.bat shadowJar

clean:
	cmd /c gradlew.bat clean
