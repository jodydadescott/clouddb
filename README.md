# clouddb

set -e

pushd core
mvn clean install -Dmaven.test.skip=true
popd

pushd banzai
mvn clean package -Dmaven.test.skip=true
popd
