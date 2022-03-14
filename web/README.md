# LVZ Polizeiticker

Visualization of LVZ police ticker.

This project was generated with [swaaplate](https://github.com/inpercima/swaaplate) version 2.3.2.

## Prerequisites

### Angular CLI

* `angular-cli 13.2.6` or higher

### Node, npm or yarn

* `node 16.13.0` or higher in combination with
  * `npm 8.1.0` or higher or
  * `yarn 1.22.17` or higher, used in this repository

## Getting started

```bash
# clone project
git clone https://github.com/CodeforLeipzig/lvz-viz
cd lvz-viz

# install tools and frontend dependencies
npm install
```

Create environment files for `devMode`, `mockMode` and `prodMode`.

```bash
cp src/environments/environment.ts src/environments/environment.dev.ts
cp src/environments/environment.ts src/environments/environment.mock.ts
cp src/environments/environment.ts src/environments/environment.prod.ts
```

**Note**: These files will not be under version control but listed in .gitignore.

## Usage

### Recommendation

It is recommanded to use a server to get full access of all angular.
You can do this for example with `npm run serve:mock`.
For the other options your app should run on a server which you like.

### Run in devMode

If you want to work with mock data, start the mock in a separate terminal, reachable on [http://localhost:3000/](http://localhost:3000/).

```bash
# mock, separate terminal
npm run run:mock
```

```bash
# build, reachable on http://localhost/app/path/to/dist/
npm run build:dev
# with mock
npm run build:mock

# build and starts a server, rebuild after changes, reachable on http://localhost:4200/
npm run serve:dev
# with mock
npm run serve:mock

# build, rebuild after changes, reachable on http://localhost/app/path/to/dist/
npm run watch:dev
# with mock
npm run watch:mock
```

### Package

```bash
# build in prodMode, compressed
npm run build:prod
```

### Tests

```bash
# test
ng test

# test with coverage (to be found in ./coverage)
ng test -- --coverage

# watch mode
ng test -- --watch

# watch mode with specific file
ng test -- --watch <filename>

# e2e
ng e2e
```

## Configuration

### General

All options have to been set in the environment files but some of them do not need to be changed.
All defaults refer to the environment file (`environment.ts`), they are prepared in devMode (`environment.dev.ts`).
Change for prodMode the option `production` to `true` and for mockMode the option `api` to `http://localhost:3000/`.

### Table of contents

* [api](#api)
* [appname](#appname)
* [defaultRoute](#defaultRoute)
* [production](#production)
* [theme](#theme)

### `api`

Defines the URL to the backend.

* default: `./`
* type: `string`

### `appname`

Applicationwide title of the app, displayed in title and toolbar.

* default: `LVZ Polizeiticker`
* type: `string`

### `defaultRoute`

The default route and the route to be redirected after a login if no route is stored or if a route does not exist.

* default: `dashboard`
* type: `string`

### `production`

Defines whether the app is in production or not.

* default: `false`
* type: `boolean`
* values: `true`/`false`

### `theme`

Name of a build-in theme from angular-material or a custom light or dark theme.

* default: `indigo-pink`
* type: `string`
* values: `deeppurple-amber`/`indigo-pink`/`pink-bluegrey`/`purple-green`/`custom-light`/`custom-dark`

To create a custom light or dark theme just edit the colors and themes in `themes.scss`.
