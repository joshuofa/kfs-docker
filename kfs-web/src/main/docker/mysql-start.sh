#!/bin/bash

if [ -e /tmp/mysql-init.sql ]; then
  /usr/sbin/mysqld --init-file=/tmp/mysql-init.sql
  rm /tmp/mysql-init.sql
else
  /usr/sbin/mysqld
fi

tail -f /var/log/mysqld.log