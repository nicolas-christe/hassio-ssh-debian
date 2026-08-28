ARG BUILD_FROM
FROM $BUILD_FROM

ENV LANG=C.UTF-8

RUN \
    apt-get update \
    && apt-get install -y --no-install-recommends \
        openssh-server \
        sudo \
        samba \
        samba-common-bin \
        samba-vfs-modules \
        smbclient \
        avahi-daemon \
        usbutils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /run/sshd /var/lib/samba/private \
    && rm -f /etc/ssh/ssh_host_* /etc/samba/smb.conf

COPY rootfs /

HEALTHCHECK --start-period=10s \
    CMD smbclient -L '\\localhost' -U '%' -m SMB3 >/dev/null 2>&1
