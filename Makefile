.PHONY: all build clean publish-core publish-all

all: build

build:
	cmd /c gradlew.bat shadowJar

build-all:
	cmd /c gradlew.bat clean shadowJar sourcesJar plainJavadocJar

publish-core:
	cmd /c gradlew.bat publishToGitHubPackages

publish-all:
	cmd /c gradlew.bat publishToGitHubPackages publishMavenPublicationToGithubPackagesRepository

clean:
	cmd /c gradlew.bat clean