
[{{ .username }}]
   browseable = yes
   writeable = yes
   path = {{ .path }}
   valid users = {{ .username }}
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   veto files = /._*/.DS_Store/Thumbs.db/icon?/.Trashes/
   delete veto files = yes
