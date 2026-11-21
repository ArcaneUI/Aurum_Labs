@echo off
echo Starting Aurum Labs site...
if not exist node_modules (
  echo Installing dependencies...
  npm install
)
npm run dev -- --open
