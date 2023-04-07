# pedestal-api-helper

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.majorcluster/pedestal-api-helper.svg)](https://clojars.org/org.clojars.majorcluster/pedestal-api-helper)

A Clojure library designed to extend usual pedestal api setup, providing:
* useful interceptors
* useful utils for dealing with validation and filtering, for example

[Clojars link](https://clojars.org/org.clojars.majorcluster/pedestal-api-helper)

## Usage

* Add the dependency:
```clojure
[org.clojars.majorcluster/pedestal-api-helper "LAST RELEASE NUMBER"]
```

## Examples:
[Read the docs](doc/index.md)

## Publish
### Requirements
* Leiningen (of course 😄)
* GPG (mac => brew install gpg)
* Clojars account
* Enter clojars/tokens page in your account -> generate one and use for password
```shell
export GPG_TTY=$(tty) && lein deploy clojars
```

## Documentation
[Link](doc/index.md)
