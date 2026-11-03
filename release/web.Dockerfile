FROM nginx:1.28-alpine
COPY config/nginx.conf /etc/nginx/conf.d/default.conf
COPY client/ /usr/share/nginx/html/
EXPOSE 80
