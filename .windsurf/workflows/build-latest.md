# Latest Build Version

This workflow builds the latest version of the Burpcord extension.

## Steps

1. Ensure you are on the latest commit
2. Run the build command to generate the latest JAR file
3. Verify the build was successful

## Build Command

This command builds the extension and creates the JAR file, plus sources + javadocs.

```bash
make build-all
```

This command sends the build to github packages.

```bash
make publish-core
```

Both commands should be run from the root of the repository.