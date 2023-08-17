# LVZ Polizeiticker

[![MIT license](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

Visualization of LVZ police ticker.

## Prerequisites

### Angular CLI

* `@angular/cli 16.2.0` or higher

### Node, npm or yarn

* `node 18.16.0` or higher in combination with
  * `npm 9.5.1` or higher, used in this repository, or
  * `yarn 1.22.19` or higher

## Getting started

```bash
# clone project
git clone
cd lvz-viz

# install tools and frontend dependencies
npm install
```

Create environment files for `development mode` and `production mode`.

```bash
cp src/environments/environment.ts src/environments/environment.dev.ts
cp src/environments/environment.ts src/environments/environment.prod.ts
```

**Note**: These files will not be under version control but listed in .gitignore.

## Usage

### Recommendation

It is recommended to use a server to get full access of all angular.
For the other options your app should run on a server which you like.

### Run in development mode

If you want to work with mock data, start the mock server in a separate terminal, reachable on [http://localhost:3000/](http://localhost:3000/).

Comment out the providers in `SearchComponent` and `StatisticComponent` to use the mock services.
Update your `environment.dev.ts` file `api` to `http://localhost:3000/`.

```bash
npm run serve:mock
```

```bash
# build, reachable on http://localhost/app/path/to/dist/
npm run build:dev

# build and starts a server, rebuild after changes, reachable on http://localhost:4200/
npm run serve:dev

# build, rebuild after changes, reachable on http://localhost/app/path/to/dist/
npm run watch:dev
```

### Package

```bash
# build in production mode, compressed
npm run build:prod
```

### Tests

```bash
# test
ng test

# e2e
ng e2e
```

## Configuration

### General

All options have to be set in the environment files but some of them do not need to be changed.
All defaults refer to the environment file (`environment.ts`), they are prepared in `development mode` (`environment.dev.ts`).
Change for `production mode` the option `production` to `true`.

### Table of contents

* [api](#api)
* [appname](#appname)
* [production](#production)
* [theme](#theme)

### `api`

Defines the URL to the backend.
If you want to work with mock data, use [http://localhost:3000/](http://localhost:3000/).

* default: `./api/`
* type: `string`

### `appname`

Application-wide title of the app, displayed in title and toolbar.

* default: `LVZ Polizeiticker`
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
