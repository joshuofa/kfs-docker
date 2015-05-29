FROM uaksd/kuali-applications:kuali-docker-base-0.1

MAINTAINER rbtucker@email.arizona.edu

# copy in the kfs build and utility scripts
COPY kfs-build /var/opt/kuali/tomcat/latest-tomcat/webapps
COPY scripts /

# set kfs web app directory owner and group
RUN chmod +x /usr/local/bin/* \
    && chown -R kualiadm:kuali /var/opt/kuali/tomcat/latest-tomcat/webapps/kfs

# create some useful shorcut environment variables
ENV TOMCAT_KFS_DIR=/var/opt/kuali/tomcat/latest-tomcat/webapps/kfs
ENV TOMCAT_KFS_WEBINF_DIR=/var/opt/kuali/tomcat/latest-tomcat/webapps/kfs/WEB-INF
ENV TRANSACTIONAL_DIRECTORY=/transactional
ENV CONFIG_DIRECTORY=/configuration
ENV LOGS_DIRECTORY=/logs
ENV SECURITY_DIRECTORY=/security
ENV TOMCAT_CONFIG_DIRECTORY=/configuration/tomcat-config
ENV KFS_CONFIG_DIRECTORY=/configuration/kfs-config

ENTRYPOINT /usr/local/bin/tomcat-start
