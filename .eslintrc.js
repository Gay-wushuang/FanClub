module.exports = {
  root: true,
  extends: ['prettier'],
  plugins: ['prettier'],
  rules: {
    'prettier/prettier': 'error',
  },
  ignorePatterns: ['node_modules', 'dist', 'build', '.next'],
};


