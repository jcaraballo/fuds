#!/bin/bash

cd "$( dirname "$0" )"
source function_moan.bash

./dist.bash || exit 1

version=$( echo target/scala-2.11/fuds-*.jar | sed 's|^target/scala-2.11/fuds-||' | sed 's|\.jar||' )

echo Publishing fuds version $version

version_url=https://api.bintray.com/content/jcaraballo/generic/fuds/${version}

#echo PUT $version_url
curl --fail -T target/scala-2.11/fuds-*.jar -ujcaraballo:${BINTRAY_API_KEY} ${version_url}/ || moan "Failed to push distribution to ${version_url}/"
echo

#echo POST ${version_url}/publish
curl --fail -X POST -ujcaraballo:${BINTRAY_API_KEY} ${version_url}/publish || moan "Failed to publish distribution with ${version_url}/publish"
echo
