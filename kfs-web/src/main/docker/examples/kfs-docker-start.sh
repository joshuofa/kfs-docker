docker -H tcp://127.0.0.1:2375 run -d --link kfs-mysql-demo:kfs-mysql-demo \
    --name kfs-docker \
    -v /var/local/kfs/configuration:/configuration:ro \
    -v /var/local/kfs/security:/security:ro \
    -v /var/local/kfs/transactional:/transactional:rw \
    -v /var/local/kfs/logs:/logs:rw \
    -p 8080:8080 uaksd/kuali-applications:kfs-docker-6.0.1-SNAPSHOT-build1 tomcat-start

