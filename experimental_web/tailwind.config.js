/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    screens: {
      'xs': { 'max': '599.98px' },
      'sm': { 'min': '600px', 'max': '959.98px' },
      'md': { 'min': '960px', 'max': '1279.98px' },
      'lg': { 'min': '1280px', 'max': '1919.98px' },
      'xl': { 'min': '1920px' },
      'lt-md': { 'max': '959.98px' }
    },
    extend: {},
  },
  plugins: [],
}
