# Petstore

> An WIP microservice that serves the [Petstore](https://petstore.swagger.io) API

This microservice is built with the following libraries

+ http4s
+ cats-effect
+ tapir
+ fs2

## Modules
`backend` - domain model, repos, services
`api` - tapir-based REST API definition
`server` - http4s based server

## Development

I've only written one in-memory Repo and it uses a `TrieMap` for storage.

## Todo
+ Create Order & User servies/controllers