FROM openjdk:8-jdk-alpine
EXPOSE 8091
ADD /target/oclfootball.jar oclfootball.jar
ENTRYPOINT [ \
"java", \
"-jar", \
"oclfootball.jar" \
]