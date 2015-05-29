FROM centos

# Install mysql 5.5 minimal server 
RUN rpmkeys --import http://repo.mysql.com/RPM-GPG-KEY-mysql \
  && yum install -y http://repo.mysql.com/yum/mysql-5.5-community/docker/x86_64/mysql-community-server-minimal-5.5.42-2.el7.x86_64.rpm \
  && rm -rf /var/cache/yum/* \
  && mkdir -p /etc/mysql

# setup mysql init script
RUN touch /tmp/mysql-init.sql \
  && echo "SET @@SESSION.SQL_LOG_BIN=0;" >> /tmp/mysql-init.sql \
  && echo "DELETE FROM mysql.user ;"  >> /tmp/mysql-init.sql \
  && echo "CREATE USER 'root'@'%' IDENTIFIED BY 'kuali' ;"  >> /tmp/mysql-init.sql \
  && echo "GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION ;"  >> /tmp/mysql-init.sql \
  && echo "CREATE DATABASE kfs;" >> /tmp/mysql-init.sql \
  && echo "CREATE USER 'kuldev'@'%' IDENTIFIED BY 'kuldev';" >> /tmp/mysql-init.sql \
  && echo "GRANT ALL PRIVILEGES ON kfs.* TO 'kuldev'@'%';" >> /tmp/mysql-init.sql \
  && echo 'FLUSH PRIVILEGES;' >> /tmp/mysql-init.sql


COPY mysql-start.sh /mysql-start.sh 
COPY my.cnf /etc/mysql/my.cnf
 
# create database
RUN  /usr/bin/mysql_install_db --force --skip-name-resolve \
    && chmod +x /mysql-start.sh

EXPOSE 3306

ENTRYPOINT ["/mysql-start.sh"]
