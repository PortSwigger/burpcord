.PHONY: all build build-all clean publish-core publish-all help

all: build

build:
	cmd /c gradlew.bat shadowJar

build-all:
	cmd /c gradlew.bat clean shadowJar sourcesJar plainJavadocJar

publish-core:
	cmd /c gradlew.bat publishAllPublicationsToGithubPackagesRepository

publish-all:
	cmd /c gradlew.bat publishAllPublicationsToGithubPackagesRepository publishMavenPublicationToGithubPackagesRepository

clean:
	cmd /c gradlew.bat clean

help:
	@cmd /c echo Available commands:
	@cmd /c echo   make build        - Build the shadow JAR
	@cmd /c echo   make build-all    - Clean build with sources and javadoc JARs
	@cmd /c echo   make clean        - Remove build artifacts
	@cmd /c echo   make publish-core - Publish to GitHub Packages
	@cmd /c echo   make publish-all  - Publish to GitHub Packages and Maven Central
	@cmd /c echo   make help         - Show this help message