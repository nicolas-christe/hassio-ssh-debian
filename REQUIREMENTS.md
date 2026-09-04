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
  `homeassistant_config:rw`, `local_apps:rw`, `all_app_configs:rw`, `ssl:rw`,
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

The image has grown from its original "just enough for sshd" scope into a
personal dev box, since the whole point of persistent per-user home
directories (see "Account / state persistence") is to survive add-on
rebuilds — anything installed by hand into the container's own root
filesystem instead does not. Bundled in the Dockerfile itself:

- SSH/Samba core: `openssh-server`, `sudo`, `samba`, `samba-common-bin`,
  `samba-vfs-modules`, `smbclient`, `avahi-daemon`.
- `usbutils` — `lsusb` etc., for diagnosing USB-serial adapters (see "USB /
  serial device access" below).
- `gh` — GitHub CLI, so `git push`/PR workflows over SSH-authenticated
  HTTPS work out of the box after a rebuild without reinstalling it by hand.
- ESP-IDF tooling: `eim-cli` (Espressif's IDF Installation Manager, from
  Espressif's own apt repo) plus its build prerequisites (`gnupg`, `flex`,
  `bison`, `gperf`, `ccache`, `dfu-util`, `cmake`, `wget`, `libffi-dev`,
  `libssl-dev`) — `eim` itself still installs ESP-IDF under each user's own
  `$HOME/.espressif`, which persists on its own; only the apt-level
  prerequisites needed to be baked into the image.
- Swift toolchain build prerequisites (managed per-user via `swiftly`,
  itself under `$HOME`): `binutils-gold`, `gcc`, `libcurl4-openssl-dev`,
  `libedit-dev`, `libicu-dev`, `libncurses-dev`, `libpython3-dev`,
  `libsqlite3-dev`, `libxml2-dev`, `pkg-config`, `uuid-dev`.
- Anything else installed by hand into a running container (e.g. `git`,
  `htop`, `nano`) is **not** baked into the image and does not survive a
  rebuild — see each user's own reinstall notes for what to redo.

## USB / serial device access

- `usb: true` + `udev: true` in `config.yaml` cover USB bus discovery and
  hotplug events, but not the cgroup passthrough for the resulting
  `/dev/ttyUSB*`/`/dev/ttyACM*` character devices themselves — `uart: true`
  is the additional flag needed for Supervisor to actually grant that (added
  in 0.1.1; without it, even `root` inside the add-on gets `EPERM` opening
  those devices, regardless of `dialout` group permissions on the node).
- HAOS's host (buildroot-based) uses gid 18 for `dialout`, but Debian's own
  base image assigns gid 20 — a well-known HAOS/container gid mismatch. The
  add-on's account-init step (`init-accounts`) force-remaps the container's
  `dialout` group to gid 18 (only if nothing else already holds it) so that
  ownership of `/dev/ttyUSB*`/`/dev/ttyACM*` — which is whatever the *host*
  kernel assigned — lines up correctly for the container's own `dialout`
  members, regardless of what this container's own `/etc/group` originally
  said.

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
