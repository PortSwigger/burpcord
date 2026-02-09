# PR: Fix null-safety in Pipe handshake and RichPresence serialization

## Motivation

We maintain [Burpcord](https://github.com/jondmarien/Burpcord), an open-source Burp Suite extension that uses DiscordIPC to display real-time security testing activity as Discord Rich Presence. Burpcord has active users who load the extension inside Burp Suite — a long-running desktop application — where Discord may already be running, still starting up, or restarting mid-session.

After upgrading from DiscordIPC 0.10.2 to 0.11.2, we hit two NPE crashes that made the library unusable without downstream workarounds:

1. **Pipe handshake null `data`** — When Discord's IPC pipe is open but the client hasn't fully authenticated the user yet, `openPipe()` crashes with an opaque NPE. This is a race condition that affects any consumer connecting early or on slower machines. We currently work around this with retry-with-backoff and a 10-second connect timeout wrapper, but the fix belongs in the library itself so all consumers benefit.

2. **RichPresence `statusDisplayType` NPE** — The new `statusDisplayType` field introduced in 0.11.x has no default value in `Builder`, so any consumer that doesn't explicitly call `setStatusDisplayType()` gets an NPE in `toJson()`. This is a silent breaking change from 0.10.x where the field didn't exist. We work around it by always setting `StatusDisplayType.Name`, but consumers upgrading from 0.10.x will hit this unexpectedly.

These fixes are minimal, non-breaking, and would eliminate the need for every downstream consumer to implement their own defensive wrappers.

## Summary

Two `NullPointerException` crashes caused by missing null-checks:

1. **`Pipe.openPipe()`** — The `data` field in the handshake `READY` response can be `null` when Discord's IPC pipe is open but the client hasn't finished user authentication. This causes an NPE on the very next line when accessing `data.getAsJsonObject("user")`.

2. **`RichPresence.toJson()`** — The `statusDisplayType` field (added in 0.11.x) defaults to `null` in `Builder`, but `toJson()` unconditionally calls `statusDisplayType.ordinal()`, crashing any consumer that doesn't explicitly set it.

## Root Cause

### Bug 1: Pipe.java

Discord's IPC server accepts the named pipe connection and receives the handshake, then validates the application via its internal API. If the client is still initializing (user not fully authenticated), the `READY` dispatch is sent with a `null` `data` field. The library does not handle this case.

```java
// Pipe.java — openPipe() — current code (BROKEN)
final JsonObject data = parsedData.getAsJsonObject("data");     // returns null
final JsonObject userData = data.getAsJsonObject("user");       // NPE!

pipe.build = DiscordBuild.from(data                             // also NPE
        .getAsJsonObject("config")
        .get("api_endpoint").getAsString());
```

### Bug 2: RichPresence.java

The `Builder` class does not default `statusDisplayType`, so `build()` passes `null` to the `RichPresence` constructor. Then `toJson()` crashes:

```java
// RichPresence.java — toJson() — current code (BROKEN)
finalObject.addProperty("status_display_type", statusDisplayType.ordinal()); // NPE if null
```

## Proposed Fix

### Pipe.java — Add null-check with descriptive IOException

```java
// Pipe.java — openPipe() — FIXED
Packet p = pipe.read();

final JsonObject parsedData = p.getJson();
final JsonObject data = parsedData.getAsJsonObject("data");

if (data == null) {
    pipe.close();
    throw new IOException(
        "Discord IPC handshake returned null data for pipe " + i
        + " — the client may not be fully initialized. Retry after Discord is fully loaded."
    );
}

final JsonObject userData = data.getAsJsonObject("user");
```

This converts the opaque NPE into a catchable `IOException` with a descriptive message, and properly closes the pipe. The existing `catch (IOException | JsonParseException ex)` block in the for-loop will handle it, allowing the library to try the next pipe index naturally.

### RichPresence.java — Default null enums in `toJson()`

```java
// RichPresence.java — toJson() — FIXED
finalObject.addProperty("type", activityType != null ? activityType.ordinal() : ActivityType.Playing.ordinal());
finalObject.addProperty("status_display_type", statusDisplayType != null ? statusDisplayType.ordinal() : StatusDisplayType.Name.ordinal());
```

And/or default in `Builder.build()`:

```java
// RichPresence.Builder — build() — FIXED (defensive defaults)
public RichPresence build() {
    return new RichPresence(
            activityType != null ? activityType : ActivityType.Playing,
            statusDisplayType != null ? statusDisplayType : StatusDisplayType.Name,
            state, stateUrl,
            // ... rest unchanged
    );
}
```

## Impact

- **Pipe.java fix**: Prevents NPE when Discord is slow to initialize. Consumers no longer need custom retry/timeout wrappers to handle this.
- **RichPresence.java fix**: Prevents NPE for any consumer that doesn't explicitly call `setStatusDisplayType()` — a breaking behavioral change from 0.10.x where this field didn't exist.

## Reproduction

1. Start Discord on a machine with slow startup (e.g., VM, remote desktop, slow network)
2. Immediately connect via IPC before Discord fully authenticates the user
3. `Pipe.openPipe()` → NPE on `data.getAsJsonObject("user")`

For the RichPresence bug:
1. Build a `RichPresence` without calling `setStatusDisplayType()`
2. Call `sendRichPresence(presence)` → NPE in `toJson()`

## Tested With

- Discord Desktop 1.0.9223 (Windows, Feb 2026)
- DiscordIPC 0.11.2
- Java 21
- Burp Suite extension (Burpcord) — field-tested across multiple machines/networks
