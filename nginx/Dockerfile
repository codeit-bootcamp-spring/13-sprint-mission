FROM nginx:alpine

COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

COPY src/main/resources/static/ /usr/share/nginx/html/

EXPOSE 80