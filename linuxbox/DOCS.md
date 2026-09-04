# Linux Box

A personal Debian-based add-on providing SSH and Samba access to your Home
Assistant host, as a replacement for the official "Terminal & SSH" and
"Samba share" add-ons.

## Before installing

Uninstall (or at least stop) the official **Terminal & SSH** and **Samba
share** add-ons first. This add-on runs with `host_network: true` and binds
the same ports (22 for SSH, 445/139 for Samba) — both can't run at once.

## First boot

1. Install the add-on, open its **Configuration** tab.
2. Paste your SSH public key into `authorized_keys` (one key per line).
3. Start the add-on.

This creates a Linux user named `nicolas` (passwordless `sudo`, no root SSH
login), whose home directory is `/share/linux-box/home/nicolas` — i.e. it
lives under Home Assistant's `/share` folder and persists there directly
(see "Home directories" below).

Connect with:

```
ssh nicolas@<your-ha-host>.local
```

(or the host's IP address — `<your-ha-host>.local` resolves via Home
Assistant OS's own mDNS responder, since this add-on shares its network
stack; that's separate from this add-on's own mDNS advertisement — see
"mDNS / Bonjour discovery" below).

Set `nicolas`'s Samba password by filling in `smb_password` in the add-on
config (applied on every start) — or, if you'd rather not put it in the
config, over SSH instead:

```bash
sudo smbpasswd -a nicolas
sudo persist-accounts
```

Then connect from macOS Finder with **Go → Connect to Server**:

```
smb://<your-ha-host>.local/share
```

Available shares: `config`, `local_apps`, `app_configs`, `ssl`, `share`,
`backup`, `media` (same paths as the official Samba add-on), plus a
`nicolas` share for the home directory.

## mDNS / Bonjour discovery

This add-on runs its own `avahi-daemon` (host_network gives it access to
the real LAN interfaces), advertising both Samba (`_smb._tcp`) and SSH
(`_ssh._tcp`). The Samba share now shows up automatically in Finder's
**Network** sidebar — no need to type `smb://` manually.

By default it advertises as `<your-ha-hostname>-linuxbox.local`, not plain
`<your-ha-hostname>.local` — deliberately distinct, since Home Assistant OS
almost certainly already runs its own avahi-daemon claiming that exact
name, and this add-on shares the same network namespace (`host_network:
true`) so it would otherwise collide. Set the `hostname` option in the
add-on config to override this — that value is used exactly as typed, for
both the mDNS name and Samba's netbios name, with no suffix added; pick
something that won't collide with anything else on your LAN. SSH's
`_ssh._tcp` advertisement isn't
shown anywhere in Finder's UI (Finder only browses SMB/AFP-type shares) —
it's there for other Bonjour-aware tools (e.g. `dns-sd -B _ssh._tcp`,
Discovery.app), not required for `ssh nicolas@<host>.local` to work, which
already works via Home Assistant OS's own mDNS responder.

## Home directories

Each Linux user's home directory lives on disk under
`/share/linux-box/home/<username>`, so it's included in `/share` and
persists independently of the add-on container. `/etc/passwd` points each
user's home field directly at this path — there's no single bind-mount over
`/home`.

## Adding another user (e.g. "bob")

There's no config-UI user management — add users manually over SSH as
`nicolas`:

```bash
sudo useradd --create-home --home-dir /share/linux-box/home/bob \
    --shell /bin/bash bob
sudo passwd bob                 # optional, only needed for local/sudo login
sudo smbpasswd -a bob           # optional, only needed for Samba access
sudo persist-accounts           # REQUIRED — see below
```

Restart the add-on (or just wait for the next boot) and `bob`'s Samba share
will appear automatically — the `smb.conf` is regenerated on every start
from whichever users have a home directory under
`/share/linux-box/home/`.

To let `bob` SSH in, add his key manually:

```bash
sudo -u bob mkdir -p /share/linux-box/home/bob/.ssh
sudo -u bob tee /share/linux-box/home/bob/.ssh/authorized_keys < /dev/null
# paste bob's public key(s) in, one per line, then Ctrl-D
sudo chmod 700 /share/linux-box/home/bob/.ssh
sudo chmod 600 /share/linux-box/home/bob/.ssh/authorized_keys
```

### `persist-accounts` — don't forget this

Linux account entries for human users (uid/gid ≥ 1000), sudo group
membership, and Samba's password database live *inside the container* and
are normally lost on an add-on rebuild/update. This add-on snapshots them
(filtered to just those human accounts — never the base image's own system
accounts) into its private `/data` volume (which survives updates, and is
only cleared on uninstall) and restores them automatically on every boot —
but only for the state that existed the last time `persist-accounts` ran.

Nothing is persisted automatically — that includes `nicolas`'s own Samba
password. Run `sudo persist-accounts` after every `useradd`, `passwd`,
`smbpasswd -a`, or `deluser` you run by hand (for `nicolas` or anyone else),
or the change will be lost the next time the add-on updates.

## What this add-on deliberately doesn't do

- **No Docker socket access.** `docker_api` in Home Assistant's add-on
  schema is read-only and requires disabling AppArmor protection for the
  add-on — not worth it for occasional Docker introspection. This add-on
  stays protected/confined.
- **No Spotlight search over Samba.** Real Spotlight requires compiling
  Samba from source with `--enable-spotlight` (not in Debian's stock
  package) plus a Tracker3 indexer and a session D-Bus — a heavy,
  security-update-forfeiting dependency for a "slim" personal box. Finder
  search still works, just client-side.
- **No NetBIOS (`nmbd`) or WS-Discovery (`wsdd`).** Neither matters for a
  Mac-only setup; connect by hostname (`.local`) or IP.
- **No config-UI user management.** Deliberately simple: only SSH
  `authorized_keys` and Samba `smb_password` for `nicolas` come from the
  add-on config. Every other account — and `nicolas`'s own Linux login
  password, if you want one — is set the same way, manually over SSH —
  `useradd`/`passwd`/`smbpasswd` + a `persist-accounts` call.
