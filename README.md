# LVZ Polizeiticker

![GitHub license](https://img.shields.io/github/license/CodeforLeipzig/lvz-viz.svg)
[![Java CI with Gradle](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/java_ci.yml/badge.svg)](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/java_ci.yml)
[![Node CI](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/node_ci.yml/badge.svg)](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/node_ci.yml)
[![Build Fullstack App](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/build.yml/badge.svg)](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/build.yml)

## Intro

Visualization of [LVZ police ticker](https://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig).

The official website is hosted at <https://lvz-viz.leipzig.codefor.de>
by [OK Lab Leipzig](http://codefor.de/projekte/2014-07-01-le-lvz_polizeiticker_visualisierung.html).

## Prerequisites

### Node, npm or pnpm

* `node 22.14.0` or higher in combination with
  * `npm 10.9.2` or higher or
  * `pnpm 10.4.1` or higher, used in this repository

It's recommended to use [nvm (Node version Manager)](https://github.com/nvm-sh/nvm).

Install pnpm by running:

```bash
npm install -g pnpm@10.4.1
```

This repo uses `pnpm` as package manager.
You can also use `npm` for your local work but changes will be made by `pnpm` only.

### Angular CLI

* `@angular/cli 19.2.8` or higher

Install @angular/cli by running:

```bash
pnpm install -g @angular/cli@19
```

### Java

* `jdk 17` or higher

### Docker (when running services within docker)

* `docker 28.0.2` or higher
* `docker compose v2.34.0` or higher

## Getting started

```bash
# clone project
git clone https://github.com/CodeforLeipzig/lvz-viz
cd lvz-viz
```

### Read more

Check the documentation for each module/component.

For frontend check [lvz-viz - frontend](./frontend/README.md).

For backend check [lvz-viz - backend](./backend/README.md).

For docker check [lvz-viz - docker](./README_docker.md).

### Install Tools

Some tools are both used by backend and frontend.
Run the following command to install:

```bash
pnpm install
```

### Starting the application

For development, you can use two separate terminals to start the backend and frontend separately.
You can find more information in the README files in the separate folders.

You can also use the following command in the root directory to start in a single terminal:

```bash
pnpm start
```
