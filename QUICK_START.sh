#!/usr/bin/env bash
set -e
echo "Start Java backend: cd backend-java && mvn spring-boot:run"
echo "Start HTML frontend: python -m http.server 3001 --directory frontend-html"
echo "Open http://localhost:3001"
