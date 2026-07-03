# Project Documentation

Torus Election Visualizer is a Quarkus web app with a plain HTML/CSS/JavaScript UI and a JSON election API.

## Layers

- resource: HTTP endpoint.
- validation: request validation.
- mapper: response mapping.
- service: contracts.
- service.impl: torus-specific implementations.
- model: domain objects and transport records.
