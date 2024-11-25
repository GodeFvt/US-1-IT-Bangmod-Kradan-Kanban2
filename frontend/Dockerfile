FROM node:lts-alpine AS build
WORKDIR /code
COPY package*.json vite.config.js index.html ./
RUN npm install
COPY . .
ARG VITE_CLIENT_ID
ARG VITE_AUTHORITY
RUN node ./node_modules/vite/bin/vite.js build --base=/us1/

FROM nginx:alpine AS run
COPY --from=build  /code/dist/ /code/dist/
COPY default.conf /etc/nginx/conf.d/default.conf