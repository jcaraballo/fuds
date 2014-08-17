#!/bin/bash

cd "$( dirname "$0" )"
source function_moan.bash

artefact_path=$( echo target/scala-2.11/fuds-*.jar )
artefact_filename=$( basename $artefact_path )
version=$( echo $artefact_filename | sed 's|fuds-||' | sed 's|\-.*.jar||' )

if [[ "${FUDS_PUBLISHER_PASSWORD}" == "" ]] ; then
  credentials=publisher
else
  credentials=publisher:${FUDS_PUBLISHER_PASSWORD}
fi

target_url=https://37.187.23.147:9010/fuds/${version}/${artefact_filename}
echo "Publishing fuds version ${version}: ${artefact_path} -> ${target_url}" >&2
curl -s --fail --cacert hallon-by-ip.cacert -3T ${artefact_path} --user $credentials $target_url >/dev/null || moan "Failed to publish ${artefact_path} to ${target_url}"

echo Success >&2
