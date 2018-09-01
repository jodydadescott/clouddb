FROM jodydadescott/alpine-java-kubectl:8u172b11_server-jre_unlimited

WORKDIR /
MAINTAINER jodyscott

RUN apk update
RUN apk add openssl bind-tools

ENV PATH="/app/bin:${PATH}"

ADD cache/app /app

CMD ["/app/bin/default"]
