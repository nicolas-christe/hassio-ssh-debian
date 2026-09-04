# Changelog

## 0.1.0

- Initial version: SSH (key-only, `nicolas` user with passwordless sudo) +
  Samba (HA folders + per-user home shares), persistent multi-user accounts
  in `/data`, Apple (`vfs_fruit`) compatibility, no Spotlight, no Docker
  socket access.
- Add an optional `smb_password` add-on config option to set `nicolas`'s
  Samba password on every start, instead of requiring `smbpasswd` over SSH.
- Image now bundles ESP-IDF's `eim` installation manager (from Espressif's
  own apt repo) plus its apt-level build prerequisites (`flex`, `bison`,
  `gperf`, `ccache`, `dfu-util`, `cmake`, `wget`, `libffi-dev`,
  `libssl-dev`), so ESP-IDF tooling survives an add-on rebuild instead of
  needing manual reinstallation.
