---
description: How to publish Burpcord to Maven Central
---

# Publish to Maven Central

This workflow guides you through configuring and publishing the project to Maven Central using the `com.vanniktech.maven.publish` plugin.

## Prerequisites

1.  **Maven Central Account**: You need an account on [trace.sonatype.org](https://central.sonatype.org/register/central-portal/).
2.  **GPG Key**: You need a GPG key to sign your artifacts.
3.  **Secrets**: You need to have the following properties available (usually in `~/.gradle/gradle.properties`):
    *   `mavenCentralUsername`: Your Sonatype token username.
    *   `mavenCentralPassword`: Your Sonatype token password.
    *   `signing.keyId`: The last 8 characters of your GPG key ID.
    *   `signing.password`: The passphrase for your GPG key.
    *   `signing.secretKeyRingFile`: The path to your secret key ring file.

## Steps

1.  **Apply Plugin**: Add the plugin to `build.gradle`.

    ```gradle
    plugins {
        id "com.vanniktech.maven.publish" version "0.28.0" // Check for latest version
    }
    ```

2.  **Configure Publishing**: Add the `mavenPublishing` block to `build.gradle`.

    ```gradle
    mavenPublishing {
        // Define coordinates
        coordinates("tech.chron0.burpcord", "burpcord", "2.1.0")

        // Configure POM metadata
        pom {
            name = "Burpcord"
            description = "Discord Rich Presence integration for Burp Suite"
            inceptionYear = "2026"
            url = "https://github.com/jondmarien/Burpcord" 
            licenses {
                license {
                    name = "The MIT License" // Or your license
                    url = "https://opensource.org/licenses/MIT"
                    distribution = "repo"
                }
            }
            developers {
                developer {
                    id = "jondmarien"
                    name = "Jonathan Marien"
                    url = "https://github.com/jondmarien"
                }
            }
            scm {
                url = "https://github.com/jondmarien/Burpcord"
                connection = "scm:git:git://github.com/jondmarien/Burpcord.git"
                developerConnection = "scm:git:ssh://git@github.com/jondmarien/Burpcord.git"
            }
        }

        // Configure publishing to Maven Central via Central Portal
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)

        // Sign all artifacts
        signAllPublications()
    }
    ```

3.  **Run Publication Task**:
    
    To publish to Maven Central (staging):
    ```bash
    ./gradlew publishToMavenCentral
    ```

    To release (close and release staging repository):
    ```bash
    ./gradlew closeAndReleaseMavenCentralStagingRepository
    ```

## Notes

-   The `shadowJar` plugin might conflict with the default publication. You may need to configure the publication to use the shadow jar artifact explicitly if that is what you intend to publish.
-   Ensure your `gradle.properties` file is secure and not committed to version control.
