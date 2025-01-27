# Releasing to Maven Central

Releases are done automatically using GitHub actions.
Just bump the version number in settings.gradle and create a GitHub release.
This will keep on working until it doesn't every few years ;)
This document is here to help when that happens.

## GPG

Maven Central requires all packages to be signed.
This necessarily means the headache of key management.
Fortunately keys don't need to be regenerated very often,
but here's how to do it when you need to.

First, you can generate a key as described in the [docs](https://central.sonatype.org/publish/requirements/gpg/).

To export the private key, you can run the following command (requires passpharase):

```shell
gpg --output mavencentral.pgp --armor --export-secret-key you@example.com
```
