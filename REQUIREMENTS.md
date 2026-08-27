# hassio-ssh-debian — Requirements

Custom Home Assistant add-on, built on HA's official Debian-based add-on
image, providing SSH and Samba access. Personal replacement for the official
"Terminal & SSH" add-on **and** the official "Samba share" add-on — both
must be uninstalled/stopped first, since this add-on binds the same ports
(22 for SSH; 139/445 for Samba).

## Purpose

- Shell into the add-on container itself for debugging/inspection.
- Run arbitrary Debian tools not available in HA OS (via `apt`).
- No web-based Ingress terminal — SSH only.

## Base image / architecture

- Base: `ghcr.io/home-assistant/aarch64-base-debian` — HA's official
  Debian-based add-on base image (S6-overlay + bashio built in, easiest to
  integrate with HA add-on tooling), used at its slimmest (no extra layers
  beyond what's needed for sshd).
- Supported architecture: `aarch64` only.

## SSH access

- Port: `22`. Add-on runs with `host_network: true` (needed anyway for Samba/
  NetBIOS discovery — see Samba section), so SSH/SMB ports are fixed on the
  host's own network stack, not remappable via Docker port publishing.
- Auth: SSH public key only, no password login.
- Key is entered by the user into the add-on's config UI (options schema),
  same pattern as the official Terminal & SSH add-on's `authorized_keys` list.
- Login user: `nicolas` — dedicated non-root user with passwordless `sudo`.
- No `root` SSH login.
- SSH host keys are generated once and persisted (see "Account/state
  persistence" below), so your Mac doesn't see a host-key-changed warning
  after every add-on update.

## Filesystem / volumes

- Standard HA add-on folders mapped in via `config.yaml`'s `map:` keys:
  `homeassistant_config:rw`, `addons:rw`, `all_addon_configs:rw`, `ssl:rw`,
  `share:rw`, `backup:rw`, `media:rw` (paths appear inside the container at
  `/homeassistant`, `/local_apps`, `/app_configs`, `/ssl`, `/share`,
  `/backup`, `/media` respectively).
- No Docker socket access (rejected — `docker_api` in HA's add-on schema is
  read-only and requires protection mode/AppArmor disabled, which conflicts
  with keeping this add-on protected/confined).
- No full host root / privileged mode, no Supervisor API token, protection
  mode (AppArmor) stays enabled.
- Each Linux user's home directory lives on disk at
  `/share/linux-box/home/<username>` (e.g. `/share/linux-box/home/nicolas`,
  later `/share/linux-box/home/bob`), and each user's `/etc/passwd` home
  field points directly there — no single bind-mount over `/home`, since
  multiple users need distinct home paths.

## Account / state persistence

- `/etc/passwd`, `/etc/shadow`, `/etc/group`, Samba's password database
  (`passdb.tdb`), and the SSH host keys are all persisted in the add-on's
  private `/data` volume (survives container restarts and add-on updates;
  only cleared on uninstall) and restored on startup.
- This keeps uid/gid and Samba credentials for `nicolas`, `bob`, etc. stable
  across add-on rebuilds — files written today keep the same ownership
  after any future update, matching what a native Linux install would do.

## Packages

- Minimal image: just what's needed to run `sshd` (`openssh-server`, `sudo`).
- No extra CLI/network/Python tooling preinstalled at this stage — added
  later as needed via `apt` inside the running container or by editing the
  Dockerfile.

## Samba

- Also runs an SMB server (`samba`), sharing:
  - The same HA folders/share names as the current official Samba add-on
    template: `[config]` (`/homeassistant`), `[local_apps]`
    (`/local_apps`), `[app_configs]` (`/app_configs`), `[ssl]` (`/ssl`),
    `[share]` (`/share`), `[backup]` (`/backup`), `[media]` (`/media`).
    The old `[addons]`/`[addon_configs]` share names are deprecated in the
    official template in favor of `local_apps`/`app_configs` — this add-on
    ships only the current names. These shares are `force user = root` /
    `force group = root`, same as the official add-on — Samba logins don't
    need write permission on the underlying files to use them.
  - Plus one home-directory share per user, backed by
    `/share/linux-box/home/<username>` on disk (see Filesystem section).
    **No** `force user`/`force group` on these — ownership follows whoever
    is logged in.
- Auth: username/password, set manually over SSH via `smbpasswd` for every
  user — including `nicolas` — no add-on config UI sets a Samba password
  for anyone. Keeps `nicolas` consistent with every other account rather
  than a special case.
- Home shares are multi-user: each Samba account maps 1:1 to a same-named
  Linux system account, so a file created from macOS while logged into
  Samba as `nicolas` ends up owned by the same uid/gid as if created
  locally on the box as `nicolas` — and likewise for any additional user
  (e.g. `bob`) added later. Stability across updates relies on the account
  persistence described above.
- Adding a future user (e.g. `bob`) is a manual admin task done over SSH:
  create the Linux system user (`useradd`), the matching Samba account
  (`smbpasswd -a`), and their home share stanza — no add-on config UI for
  user management.
- macOS support: `vfs_fruit` + `streams_xattr` + `catia` on all shares, same
  as the official add-on (proper resource-fork/extended-attribute handling,
  no `.DS_Store`/`._*` AppleDouble clutter, fast Finder browsing).
  **No Spotlight** — confirmed it requires compiling Samba from source with
  `--enable-spotlight` (not in Debian's stock package) plus Tracker3 and a
  session D-Bus that doesn't exist in this container, and would forfeit
  Debian's security updates for the SMB server. Not worth it — Finder
  search still works client-side, just slower on large trees.

## Backups

- Home directories under `/share/linux-box/home` are included in HA
  snapshots by default, same as everything else under `/share` — no
  exclusion configured. Backup size will grow with home directory contents.

## Distribution / install

- Published as a **private** repository on Nicolas's personal GitHub
  account, added to Home Assistant as a custom add-on repository
  (installable via repo URL), rather than a purely local `/addons/local`
  add-on.
