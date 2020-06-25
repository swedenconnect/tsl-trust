FROM openjdk:8-jre

#RUN mkdir /opt/docker
#RUN mkdir /opt/docker/internal
ADD target/dependency/apache-tomcat-9.0.36 /opt/tomcat/
RUN rm -rf /opt/tomcat/webapps/docs
RUN rm -rf /opt/tomcat/webapps/examples
RUN rm -rf /opt/tomcat/webapps/manager
RUN rm -rf /opt/tomcat/webapps/host-manager

# Redirecting log directories
RUN rm -rf /opt/tomcat/logs && ln -s /var/log/sigval/ /opt/tomcat/logs

# Making scripts executable
RUN chmod a+x /opt/tomcat/bin/*.sh

# HTTP and HTTPS
EXPOSE 8443
EXPOSE 8080
#Debug port
EXPOSE 8000
# If AJP is enabled
#EXPOSE 8009

VOLUME /var/log/signservice
CMD mkdir -p /var/log/sigval/ && /opt/tomcat/bin/dockerStart.sh
