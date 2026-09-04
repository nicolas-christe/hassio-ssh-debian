# Changelog

## 0.1.0

- Initial version: SSH (key-only, `nicolas` user with passwordless sudo) +
  Samba (HA folders + per-user home shares), persistent multi-user accounts
  in `/data`, Apple (`vfs_fruit`) compatibility, no Spotlight, no Docker
  socket access.
- Add an optional `smb_password` add-on config option to set `nicolas`'s
  Samba password on every start, instead of requiring `smbpasswd` over SSH.
