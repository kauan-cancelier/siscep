/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        marca: { 50: '#eef3fb', 100: '#d5e1f4', 500: '#2f5ea8', 600: '#1e4078', 700: '#173263', 800: '#10244a' },
      },
    },
  },
  plugins: [],
};
