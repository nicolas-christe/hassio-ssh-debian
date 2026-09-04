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
        gh \
        gnupg \
        flex \
        bison \
        gperf \
        ccache \
        dfu-util \
        cmake \
        wget \
        libffi-dev \
        libssl-dev \
        binutils-gold \
        gcc \
        libcurl4-openssl-dev \
        libedit-dev \
        libicu-dev \
        libncurses-dev \
        libpython3-dev \
        libsqlite3-dev \
        libxml2-dev \
        pkg-config \
        uuid-dev \
    && install -d -m 0755 /etc/apt/keyrings \
    && curl -fsSL https://dl.espressif.com/dl/eim/eim.asc | gpg --dearmor -o /etc/apt/keyrings/eim.gpg \
    && printf 'Types: deb\nURIs: https://dl.espressif.com/dl/eim/apt\nSuites: stable\nComponents: main\nSigned-By: /etc/apt/keyrings/eim.gpg\n' \
        > /etc/apt/sources.list.d/eim.sources \
    && apt-get update \
    && apt-get install -y --no-install-recommends eim-cli \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /run/sshd /var/lib/samba/private \
    && rm -f /etc/ssh/ssh_host_* /etc/samba/smb.conf

COPY rootfs /

HEALTHCHECK --start-period=10s \
    CMD smbclient -L '\\localhost' -U '%' -m SMB3 >/dev/null 2>&1
