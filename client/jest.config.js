module.exports = {
  collectCoverage: true,
  coverageReporters: ['html', 'cobertura'],
  /** check: https://thymikee.github.io/jest-preset-angular/docs/getting-started/options */
  moduleNameMapper: {
    '^(.*)/environments/(.*)$': '<rootDir>/src/environments/environment.ts',
    "^(.*)/shared/(.*)$": "<rootDir>/src/app/shared/$2",
  },
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testPathIgnorePatterns: ["<rootDir>/cypress"],
};
