#!/bin/bash

function existIfError() {
     [ $? -ne 0 ] && exit 1
}

RESOURCES_PATH="src/main/resources"
STATIC_PATH="${RESOURCES_PATH}/static"

[ ! -d "../kitty" ] && exit 1
[ ! -d "../whale" ] && exit 1
[ -d "${STATIC_PATH}" ] && rm -rf ${STATIC_PATH}

cd ../kitty && npm run build
existIfError

cp -r ./asset/* ./static/
existIfError

cd ../rabbit && cp -r ../kitty/static ${RESOURCES_PATH}
existIfError

cd ../whale && mvn clean install
existIfError

cd ../rabbit && mvn clean install
existIfError

rm -rf ${STATIC_PATH}