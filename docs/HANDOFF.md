# Handoff — Meeting Room Panel tablet app

For the Rihal team taking over the door-tablet app. The portal it talks to is in a separate repo, **meeting-room-backend**; see its `docs/HANDOFF.md`.

## What it is

An Android app (Kotlin, Jetpack Compose) that runs as a locked-down kiosk (Device Owner) on a tablet outside each meeting room:

- It shows whether the room is free, plus today's and tomorrow's schedule.
- People book, check in, extend, end, find a free room and report a problem by tapping. There is no typing.
- It talks **only** to the portal's `/api/device/*` routes, using a revocable per-tablet token. It holds no Microsoft credentials.
- The portal address comes from the setup QR code and can be moved from the portal (see "Portal address" below).

The current version is **0.4.1** (versionCode 4), released to every tablet on 15 October 2026.

## The signing key

`room-panel.jks` (alias `room-panel`) and its password are held in the **Rihal IT secrets vault**. Never commit them, and never send them by email or chat.

Every APK a tablet installs must be signed with this key, or Android refuses the update. If the key is lost, every tablet has to be factory-reset and set up again.

## Build, test, release

From `android/`:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug verifyRoborazziDebug   # must pass before every push
./gradlew recordRoborazziDebug                                             # after an intentional UI change; then look at the screenshots
```

To release a new version:

1. Raise `versionCode` and `versionName` in `android/app/build.gradle.kts`.
2. Build the signed APK. The keystore and password come from the vault:
   ```sh
   ./gradlew assembleRelease -PbackendUrl=https://<portal-host> \
     -PreleaseKeystore=/path/room-panel.jks -PreleaseKeyAlias=room-panel \
     -PreleaseStorePassword="$PW" -PreleaseKeyPassword="$PW"
   # A release refuses to build without an explicit https:// -PbackendUrl (no silent default).
   sha256sum app/build/outputs/apk/release/app-release.apk
   ```
3. Publish it from the portal. The steps are in the portal's `docs/HANDOFF.md` under "Releasing a new tablet app version": one tablet first with **Test version**, then everyone.

CI (`.github/workflows/android.yml`) runs the checks on pushes to `main` and on pull requests.

## Rules for the code

- Domain logic goes in `domain/` as pure Kotlin with JUnit tests. The UI only renders `PanelUiState`.
- Every user-facing string goes in `res/values/strings.xml` **and** `res/values-ar/strings.xml`. Layouts must work right to left.
- Status colours must keep at least 4.5:1 contrast with white. Touch targets must be at least 64 dp.
- No Microsoft or Graph credentials on the device, ever. Microsoft Graph only, never EWS.

`CLAUDE.md` has the full list.

## Portal address

- **New tablets** take the portal address from the setup QR code (`server_url`).
- **Installed tablets** move when an admin uses **Setup checks → Move tablets to a new address**. A tablet switches only after the new portal accepts its token, and otherwise retries every 10 minutes.
- **Cable-installed tablets** start with the address built into the APK (`-PbackendUrl`, default the Vercel portal).
- The runbook for moving to Rihal IT servers is `docs/self-hosting.md` in the portal repo.

## Docs

| File | What |
| --- | --- |
| `docs/tablet-setup.md` | QR setup, the cable method, kiosk mode, updates, troubleshooting |
| `docs/m365-setup.md` | Microsoft 365 / Entra setup and certificate renewal |
| `docs/PLAN.md` | The original plan and decisions |

## Open items

- 0.4.1 has not been confirmed on a real tablet yet. The portal's **Setup checks → Check tablet status** shows each tablet's version.
- The release build has only been tested in unit and Robolectric tests. There is no emulator in the build environment.
