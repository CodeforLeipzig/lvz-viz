module.exports = {
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testPathIgnorePatterns: ["<rootDir>/cypress"],
  moduleNameMapper: {
    '^(.*)/environments/(.*)$': '<rootDir>/src/environments/environment.ts',
  }
};
