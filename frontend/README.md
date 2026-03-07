# lvz-viz - Frontend

The UI of LVZ police ticker.

## Getting started

```bash
# all commands used in ./frontend
cd frontend

# install tools and frontend dependencies
pnpm install
```

Create an environment file for `development mode`.

```bash
cp src/environments/environment.ts src/environments/environment.dev.ts
```

**Note**: This file will not be under version control but listed in .gitignore.

## Usage

### Use mock data (if working with mock data)

If you want to work with mock data, start the mock server in a separate terminal, reachable on [http://localhost:3000/](http://localhost:3000/).

Comment out the providers in `SearchComponent` and `StatisticComponent` to use the mock services.
Update your `environment.dev.ts` file `api` to `http://localhost:3000/`.

```bash
pnpm run mock
```

### Run in development mode

```bash
# build, reachable on http://localhost/app/path/to/dist/
pnpm run build:dev

# build and starts a server, rebuild after changes, reachable on http://localhost:4200/
pnpm run start
```

### Package

```bash
# build in production mode, compressed
pnpm run build:prod
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

All options have to be set in the environment files, but some of them do not need to be changed.
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
If you want to work with backend data, use [http://localhost:4200/api/](http://localhost:4200/api/).

* default: `./api/`
* type: `string`

### `appname`

Application-wide title of the app, displayed in the title and toolbar.

* default: `LVZ-Polizeiticker`
* type: `string`

### `production`

Defines whether the app is in production or not.

* default: `false`
* type: `boolean`
* values: `true`/`false`

### `theme`

Name of a built-in theme from angular-material or a custom light or dark theme.

* default: `indigo-pink`
* type: `string`
* values: `deeppurple-amber`/`indigo-pink`/`pink-bluegrey`/`purple-green`/`custom-light`/`custom-dark`

To create a custom light or dark theme, edit the colors and themes in `themes.scss`.
