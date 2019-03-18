# gcs-offset-backing-store [![CircleCI](https://circleci.com/gh/hden/gcs-offset-backing-store/tree/master.svg?style=svg)](https://circleci.com/gh/hden/gcs-offset-backing-store/tree/master) [![codecov](https://codecov.io/gh/hden/gcs-offset-backing-store/branch/master/graph/badge.svg)](https://codecov.io/gh/hden/gcs-offset-backing-store) [![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fhden%2Fgcs-offset-backing-store.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fhden%2Fgcs-offset-backing-store?ref=badge_shield)

A Kafka Connect OffsetBackingStore backed by Google Cloud Storage.

## Usage

```connector.properties
offset.storage=org.apache.kafka.connect.storage.gcs.GCSOffsetBackingStore
offset.storage.gcs.bucket=bucket
offset.storage.gcs.path=path/to/offsets.json
```

## Implementation Detail
Keys and values are Base64-encoded to support both JSON and Avro representation.

## License

Copyright © 2019 Haokang Den <haokang.den@gmail.com>

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.


[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fhden%2Fgcs-offset-backing-store.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fhden%2Fgcs-offset-backing-store?ref=badge_large)