
[{{ .username }}]
   browseable = yes
   writeable = yes
   path = {{ .path }}
   valid users = {{ .username }}
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba
