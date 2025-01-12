#!/bin/bash

# Define the directory you want to iterate through
TARGET_DIR="." # Change this to your directory path

# Navigate to the target directory
cd "$TARGET_DIR" || { echo "Directory not found! Exiting..."; exit 1; }

# Iterate through all files in the directory
for file in *; do
    # Add each file to git
    git add "$file"
    git commit -m "add $file"
    git push
done

echo "All files added, committed, and pushed successfully!"
