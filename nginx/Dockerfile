FROM nginx:1.30-alpine

# 요청 라우팅 설정과 미리 빌드된 프론트 정적 파일을 같은 Nginx 이미지에 포함한다.
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY src/main/resources/static/ /usr/share/nginx/html/

EXPOSE 80