#!/usr/bin/env bash

./gradlew :lib:asynckv:clean
./gradlew :lib:asynckv:publish

./gradlew :lib:asynckv-preference:clean
./gradlew :lib:asynckv-preference:publish

./gradlew :lib:asynckv-mmkv:clean
./gradlew :lib:asynckv-mmkv:publish

./gradlew :lib:asynckv-datastore:clean
./gradlew :lib:asynckv-datastore:publish

