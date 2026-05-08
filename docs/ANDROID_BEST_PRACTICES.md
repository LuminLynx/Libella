# Android Best Practices & Google Play Guidelines

> **Scope.** Android-specific decisions made during Phase 1 to keep
> the app aligned with Google Play Store guidelines and current
> Jetpack / Compose best practices. Complementary to `STRATEGY.md`
> (product strategy) and `EXECUTION.md` (phase sequencing); not a
> substitute for either.

---

## How this file is maintained

**Update in the same PR** that makes the change when the PR:

- Introduces a new architectural decision (new library, new pattern, new platform feature).
- Reverses or significantly modifies an existing decision in this doc.
- Touches Google Play–relevant config (target/minSdk, signing, permissions, backup, ProGuard/R8, data-extraction rules).
- Adds a new platform subsystem with a "why is it shaped this way?" justification (payments, notifications, deep links, etc.).

**Skip the update for** bug fixes that don't change the underlying decision, content tweaks, test-only changes, pure refactors, or one-off polish.

**PR discipline.** The PR description should explicitly say either *"adds §N to ANDROID_BEST_PRACTICES.md"* or *"no documented decisions change."* Reviewers ask "should this be in the best-practices doc?" during review.

**Periodic audit.** At each phase boundary, sweep the file: confirm each section still matches the code, retire entries replaced by new decisions, refresh external links if Google's docs moved.

---

## 1. Auto-backup with selective exclusion

**Decision.** `android:allowBackup="true"` stays enabled. The encrypted
prefs file (`ai101_auth_prefs.xml`) is excluded from backup via
`android:fullBackupContent` (API 23–30) and
`android:dataExtractionRules` (API 31+).

**Why.** Disabling backup wholesale (`allowBackup="false"`) is the easy
fix for the `EncryptedSharedPreferences` corruption bug — a backed-up
encrypted file restored on a fresh install fails to decrypt because
AndroidKeyStore master keys are *never* backed up by design. But
disabling backup forfeits user-friendly device-migration behavior for
non-sensitive state (e.g. the completion cache).

Google's official guidance for apps using `EncryptedSharedPreferences`
is to **exclude only the encrypted file**, not the whole app
([developer.android.com/privacy-and-security/security-tips#UserData](https://developer.android.com/privacy-and-security/security-tips#UserData)).

**Files.**
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/main/AndroidManifest.xml` (references both)

**Defense-in-depth.** `EncryptedTokenStorage` also catches any
exception during `EncryptedSharedPreferences.create()`, wipes the
prefs file, and recreates. Rescues any device already in a corrupted
state from a prior install; lets future installs self-heal even if
the backup-rules cover misses an edge case.

**Source PR.** [#65](https://github.com/LuminLynx/FOSS-101/pull/65).

---

## 2. Compose state production: VM exposes state, UI drives effects

**Decision.** ViewModels do **not** call data-fetch logic from
`init { … }`. Loads are triggered by the screen's lifecycle effects
(`LifecycleResumeEffect`, `LaunchedEffect`).

**Why.** Aligned with Google's
[UI Layer State Production guide](https://developer.android.com/jetpack/guide/ui-layer/state-production).
Concrete benefits we hit during Phase 1:

- **Tests don't fire implicit network calls on construction.** Tests
  build the VM, then call `viewModel.load()` explicitly. Easy to
  assert "load was not called" cases.
- **Single source of truth for "when do we load?"** — the lifecycle
  effect. No flag duplication between init and resume.
- **Auth-state changes propagate.** When the user signs in via
  Settings and returns to path home, the resume effect fires →
  `viewModel.load()` runs → cross-device completion sync runs with
  the now-valid token → cache is seeded. With `init { load() }`, the
  load only fired once at empty-cache time and never re-ran.

**Anti-pattern we removed.** Earlier in Phase 1 we used
`var initialMount by remember { mutableStateOf(true) }` (and later
`rememberSaveable`) as a "have I been here before" guard inside the
`LifecycleResumeEffect`. Both shapes are workarounds for state that
should belong to the VM (`hasLoadedOnce`) or be eliminated entirely
(always load on resume). `rememberSaveable` is for **user-visible UI
state** (text fields, scroll positions), not lifecycle bookkeeping.

**Source PR.** [#66](https://github.com/LuminLynx/FOSS-101/pull/66).

**Migration status.** `PathHomeViewModel` follows this pattern as of
PR #66. `UnitReaderViewModel` still has `init { load() }`; migrating
it is queued as Phase 2 polish since the unit reader's lifecycle
doesn't currently exhibit the auth-change bug.

---

## 3. One-shot navigation events via Channel

**Decision.** Auth-expired-style events that should fire **once per
occurrence** are emitted through a `Channel` collected by the screen,
not stored on the UI state.

**Why.** A `state.authExpired = true` flag on the Error state would
re-trigger navigation on every recomposition — including after the
user pops back from the sign-in screen with stale state, creating a
login loop.

`Channel(BUFFERED)` events are consumed exactly once. The screen
collects via `LaunchedEffect(viewModel)` which keys to the VM's
identity (stable across recompositions).

**Source PR.** [#59](https://github.com/LuminLynx/FOSS-101/pull/59)
(review fix during chunk 7).

---

## 4. Per-user completion cache scoping

**Decision.** `SharedPrefsCompletionCache` keys entries as
`completed_unit_ids:<userId>`. Reads and writes silently no-op when no
user is authenticated.

**Why.** A single shared key would leak completion state across
accounts on the same device. Per-user keying is mechanically simple
and matches how the underlying backend records (`completions` table)
are partitioned.

**Edge cases handled.**
- Logout doesn't wipe the cache; a returning user gets their prior
  state back (correct).
- Different user signs in → reads their own keyed entries; previous
  user's entries are inaccessible without their userId (correct).

**Source PR.** [#59](https://github.com/LuminLynx/FOSS-101/pull/59)
(review fix during chunk 7).

---

## 5. Cross-device completion sync

**Decision.** On every path-home resume, `PathHomeViewModel.load()`
performs a best-effort `GET /api/v1/completions`, then replaces the
local cache with the server-side set, then computes "Continue."

**Why.** The local SharedPreferences cache is per-device. Same account
on a second device would see every unit as uncompleted without a
server pull. The sync is **best-effort** (failures swallowed, fall
through to local cache) so offline / transient-5xx / pre-auth states
still load the path home with last-known data.

**Source PR.** [#64](https://github.com/LuminLynx/FOSS-101/pull/64).

---

## 6. JWT propagation pattern

**Decision.** API service factories accept a `tokenProvider: () -> String?`
callback and attach `Authorization: Bearer …` to every request when
the provider returns non-null. The callback hits
`EncryptedTokenStorage` on demand, so token rotation propagates
without holding stale references.

**Why.** Avoids passing tokens through every layer. Centralizes the
"where does the token live?" question to `RepositoryProvider`'s lazy
wiring. Matches the existing chunk-3 `GlossaryApiServiceFactory`
shape.

**Source PRs.** Chunk 7 (`PathApiServiceFactory`); chunk 3
(`GlossaryApiServiceFactory`).

---

## 7. Encrypted token storage

**Decision.** Auth tokens, user id, and display profile bits live in
`EncryptedSharedPreferences` (AES256-GCM values, AES256-SIV keys),
backed by an AndroidKeyStore master key.

**Why.** Required for Play Store apps storing auth tokens at rest.
The master key is not backed up (by Android design); see §1 above for
the corresponding backup-rules / recovery setup.

---

## Reading list

- [Privacy and Security tips](https://developer.android.com/privacy-and-security/security-tips) — Google's overall security guidelines for Play Store apps.
- [Auto Backup for Apps](https://developer.android.com/identity/data/autobackup) — backup rules format and behavior.
- [UI Layer State Production](https://developer.android.com/jetpack/guide/ui-layer/state-production) — current Compose / VM state production guidance.
- [Encrypted Shared Preferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) — official API docs and the keystore-key-not-backed-up caveat.
