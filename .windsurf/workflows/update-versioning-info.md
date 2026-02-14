---
auto_execution_mode: 2
---
# Update Versioning Information

Please use @AGENTS.md , @README.md , and analyze the codebase for reference. Let me know if you have any questions.

Also, update the @CHANGELOG.md (to match), and @release_notes.md , as well as @README.md , and update the version number to the next semantic version based on the changes made, or what the user asks for.

The @CHANGELOG.md is more user facing, look how the other entries are written, and the @release_notes.md is more dev-oriented.

Make sure to follow the existing format and style in the changelog and release notes.

Ensure all version numbers are consistent across all files.

Double-check that the version in @build.gradle matches the version in @CHANGELOG.md and @release_notes.md.

Verify that the version in the JAR file name matches the version in @build.gradle.

Also, ensure the version is updated in any configuration files that might reference it, such as @settings.gradle, @gradle.properties, or any other version-specific configuration files.
