FROM adoptopenjdk/openjdk11:jre

ARG MODULE

WORKDIR /app

ADD dep-libs.tar    /app/lib/
COPY qq-read-task        /app/bin/run
ADD *.jar           /app/lib/

RUN ln -fs /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

CMD ["/app/bin/run"]