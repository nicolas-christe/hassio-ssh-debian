[global]
   netbios name = {{ env "HOSTNAME" }}
   workgroup = {{ .workgroup }}
   server string = Linux Box

   security = user
   ntlm auth = yes
   idmap config * : backend = tdb
   idmap config * : range = 1000000-2000000

   min protocol = SMB2
   ea support = yes

   load printers = no
   disable spoolss = yes

   log level = 1

   hosts allow = 127.0.0.1 10.0.0.0/8 172.16.0.0/12 192.168.0.0/16 169.254.0.0/16 fe80::/10 fc00::/7

   mangled names = no
   dos charset = CP850
   unix charset = UTF-8

   kernel oplocks = yes

[config]
   browseable = yes
   writeable = yes
   path = /homeassistant
   force user = root
   force group = root
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba

[local_apps]
   browseable = yes
   writeable = yes
   path = /local_apps
   force user = root
   force group = root
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba

[app_configs]
   browseable = yes
   writeable = yes
   path = /app_configs
   force user = root
   force group = root
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba

[ssl]
   browseable = yes
   writeable = yes
   path = /ssl
   force user = root
   force group = root
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba

[share]
   browseable = yes
   writeable = yes
   path = /share
   force user = root
   force group = root
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba

[backup]
   browseable = yes
   writeable = yes
   path = /backup
   force user = root
   force group = root
   kernel oplocks = no
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba

[media]
   browseable = yes
   writeable = yes
   path = /media
   force user = root
   force group = root
   kernel oplocks = no
   vfs objects = catia fruit streams_xattr
   fruit:aapl = yes
   fruit:nfs_aces = no
   fruit:metadata = stream
   fruit:veto_appledouble = no
   fruit:wipe_intentionally_left_blank_rfork = yes
   fruit:delete_empty_adfiles = yes
   fruit:model = MacSamba
