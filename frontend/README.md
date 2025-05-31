# LVZ Polizeiticker - frontend

## Getting started

```bash
# all commands used in ./frontend
cd frontend

# install tools and frontend dependencies
pnpm install
```

Create environment file for `development mode`.

```bash
cp src/environments/environment.ts src/environments/environment.dev.ts
```

**Note**: These file will not be under version control but listed in .gitignore.

## Usage

### Recommendation

It is recommended to use a server to get full access of all angular.
For the other options your app should run on a server which you like.

### Run in development mode

```bash
# build, reachable on http://localhost/app/path/to/dist/
pnpm build:dev

# build and starts a server, rebuild after changes, reachable on http://localhost:4200/
pnpm start
```

### Package

```bash
# build in production mode, compressed
pnpm build:prod
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

* default: `./api/`
* type: `string`

### `appname`

Applicationwide title of the app, displayed in title and toolbar.

* default: `LVZ Polizeiticker`
* type: `string`

### `production`

Defines whether the app is in production or not.

* default: `false`
* type: `boolean`
* values: `true`/`false`

### `theme`

Name of a pre-build-theme or a custom theme.

* default: `rose-red`
* type: `string`
* values: `rose-red`/`azure-blue`/`magenta-violet`/`cyan-orange`/`custom`

To modify the custom theme just edit the colors and themes in `themes.scss`.
