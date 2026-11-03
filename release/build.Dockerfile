# Used only by the release maintainer. End users receive the output files.
FROM node:24.11.0-bookworm-slim AS client-build
WORKDIR /build/client
COPY client/package*.json ./
RUN npm ci --no-audit --no-fund
COPY client/tools ./tools
COPY client/src ./src
RUN npm test && npm run build && test ! -e dist/sprites

FROM maven:3.9.11-eclipse-temurin-21 AS server-build
WORKDIR /build/server
COPY server/pom.xml ./
COPY server/src ./src
RUN mvn -B -ntp clean verify

FROM scratch AS artifacts
COPY --from=client-build /build/client/dist /client
COPY --from=server-build /build/server/target/minimmorts-server.jar /server/minimmorts-server.jar
