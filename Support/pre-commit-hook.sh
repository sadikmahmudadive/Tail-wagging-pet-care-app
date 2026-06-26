#!/bin/bash

# Pre-commit hook to prevent leaking API keys

echo "Running security check..."

# 1. Check for OpenAI Keys
if git diff --cached | grep -E "sk-proj-[a-zA-Z0-9]{30,}"; then
    echo "ERROR: Hardcoded OpenAI API key detected in staged changes!"
    echo "Please move the key to local.properties and use BuildConfig."
    exit 1
fi

# 2. Check for Google/Gemini Keys
if git diff --cached | grep -E "AIza[a-zA-Z0-9_-]{35}"; then
    echo "ERROR: Hardcoded Google API key detected in staged changes!"
    echo "Please move the key to local.properties and use BuildConfig."
    exit 1
fi

# 3. Ensure local.properties is not staged
if git diff --cached --name-only | grep "local.properties"; then
    echo "ERROR: local.properties is staged for commit!"
    echo "This file contains sensitive keys and should be kept local."
    exit 1
fi

echo "Security check passed."
exit 0
