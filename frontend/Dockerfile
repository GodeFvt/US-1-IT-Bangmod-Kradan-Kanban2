FROM node:lts-alpine AS build
WORKDIR /code
COPY package*.json vite.config.js index.html ./
RUN npm install
COPY . .
ARG VITE_CLIENT_ID
ARG VITE_AUTHORITY
ARG VITE_API_ROOT
ARG VITE_GOOGLE_LOGIN_URL
ENV VITE_API_ROOT=$VITE_API_ROOT
ENV VITE_GOOGLE_LOGIN_URL=$VITE_GOOGLE_LOGIN_URL
RUN node ./node_modules/vite/bin/vite.js build

FROM nginx:alpine AS run
COPY --from=build  /code/dist/ /code/dist/
COPY default.conf /etc/nginx/conf.d/default.conf
